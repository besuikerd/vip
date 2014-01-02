package com.eyecall.volunteer;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TableRow;
import android.widget.Toast;

import com.eyecall.connection.Connection;
import com.eyecall.event.CheckedChangedEvent;
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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class LocationActivity extends FragmentActivity implements EventListener, OnMapLongClickListener{
	
	private static final Logger logger = LoggerFactory.getLogger(LocationActivity.class);
    
    private Location location;
    
    private GoogleMap map;
    
    private Marker marker;
    
    private Circle circle;
    
    private TableRow radiusRow;
    
    private SeekBar radiusBar;

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
    		logger.debug("LocationActivity started with location {}", location);
    	}else{
    		location = null;
    		logger.debug("LocationActivity started without location");
    	}
    	
    	radiusRow = (TableRow) findViewById(R.id.tableRow_radius);
    	radiusBar = (SeekBar) findViewById(R.id.location_radius);
    	// Make invisible
    	if(location==null || location.isPreferred()){
    		radiusRow.setVisibility(View.INVISIBLE);
    	}
    	
    	// Register listeners and events
    	registerEvents();
    	
    	// Init map
    	initMap();
    	
    	// Draw radius
    	if(location!=null && !location.isPreferred()){
    		radiusBar.setProgress((int) (location.getRadius()*100));
    		drawCircle();
    	}
    }
    
    @Override
    public void onMapLongClick(LatLng point) {
    	marker.setPosition(point);
    	drawCircle();
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
    
    private void drawCircle(){    	
    	if(marker!=null && radiusRow.getVisibility()==View.VISIBLE){
    		// Instantiates a new CircleOptions object and defines the center and radius
    		if(circle==null){
    			CircleOptions circleOptions = new CircleOptions()
    		    .center(marker.getPosition())
    		    .radius(toKm(marker.getPosition(), (double)radiusBar.getProgress()/100)) // In meters
    		    .strokeColor(0xaaff0000)
    		    .fillColor(0x33ff0000)
    		    .strokeWidth(1);
    		    ; 
    			circle = map.addCircle(circleOptions);
    		}else{
    			circle.setCenter(marker.getPosition());
    			circle.setRadius(radiusBar.getProgress()*1000);
    		}
    	}else{
    		// Remove circle
    		if(circle!=null){
    			circle.remove();
    			circle = null;
    		}
    	}
    }
    
    /**
     * Calculates the radius in km by the given position and radius in degrees
     * @param position The position
     * @param i	The radius in degrees
     * @return Radius in kilometer
     * @see http://stackoverflow.com/questions/365826/calculate-distance-between-2-gps-coordinates
     */
    private double toKm(LatLng position, double degrees) {
    	double long1 = position.longitude;
    	double lat1 = position.latitude;
    	double long2 = position.longitude;
    	double lat2 = position.latitude + degrees;
    	double dlong = Math.toRadians(long2 - long1);
        double dlat = Math.toRadians(lat2 - lat1);
        double a = Math.pow(Math.sin(dlat/2.0), 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.pow(Math.sin(dlong/2.0), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = 6367 * c;
        
        return d;
	}

	private void registerEvents(){
    	findViewById(R.id.location_button_cancel).setOnClickListener(new InputEventListener(EventTag.CANCEL_LOCATION_ADD, null));
    	findViewById(R.id.location_button_save).setOnClickListener(new InputEventListener(EventTag.SAVE_LOCATION, null));
    	((RadioGroup)findViewById(R.id.location_radiogroup_preferred)).setOnCheckedChangeListener(new InputEventListener(EventTag.LOCATION_PREFERRED_CHANGED, null));
    	radiusBar.setOnSeekBarChangeListener(new InputEventListener(EventTag.LOCATION_RADIUS_CHANGED, null));
    	EventBus.getInstance().subscribe(this);
    }

	@Override
	public void onEvent(Event e) {
		switch(EventTag.lookup(e.getTag())){
		case LOCATION_PREFERRED_CHANGED:
			CheckedChangedEvent event = (CheckedChangedEvent)e;
			if(event.getCheckedId()==R.id.location_radio_preferred){
				radiusRow.setVisibility(View.INVISIBLE);
			}else{
				radiusRow.setVisibility(View.VISIBLE);
			}
			drawCircle();
			break;
		case LOCATION_RADIUS_CHANGED:
			drawCircle();
			break;
		case CANCEL_LOCATION_ADD:
			this.finish();
			break;
		case SAVE_LOCATION:
			// Get volunteer id
			if(Constants.VOLUNTEER_ID==null){
				Toast.makeText(this, "No app id found. Please restart app", Toast.LENGTH_LONG).show();
				logger.warn("No volunteer id found");
				return;
			}
			
			// Disable save button
			this.findViewById(R.id.location_button_save).setEnabled(false);
			
			// Make connection with server
			if(!connect()) return;
			
			// Check if old location should be removed
			if(location==null){
				location = new Location();
			}else{
				// First remove old location
				VolunteerProtocolHandler.removeLocation(connection, Constants.VOLUNTEER_ID, location);
				logger.debug("Removed location: {}", location.toString());
				
				// Server disconnected => reconnect
				if(!connect()) return;
			}
			
			// Get input 
			RadioGroup radios = (RadioGroup) findViewById(R.id.location_radiogroup_preferred);
			int selected = radios.getCheckedRadioButtonId();
			location.setPreferred(selected==R.id.location_radio_preferred);
			location.setLatitude(marker.getPosition().latitude);
			location.setLongitude(marker.getPosition().longitude);
			location.setRadius(location.isPreferred() ? 0.0 : (double)radiusBar.getProgress()/100);
			location.setId(-1);
			
			// Add new location
			VolunteerProtocolHandler.addLocation(connection, Constants.VOLUNTEER_ID, location);
			EventBus.getInstance().post(new Event(EventTag.LOCATION_ADDED, location));
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
			// Toast
			Toast.makeText(this, "Location saved", Toast.LENGTH_LONG).show();
			
			// Go back to list
			this.finish();
			break;
		default:
			break;
		}
	}
	
	private boolean connect(){
		try {
			connection = new Connection(Constants.SERVER_URL, Constants.SERVER_PORT, new VolunteerProtocolHandler(), VolunteerState.IDLE);
			connection.init(false);
			return true;
		} catch (IOException exception) {
			Toast.makeText(this, "Unable to connect to server. Try again later", Toast.LENGTH_LONG).show();
			logger.warn("Unable to connect to server: {}", exception.getMessage());
			return false;
		}
	}
}
