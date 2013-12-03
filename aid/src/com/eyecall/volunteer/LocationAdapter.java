package com.eyecall.volunteer;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.eyecall.eventbus.InputEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

class LocationAdapter extends BaseAdapter {
	
	private static final Logger logger = LoggerFactory
			.getLogger(LocationAdapter.class);
	
	private List<Location> locations;
	//private Context context;

	private FragmentActivity activity;
	
	public LocationAdapter(FragmentActivity activity) {
		super();
		this.locations = new ArrayList<Location>();
		this.activity = activity;
	}

	@Override
	public int getCount() {
		return locations.size();
	}

	@Override
	public Object getItem(int position) {
		return locations.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Location l = locations.get(position);
		LayoutInflater inflater = (LayoutInflater) activity.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		ViewGroup v = (ViewGroup) inflater.inflate(R.layout.row_location,null);
		
		TextView locationText = (TextView) v.findViewById(R.id.row_text_location);
		TextView preferredText = (TextView) v.findViewById(R.id.row_text_preferred);
		ImageButton removeButton = (ImageButton) v.findViewById(R.id.row_button_remove);
		ImageButton editButton = (ImageButton) v.findViewById(R.id.row_button_edit);
		
		// Find the fragment for the map and set the tag
		
		//Fragment fragment = activity.getSupportFragmentManager().(Fragment) v.findViewById(R.id.row_map);
		//logger.debug("Setting tag of fragment: {}", l.getTag());
		//fragment.setTag(l.getTag());
		
		
		removeButton.setOnClickListener(new InputEventListener(EventTag.REMOVE_LOCATION, l));
		editButton.setOnClickListener(	new InputEventListener(EventTag.EDIT_LOCATION, l));
		
		locationText.setText("long " + l.getLongitude() + " lat " + l.getLatitude());
		if(l.isPreferred()){
			preferredText.setText(R.string.row_text_preferred);
		}else{
			preferredText.setText(R.string.row_text_not_preferred);
		}
		
		// Replace the fragment with a map
		//initMap(l, fragment);
		
		return v;
	}
	
	// Werkt niet
	/*private void initMap(Location location, Fragment fragment){
    	int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
    	if(status==ConnectionResult.SUCCESS){
    		SupportMapFragment mapFragment = (SupportMapFragment) fragment;
    		if(mapFragment==null){
    			logger.error("SupportMapFragment not found");
    			return;
    		}
    		GoogleMap map = mapFragment.getMap();
            if(map!=null){
            	// Setup map
            	map.setMyLocationEnabled(false);
                // Set position and marker
            	LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 18));
                map.addMarker(new MarkerOptions().title("Location").position(position));
            }else{
            	logger.warn("Map not loaded");
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
    }*/

	/** 
	 * Adds a location to the view, but doesn't send it to the server
	 * @param location
	 */
	public void addLocation(Location location) {
		locations.add(location);
		this.notifyDataSetChanged();
	}
	
	
	
	
	

	
	
}
