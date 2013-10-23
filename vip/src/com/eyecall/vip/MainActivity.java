package com.eyecall.vip;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import com.eyecall.connection.Connection;
import com.eyecall.event.ClickEvent;
import com.eyecall.event.EventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

/**
 * The main Activity of this Applications
 */
public class MainActivity extends Activity implements GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener {
	
	/*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    /** Tag for this app used for debugging */
	public static final String TAG = "Eyecall VIP";

	/** Server address or hostname */
	private static final String SERVER_ADDRESS = "123.123.123.123";
	/** Server port */
	private static final int SERVER_PORT = 123;
	
	/** ProtocolHandler for this app */
	public static VIPProtocolHandler protocolHandler;
	/** Connection with the server */
	public static Connection connection;
	
	private static MainActivity instance;
	
	
	/** LocationClient used for getting last known location */
	private LocationClient locationClient;
	
	/** Known location of VBP, could be null */
	private Location location = null;
	
	private EventHandler eventHandler;

	public MainActivity(){
		instance = this;
		eventHandler = new EventHandler(this);
	}
	
	public static MainActivity getInstance(){
		return instance;
	}
	
	/* *****************************************************
	 *                     CALLBACKS
	 * *****************************************************/
	
	@Override
	protected void onStart() {
	    super.onStart();
	    
	    // Register for events
	    EventBus.getInstance().register(eventHandler);
	    
	    // Add listener to button
	    Button button = (Button) findViewById(R.id.button_request);
	    button.setOnClickListener(new InputEventListener("button_request", null));
	    
	    // Keep screen on
	    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	    
	    connectToGoogleSevices();
	    // After connected to Google, app will continue
	    // Because in order to continue the location needs to be known
	}

	@Override
	protected void onStop() {
	    // Disconnecting the client invalidates it.
	    locationClient.disconnect();
	    super.onStop();
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
        Log.d(MainActivity.TAG, "Location found: lat:" + location.getLatitude() + " long:" + location.getLongitude());
        connectToServer();
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
        connectToServer();
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
    
    private void connectToServer(){
    	// Initialize connection with the server
		try {
			Log.d(TAG, "initConnection(): start");
			initConnection();
			Log.d(TAG, "initConnection(): completed");
		} catch (UnknownHostException e) {
			Toast.makeText(this, R.string.error_unknown_host, Toast.LENGTH_LONG).show();
			Log.d(TAG, "initConnection(): UnknownHostException");
			Log.d(TAG, e.getMessage());
			enableRequestButton();
			return;
		} catch (IOException e) {
			Toast.makeText(this, R.string.error_connection_failed, Toast.LENGTH_LONG).show();
			Log.d(TAG, "initConnection(): IOException");
			Log.d(TAG, e.getMessage());
			enableRequestButton();
			return;
		}
		
		sendRequest();
    }
    
    
    private void sendRequest(){
		// Send help request
		protocolHandler.sendHelpRequest(location);
		
        // And wait for response ... (VIPProtocolHandler)
    }

    private void enableRequestButton() {
		Button button = (Button) findViewById(R.id.button_request);
		button.setEnabled(true);
	}
    
    private void disableRequestButton() {
		Button button = (Button) findViewById(R.id.button_request);
		button.setEnabled(false);
	}

	/**
	 * Initialize the connection with the server
	 * @throws IOException 
	 * @throws UnknownHostException 
	 * @ensure this.connection!=null
	 */
	private void initConnection() throws UnknownHostException, IOException {
		if(connection!=null) return;
		Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
		protocolHandler = new VIPProtocolHandler();
		connection = new Connection(socket, protocolHandler, VIPState.IDLE);
	}

	public void openHelpActivity(){
		Log.d(MainActivity.TAG, "Opening HelpActivity...");
		Intent intent = new Intent(getApplicationContext(), HelpActivity.class);
		this.startActivity(intent);
	}
	
	private class EventHandler implements EventListener{
		private Activity activity;
		
		public EventHandler(Activity activity){
			this.activity = activity;
		}
		
		@Override
		public void onEvent(Event e){
			if(e.getTag().equals("button_request")){
				if(!(e instanceof ClickEvent)) return;
				disableRequestButton();
				if(locationClient==null || !locationClient.isConnected()){
					connectToGoogleSevices();
				}else{
					connectToServer();
				}
			}
		}
	}
	
	

	
}
