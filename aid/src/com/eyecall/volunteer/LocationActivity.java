package com.eyecall.volunteer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.eyecall.aid.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class LocationActivity extends Activity{
    private com.eyecall.connection.Connection connection;
    
    private Location location;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	this.setContentView(R.layout.activity_location);
    	
    	Intent intent = getIntent();
    	if(intent.hasExtra("location")){
    		location = intent.getParcelableExtra("location");
    	}
    	
    	// Get a handle to the Map Fragment
    	SupportMapFragment.newInstance().
        GoogleMap map = ((MapFragment) SupportMapFragment.newInstance().getFragmentManager().findFragmentById(R.id.location_map).getMap();
        map.setMyLocationEnabled(true);
        if(location==null){
        	LatLng position = new LatLng(0,0);
        	map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 10));
        }else{
        	LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
        	map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 18));
        	map.addMarker(new MarkerOptions().title("Location").position(position));
        }
        
        
        
    	
    	//if(location!=null){
    	//	
    	//}
    }
}
