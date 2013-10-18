package com.eyecall.aid;

import java.net.Socket;
import java.util.List;

import com.eyecall.connection.Connection;
import com.eyecall.connection.Message;
import com.eyecall.volunteer.VolunteerProtocolHandler;
import com.eyecall.volunteer.VolunteerState;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class LocationsActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_locations);
		Connection c = new Connection(new Socket("localhost",1000),new VolunteerProtocolHandler(),VolunteerState.IDLE);
		// TODO volunteer id toevoegen om locations van die persoon op te vragen
		c.send(new Message("locations").add());
		// TODO locations toevoegen aan de LocationAdapter
		((ListView) findViewById(R.id.list_locations)).setAdapter(new LocationAdapter ());
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
			((TextView) v.findViewById(R.id.text_location)).setText(l.getName());
			
			return v;
		}
		
	}
}
