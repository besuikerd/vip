package com.eyecall.volunteer;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.eyecall.connection.Connection;
import com.eyecall.eventbus.Event;
import com.eyecall.eventbus.EventBus;
import com.eyecall.eventbus.EventListener;
import com.eyecall.eventbus.InputEventListener;
import com.eyecall.protocol.ProtocolField;
import com.eyecall.push.PushRegistration;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class MainActivity extends Activity implements EventListener{

	private static final Logger logger = LoggerFactory.getLogger(MainActivity.class);
	
	private ProgressDialog dialog;

	private LocationAdapter locationAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		if(!checkInternet()){
			Toast.makeText(this, R.string.no_internet, Toast.LENGTH_LONG).show();
			this.finish();
			return;
		}
		
		// Check Google Play Services
		checkPlayServices();

		// Set view
		setContentView(R.layout.activity_locations);
		
		// Set listeners
		Button addLocation = (Button) findViewById(R.id.locations_button_add);
		addLocation.setOnClickListener(new InputEventListener(EventTag.ADD_LOCATION, null));
		Button refresh = (Button) findViewById(R.id.locations_button_refresh);
		refresh.setOnClickListener(new InputEventListener(EventTag.REFRESH_LOCATIONS, null));
		
		// Register for events
		EventBus.getInstance().subscribe(this);
		
		// Set adapter for listview
		ListView locationList = ((ListView) findViewById(R.id.locations_list));
		locationAdapter = new LocationAdapter(this);
		locationList.setAdapter(locationAdapter);
		
		//register / verify registration key
		new PushRegistration(this).register();
		

		/* DIALOGS */
		// TODO verplaatsen naar dialoggedeelte
		//new AlertDialog.Builder(this).setTitle("Heeft u kunnen helpen?").setView(getLayoutInflater().inflate(R.layout.dialog_success,null));
		//new AlertDialog.Builder(this).setTitle("Wilt u vaker oproepen in de buurt van deze locatie ontvangen?").setView(getLayoutInflater().inflate(R.layout.dialog_preferred,null));
	}
	
	private boolean checkInternet() {
		ConnectivityManager connectivityManager =  (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		
		
//		logger.debug("WIFI status: {}", connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI  ).getState());
//		logger.debug("Mobile status: {}", connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState());
		
		NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		
		return wifi != null && wifi.getState().equals(NetworkInfo.State.CONNECTED) || mobile != null && mobile.getState().equals(NetworkInfo.State.CONNECTED);
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		locationAdapter.notifyDataSetChanged();
	}

	/**
	 * Request the locations from the server and load them into the view
	 * This can only be used if the volunteer id is known!
	 */
	private void loadLocationList() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				dialog = new ProgressDialog(getActivity());
				dialog.setTitle(R.string.loading_locations);
				dialog.setCancelable(false);
				dialog.setMessage(getString(R.string.wait));
				dialog.show();
			}
		});
	    TimerTask timerRunnable = new TimerTask() {	
	        @Override
	        public void run() {
	        	runOnUiThread(new Runnable(){
	        		@Override
	        		public void run() {
	        			if(dialog!=null){
			        		dialog.dismiss();
			        		dialog=null;
//			        		Toast.makeText(context, R.string.loading_locations_timeout, Toast.LENGTH_LONG).show();
			        	}
	        		}
	        		
	        	});
	        }
	    };
	    
	    new Timer().schedule(timerRunnable, 5000);
		
		Connection c;
		try {
			c = new Connection(Constants.SERVER_URL,
					Constants.SERVER_PORT,
					new VolunteerProtocolHandler(),
					VolunteerState.IDLE);
			c.init(false);
			VolunteerProtocolHandler.requestLocations(c, Constants.VOLUNTEER_ID);
		} catch (UnknownHostException e) {
			logger.warn("Unable to connect: {}", e);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private void checkPlayServices(){
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
		if (resultCode != ConnectionResult.SUCCESS){
		} else{
			GooglePlayServicesUtil.getErrorDialog(resultCode, this, 1);
		}
	}
	
	
	@Override
	public void onEvent(Event e) {
		switch(EventTag.lookup(e.getTag())){
		case ADD_LOCATION:
			openLocationActivity(null);
			break;
		case REMOVE_LOCATION:
			new AlertDialog.Builder(this)
	        .setIcon(android.R.drawable.ic_dialog_alert)
	        .setTitle(R.string.remove_location_confirm_title)
	        .setMessage(R.string.remove_location_confirm)
	        .setPositiveButton(R.string.remove, new InputEventListener(EventTag.REMOVE_LOCATION_CONFIRM, e.getData()))
	        .setNegativeButton(R.string.cancel, null)
	        .show();
		case REMOVE_LOCATION_CONFIRM:
			Connection c;
			Location location = (Location) e.getData();
			if(location.getId()>0){
				try {
					c = new Connection(Constants.SERVER_URL,
							Constants.SERVER_PORT,
							new VolunteerProtocolHandler(),
							VolunteerState.IDLE);
					c.init(false);
					VolunteerProtocolHandler.removeLocation(c, Constants.VOLUNTEER_ID, location);
				} catch (UnknownHostException ex) {
					logger.warn("Unable to connect: {}", ex);
				}
				
				locationAdapter.removeLocation(location);
			 
				Toast.makeText(this, R.string.location_removed, Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(this, R.string.location_refresh_first, Toast.LENGTH_SHORT).show();
			}
			break;
		case EDIT_LOCATION:
			openLocationActivity((Location) e.getData());
			break;
		case REFRESH_LOCATIONS:
			loadLocationList();
			break;
		case LOCATION_ADDED:
			// Add location to list
			locationAdapter.addLocation((Location) e.getData());
			loadLocationList();
			break;
		case LOCATIONS_RECEIVED:
			if(dialog!=null) dialog.dismiss();
			dialog = null;
			
			logger.debug("setting up locations in ui..");
			
			@SuppressWarnings("unchecked")
			final List<Location> locations = (List<Location>) e.getData();
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					locationAdapter.clear();
					for(Location location : locations){
						locationAdapter.addLocation(location);
					}
				}
			});
			
			break;
		case ID_ACCEPTED:
			// Volunteer id is accepted -> Save it
			PreferenceManager.getDefaultSharedPreferences(this).edit().putString(ProtocolField.VOLUNTEER_ID.getName(), e.getData().toString()).commit();
			Constants.VOLUNTEER_ID = e.getData().toString();
			// Continue loading
			this.loadLocationList();
			
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					Toast.makeText(getApplicationContext(), "id registered!", Toast.LENGTH_LONG).show();
				}
			});
			break;
		case ID_REJECTED:
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getApplicationContext(), R.string.error_register_unable, Toast.LENGTH_LONG).show();
				}
			});
			
			break;
		case ID_INVALID:
			// Try to re-register
			logger.debug("It seems the volunteer id is invalid. Re-registering...");
			final Context context = this;
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					new PushRegistration(context).register();
				}
			});
			
		default:
			break;
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


	public Activity getActivity(){
		return this;
	}

}