package com.eyecall.volunteer;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.eyecall.connection.Connection;
import com.eyecall.event.ClickEvent;
import com.eyecall.eventbus.Event;
import com.eyecall.eventbus.EventBus;
import com.eyecall.eventbus.EventListener;
import com.eyecall.eventbus.InputEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class LocationActivity extends FragmentActivity implements EventListener, OnMapLongClickListener{
	
	private static final Logger logger = LoggerFactory.getLogger(LocationActivity.class);
    
    private Location location;
    
    private GoogleMap map;
    
    private Marker marker;

	private Connection connection;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	this.setContentView(R.layout.activity_location);
    	
    	//preferencesManager = new PreferencesManager(this);
    	
    	// Set location
    	Intent intent = getIntent();
    	if(intent.hasExtra("location")){
    		location = intent.getParcelableExtra("location");
    	}
    	
    	// Register listeners and events
    	registerEvents();
    	
    	// Init map
    	initMap();
    }
    
    @Override
    public void onMapLongClick(LatLng point) {
    	marker.setPosition(point);
    }
    
    private void initMap(){
    	int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
    	if(status==ConnectionResult.SUCCESS){
    		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_map)).getMap();
            if(map!=null){
            	// Setup map
            	map.setMyLocationEnabled(true);
                LatLng position;
                if(location==null){
                	// New location
                	android.location.Location myLocation = map.getMyLocation();
                	if(myLocation!=null){
                		position = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                		map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 10));
                	}else{
                		position = new LatLng(0,0);
                		map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 1));
                	}
                }else{
                	// Exisiting location
                	position = new LatLng(location.getLatitude(), location.getLongitude());
                	map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 18));
                }
                map.setOnMapLongClickListener(this);
                marker = map.addMarker(new MarkerOptions().title("Location").position(position));
            }else{
            	logger.warn("Map not loaded, onCreateView not finished");
            }
    	}else{
    		logger.warn("Map not loaded, GooglePlayServices unavailable:");
    		if(status==ConnectionResult.SERVICE_MISSING){
    			logger.warn("SERVICE_MISSING");
    		}else if(status==ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED){
    			logger.warn("SERVICE_VERSION_UPDATE_REQUIRED");
    		}else if(status==ConnectionResult.SERVICE_DISABLED){
    			logger.warn("SERVICE_DISABLED");
    		}else if(status==ConnectionResult.SERVICE_INVALID){
    			logger.warn("SERVICE_INVALID");
    		}
    	}
    }
    
    private void registerEvents(){
    	findViewById(R.id.location_button_cancel).setOnClickListener(new InputEventListener(EventTag.CANCEL_LOCATION_ADD, null));
    	findViewById(R.id.location_button_save).setOnClickListener(new InputEventListener(EventTag.SAVE_LOCATION, null));
    	EventBus.getInstance().subscribe(this);
    }

	@Override
	public void onEvent(Event e) {
		if(e instanceof ClickEvent){
			ClickEvent event = (ClickEvent) e;
			if(event.getTag().equals(EventTag.CANCEL_LOCATION_ADD.getName())){
				this.finish();
			}else if(event.getTag().equals(EventTag.SAVE_LOCATION.getName())){
				// Get volunteer id
				if(Constants.VOLUNTEER_ID==null){
					Toast.makeText(this, "No app id found. Please restart app", Toast.LENGTH_LONG).show();
					logger.warn("No volunteer id found");
					return;
				}
				
				// Make connection with server
				try {
					connection = new Connection(Constants.SERVER_URL, Constants.SERVER_PORT, new VolunteerProtocolHandler(), VolunteerState.IDLE);
					connection.init(false);
				} catch (IOException exception) {
					Toast.makeText(this, "Unable to connect to server. Try again later", Toast.LENGTH_LONG).show();
					logger.warn("Unable to connect to server: {}", exception.getMessage());
					return;
				}
				
				// Check if old location should be removed
				if(location==null){
					location = new Location();
				}else{
					// First remove old location
					VolunteerProtocolHandler.removeLocation(connection, Constants.VOLUNTEER_ID, location);
					logger.debug("Removed location: {}", location.toString());
				}
				
				// Get input 
				RadioGroup radios = (RadioGroup) findViewById(R.id.location_radiogroup_preferred);
				int selected = radios.getCheckedRadioButtonId();
				location.setPreferred(selected==R.id.location_radio_preferred);
				location.setLatitude(marker.getPosition().latitude);
				location.setLongitude(marker.getPosition().longitude);
				location.setRadius(0);
				
				// Add new location
				VolunteerProtocolHandler.addLocation(connection, Constants.VOLUNTEER_ID, location);
				logger.debug("Added location: {}", location.toString());
				
				// Close connection
				// Run in other thread to avoid blocking of UI thread
				new Thread(){
					@Override
					public void run() {
						try {
							connection.close();
						} catch (IOException exception) {
							logger.warn("Unable to close connection: {}", exception);
						}
					};
				}.start();
				
				logger.debug("Location saved: {}", location.toString());
				// toast
				Toast.makeText(this, "Location saved", Toast.LENGTH_LONG).show();
				
				// Go back to list
				this.finish();
			}
		}
		
	}
}
