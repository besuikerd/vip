package com.eyecall.volunteer;

import java.util.ArrayList;
import java.util.List;

import nl.besuikerd.imageloader.DefaultImageLoaderManager;
import nl.besuikerd.imageloader.ImageViewImageLoadingHandler;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.eyecall.eventbus.InputEventListener;

class LocationAdapter extends BaseAdapter {
	
	//private static final Logger logger = LoggerFactory
	//		.getLogger(LocationAdapter.class);
	
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
		// Inflate layout
		LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		ViewGroup v = (ViewGroup) inflater.inflate(R.layout.row_location,null);
		
		// Get views
		//TextView locationText = (TextView) v.findViewById(R.id.row_text_location);
		TextView preferredText = (TextView) v.findViewById(R.id.row_text_preferred);
		ImageButton removeButton = (ImageButton) v.findViewById(R.id.row_button_remove);
		ImageButton editButton = (ImageButton) v.findViewById(R.id.row_button_edit);
		
		// Set image
		ImageView map = (ImageView) v.findViewById(R.id.row_map);
		String url = "http://maps.googleapis.com/maps/api/staticmap?center=" + l.getLatitude() + "," + l.getLongitude() + "&zoom=10&size=150x150&markers=" + l.getLatitude() + "," + l.getLongitude() + "&sensor=false";
		new DefaultImageLoaderManager().postImage(new ImageViewImageLoadingHandler(context, map, url));
		
		// Set listeners
		removeButton.setOnClickListener(new InputEventListener(EventTag.REMOVE_LOCATION, l));
		editButton.setOnClickListener(	new InputEventListener(EventTag.EDIT_LOCATION, l));
		
		// Set text
		//locationText.setText("long " + l.getLongitude() + " lat " + l.getLatitude());
		if(l.isPreferred()){
			preferredText.setText(R.string.row_text_preferred);
		}else{
			preferredText.setText(R.string.row_text_not_preferred);
		}
		
		// Done
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

	public void removeLocation(Location location) {
		locations.remove(location);
		this.notifyDataSetChanged();
	}

	public void clear() {
		locations.clear();		
	}
	
	
	
	
	

	
	
}
