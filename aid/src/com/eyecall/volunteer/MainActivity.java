package com.eyecall.volunteer;

import java.util.List;

import com.eyecall.event.ClickEvent;
import com.eyecall.eventbus.Event;
import com.eyecall.eventbus.EventBus;
import com.eyecall.eventbus.EventListener;
import com.eyecall.eventbus.InputEventListener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity implements EventListener{
	
	public static final String TAG = "Eyecall Volunteer";
	private PreferencesManager preferencesManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		preferencesManager = new PreferencesManager(this.getBaseContext());
		
		// Set view
		setContentView(R.layout.activity_locations);
		
		// Set listeners
		Button addLocation = (Button) findViewById(R.id.locations_button_add);
		addLocation.setOnClickListener(new InputEventListener(EventTag.ADD_LOCATION, null));
		
		EventBus.getInstance().subscribe(this);
		
		// Set adapter for listview
		ListView locationList = ((ListView) findViewById(R.id.locations_list));
		List<Location> locations = preferencesManager.getLocations();
		locationList.setAdapter(new LocationAdapter(locations));
		
		/* DIALOGS */
		// TODO verplaatsen naar dialoggedeelte
		//new AlertDialog.Builder(this).setTitle("Heeft u kunnen helpen?").setView(getLayoutInflater().inflate(R.layout.dialog_success,null));
		//new AlertDialog.Builder(this).setTitle("Wilt u vaker oproepen in de buurt van deze locatie ontvangen?").setView(getLayoutInflater().inflate(R.layout.dialog_preferred,null));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	@Override
	public void onEvent(Event e) {
		if(e instanceof ClickEvent){
			ClickEvent event = (ClickEvent) e;
			if(event.getTag().equals(EventTag.ADD_LOCATION.getName())){
				openLocationActivity(null);
			}
		}
		
	}

	/**
	 * Opens the LocationActivity and passes the location
	 * If a location is passed, this location is edited
	 * If null is passes, a new location is added
	 * @param location
	 */
	private void openLocationActivity(Location location){
		Intent intent = new Intent(this, LocationActivity.class);
		if(location!=null) intent.putExtra("location", location);
		this.startActivity(intent);
	}


	class LocationAdapter extends BaseAdapter {
		private List<Location> locations;
		
		public LocationAdapter(List<Location> locations) {
			super();
			this.locations = locations;
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
			ViewGroup v = (ViewGroup) getLayoutInflater().inflate(R.layout.row_location,null);
			
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
		
	}

}