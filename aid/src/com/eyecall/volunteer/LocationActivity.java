package com.eyecall.volunteer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.RadioGroup;

import com.eyecall.event.ClickEvent;
import com.eyecall.eventbus.Event;
import com.eyecall.eventbus.EventBus;
import com.eyecall.eventbus.EventListener;
import com.eyecall.eventbus.InputEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class LocationActivity extends FragmentActivity implements EventListener{
    private com.eyecall.connection.Connection connection;
    
    private Location location;
    
    private GoogleMap map;
    
    private PreferencesManager preferencesManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	this.setContentView(R.layout.activity_location);
    	
    	preferencesManager = new PreferencesManager(this);
    	
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
    
    private void initMap(){
    	// Get a handle to the Map Fragment
    	map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.location_map)).getMap();
        map.setMyLocationEnabled(true);
        if(location==null){
        	LatLng position = new LatLng(0,0);
        	map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 10));
        }else{
        	LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
        	map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 18));
        	map.addMarker(new MarkerOptions().title("Location").position(position));
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
				if(location==null) location = new Location();
				//location.setLatitude(map.)
				RadioGroup radios = (RadioGroup) findViewById(R.id.location_radiogroup_preferred);
				int selected = radios.getCheckedRadioButtonId();
				location.setPreferred(preferred)
				
				
				preferencesManager.saveLocation(location);
			}
		}
		
	}
}
