package com.eyecall.volunteer;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.eyecall.eventbus.InputEventListener;

class LocationAdapter extends BaseAdapter {
	
	private static final Logger logger = LoggerFactory
			.getLogger(LocationAdapter.class);
	
	private List<Location> locations;
	private Context context;
	
	public LocationAdapter(Context context) {
		super();
		this.locations = new ArrayList<Location>();
		this.context = context;
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
		LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		ViewGroup v = (ViewGroup) inflater.inflate(R.layout.row_location,null);
		
		TextView locationText = (TextView) v.findViewById(R.id.location_header_preferred);
		TextView preferredText = (TextView) v.findViewById(R.id.row_text_preferred);
		ImageButton removeButton = (ImageButton) v.findViewById(R.id.row_button_remove);
		
		removeButton.setOnClickListener(new InputEventListener(EventTag.REMOVE_LOCATION, l));
		
		locationText.setText("long " + l.getLongitude() + " lat " + l.getLatitude());
		if(l.isPreferred()){
			preferredText.setText(R.string.row_text_preferred);
		}else{
			preferredText.setText(R.string.row_text_not_preferred);
		}
		
		return v;
	}

	/** 
	 * Adds a location to the view, but doesn't send it to the server
	 * @param location
	 */
	public void addLocation(Location location) {
		locations.add(location);
		this.notifyDataSetChanged();
	}

	
	
}
