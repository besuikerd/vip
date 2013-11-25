package com.eyecall.volunteer;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.eyecall.protocol.ProtocolField;
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
	
	private static final Logger logger = LoggerFactory.getLogger(MainActivity.class);
    
    private Location location;
    
    private GoogleMap map;
    
    private Marker marker;
    
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
                	position = new LatLng(map.getMyLocation().getLatitude(), map.getMyLocation().getLongitude());
                	map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 10));
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
    
    private String getVolunteerId(){
    	SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
		String volunteerId = null;
		if(preferences.contains(ProtocolField.VOLUNTEER_ID.getName())){
			volunteerId = preferences.getString(ProtocolField.VOLUNTEER_ID.getName(), null);
		}
		return volunteerId;
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
				String volunteerId = getVolunteerId();
				if(volunteerId==null){
					Toast.makeText(this, "No app id found. Please restart app", Toast.LENGTH_LONG).show();
					logger.warn("No volunteer id found");
					return;
				}
				
				// Make connection with server
				Connection connection;
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
					VolunteerProtocolHandler.removeLocation(connection, volunteerId, location);
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
				VolunteerProtocolHandler.addLocation(connection, volunteerId, location);
				logger.debug("Added location: {}", location.toString());
				
				// Close connection
				try {
					connection.close();
				} catch (IOException exception) {
					logger.warn("Unable to close connection: {}", exception);
				}
				
				// toast
				Toast.makeText(this, "Location saved", Toast.LENGTH_LONG).show();
				
				// Go back to list
				this.finish();
			}
		}
		
	}
}
