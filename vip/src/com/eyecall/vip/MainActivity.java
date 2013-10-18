package com.eyecall.vip;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import com.eyecall.connection.Connection;
import com.eyecall.connection.Message;
import com.eyecall.connection.ProtocolHandler;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.view.WindowManager;
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
	private static final String SERVER_ADDRESS = null;
	/** Server port */
	private static final int SERVER_PORT = 0;
	
	public static Connection connection;
	/** LocationClient used for getting last known location */
	private LocationClient locationClient;
	
    @Override
    protected void onStart() {
        super.onStart();
        
        // Determine current location via Google Services
        locationClient = new LocationClient(this, this, this);
        // Connect the client.
        locationClient.connect();
        Location location = locationClient.getLastLocation();
	    
        // Initialize connection with the server
		try {
			initConnection();
		} catch (UnknownHostException e) {
			Toast.makeText(this, R.string.error_unknown_host, Toast.LENGTH_LONG).show();
			return;
		} catch (IOException e) {
			Toast.makeText(this, R.string.error_connection_failed, Toast.LENGTH_LONG).show();
			return;
		}
		
		// Send help request
		sendHelpRequest(location);
		
		// Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
        // And wait for response ... 
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        locationClient.disconnect();
        super.onStop();
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
		connection = new Connection(socket, new VIPProtocolHandler(), VIPState.IDLE);
	}
	
	private void sendHelpRequest(Location location){
		connection.send(new Message(ProtocolHandler.REQUEST_HELP).add("longitude", location.getLongitude()).add("latitude", location.getLatitude()));
	}

	public void openHelpActivity(){
		Intent intent = new Intent(getApplicationContext(), HelpActivity.class);
		this.startActivity(intent);
	}

	/*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
        Toast.makeText(this, "Google Services Connected", Toast.LENGTH_SHORT).show();
    }
    
    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
        // Display the connection status
        Toast.makeText(this, "Google Services Disconnected", Toast.LENGTH_SHORT).show();
    }
    
    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
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

	
}
