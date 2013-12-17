package com.eyecall.vip;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.eyecall.android.ConnectionInstance;
import com.eyecall.connection.Connection;
import com.eyecall.connection.Message;
import com.eyecall.event.ClickEvent;
import com.eyecall.event.RequestGrantedEvent;
import com.eyecall.eventbus.Event;
import com.eyecall.eventbus.EventBus;
import com.eyecall.eventbus.EventListener;
import com.eyecall.eventbus.InputEventListener;
import com.eyecall.protocol.ProtocolField;
import com.eyecall.protocol.ProtocolName;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

/**
 * The main Activity of this Applications
 */
public class MainActivity extends Activity implements GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener, EventListener{
	private static final Logger logger = LoggerFactory.getLogger(MainActivity.class);
	
	
	/*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    /** Tag for this app used for debugging */
	public static final String TAG = "Eyecall VIP";

	/** Server address or hostname */
	private static final String SERVER_ADDRESS = "besuikerd.nl";
	/** Server port */
	private static final int SERVER_PORT = 5000;
	
	/** LocationClient used for getting last known location */
	private LocationClient locationClient;
	
	/** Known location of VBP, could be null */
	private Location location = null;
	
	/* *****************************************************
	 *                     CALLBACKS
	 * *****************************************************/
	@Override
	protected void onCreate(Bundle savedInstance) {
	    super.onCreate(savedInstance);
	    setContentView(R.layout.activity_main);
	    // Register for events
	    EventBus.getInstance().subscribe(this);
	    
	    // Add listener to button
	    ((Button) findViewById(R.id.button_request)).setOnClickListener(new InputEventListener(EventTag.REQUEST_BUTTON_PRESSED.getName(), null));
	    
	    // Keep screen on
	    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	    
	    connectToGoogleSevices();
	    // After connected to Google, app will continue
	    // Because in order to continue the location needs to be known
	}

	@Override
	protected void onPause() {
		super.onPause();
		Connection c = null;
		if((c = ConnectionInstance.getExistingInstance()) != null && c.getState().equals(VIPState.WAITING)){
			c.send(new Message(ProtocolName.CANCEL_REQUEST));
		}
		//enable button again to allow new help requests
		if(locationClient != null){
			((Button) findViewById(R.id.button_request)).setEnabled(true);
		}
	}
	

	/**
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
    	
    	Log.d(MainActivity.TAG, "onConnected(): Google services connected (location)");
        // Display the connection status
        Toast.makeText(this, "Google Services Connected", Toast.LENGTH_SHORT).show();
        location = locationClient.getLastLocation();
        if(location==null){
        	 Log.d(MainActivity.TAG, "No last location known!");
        }else{
        	 Log.d(MainActivity.TAG, "Location found: lat:" + location.getLatitude() + " long:" + location.getLongitude());
        }
        ((Button) findViewById(R.id.button_request)).setEnabled(true);
        
        
        
        
        //register location listener
        locationClient.requestLocationUpdates(LocationRequest.create(), new LocationListener() {
			
			@Override
			public void onLocationChanged(final Location location) {
				logger.debug("location changed: ({},{})", location.getLatitude(), location.getLongitude());
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						Toast.makeText(getApplicationContext(), String.format("location update: (%f,%f)", location.getLatitude(), location.getLongitude()), Toast.LENGTH_SHORT).show();
					}
				});
				
				
				Connection c = null;
				try {
					if((c = ConnectionInstance.getInstance()) != null && c.getState().equals(VIPState.BEING_HELPED)){
						c.send(new Message(ProtocolName.UPDATE_LOCATION).add(ProtocolField.LATITUDE, location.getLatitude()).add(ProtocolField.LONGITUDE, location.getLongitude()));
					}
				} catch(UnknownHostException e){
					
				}
			}
		});
        
        new Timer().scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				
				final Location loc = locationClient.getLastLocation();
				if(loc != null){
					
					//post location update if state is BEING_HELPED
					try {
						Connection c = ConnectionInstance.getInstance(Constants.SERVER_URL, Constants.SERVER_PORT);
						if(c.getState().equals(VIPState.BEING_HELPED)){
							c.send(new Message(ProtocolName.UPDATE_LOCATION).add(ProtocolField.LATITUDE, loc.getLatitude()).add(ProtocolField.LONGITUDE, loc.getLongitude()));
						}
					} catch (UnknownHostException e) {
					}
				}
			}
		}, 0, 20000);
    }
    
    
    /**
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
        // Display the connection status
    	Log.d(MainActivity.TAG, "Google services disconnected (location)");
        Toast.makeText(this, "Google Services Disconnected", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    	Log.d(MainActivity.TAG, "Google services connection failed (location)");
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            showDialog(connectionResult.getErrorCode());
        }
    }
    
    /* *****************************************************
	 *                   OTHER METHODS
	 * *****************************************************/
	
    private void connectToGoogleSevices(){
    	// Determine current location via Google Services
	    locationClient = new LocationClient(this, this, this);
	    // Connect the client.
	    locationClient.connect();
	    // If ready -> onConnected (or onConnectionFailed) called
    }

    private void enableRequestButton() {
		Button button = (Button) findViewById(R.id.button_request);
		button.setEnabled(true);
	}
    
    private void disableRequestButton() {
		Button button = (Button) findViewById(R.id.button_request);
		button.setEnabled(false);
	}


	public void openHelpActivity(){
		Log.d(MainActivity.TAG, "Opening HelpActivity...");
		Intent intent = new Intent(getApplicationContext(), HelpActivity.class);
		this.startActivity(intent);
	}

	@Override
	public void onEvent(Event e) {
		if(e instanceof ClickEvent){
			// Send a new request ("HELP" button pressed)
			if(e.getTag().equals(EventTag.REQUEST_BUTTON_PRESSED.getName())){
				disableRequestButton();
				if(locationClient==null || !locationClient.isConnected()){
					connectToGoogleSevices();
				}
				Location l = locationClient.getLastLocation();
				try {
					logger.debug("recreating conn...");
					Connection c = ConnectionInstance.recreateConnection();
					logger.debug("conn recreated!");
					c.send(new Message(ProtocolName.REQUEST_HELP).add(ProtocolField.LATITUDE, l.getLatitude()).add(ProtocolField.LONGITUDE, l.getLongitude()));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
			}
		
		}else if(e instanceof RequestGrantedEvent){
			// Somebody is willing to help
			// Open HelpActivity
			openHelpActivity();
		}
	}	
}
