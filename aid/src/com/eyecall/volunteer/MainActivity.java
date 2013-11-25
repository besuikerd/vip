package com.eyecall.volunteer;

import java.net.UnknownHostException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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
	
	public static final String TAG = "Eyecall Volunteer";
	
	private ProgressDialog dialog;

	private LocationAdapter locationAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		// Check Google Play Services
		checkPlayServices();

		// Set view
		setContentView(R.layout.activity_locations);
		
		// Set listeners
		Button addLocation = (Button) findViewById(R.id.locations_button_add);
		addLocation.setOnClickListener(new InputEventListener(EventTag.ADD_LOCATION, null));
		
		// Register for events
		EventBus.getInstance().subscribe(this);
		
		// Set adapter for listview
		ListView locationList = ((ListView) findViewById(R.id.locations_list));
		locationAdapter = new LocationAdapter(this);
		locationList.setAdapter(locationAdapter);
		
		//check if application has yet been registered
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		if (!preferences.contains(ProtocolField.VOLUNTEER_ID.getName())) {
			// No id saved -> Get id
			new PushRegistration(this).register();
			// Locations in the list are loaded when registration is complete (id is needed)
		}else{
			// Id found -> Set id and continue
			Constants.VOLUNTEER_ID = preferences.getString(ProtocolField.VOLUNTEER_ID.getName(), "");
			this.loadLocationList();
		}

		/* DIALOGS */
		// TODO verplaatsen naar dialoggedeelte
		//new AlertDialog.Builder(this).setTitle("Heeft u kunnen helpen?").setView(getLayoutInflater().inflate(R.layout.dialog_success,null));
		//new AlertDialog.Builder(this).setTitle("Wilt u vaker oproepen in de buurt van deze locatie ontvangen?").setView(getLayoutInflater().inflate(R.layout.dialog_preferred,null));
	}

	/**
	 * Request the locations from the server and load them into the view
	 * This can only be used if the volunteer id is known!
	 */
	private void loadLocationList() {
		this.dialog = new ProgressDialog(this);
		dialog.setTitle(R.string.loading_locations);
		dialog.setCancelable(false);
		dialog.setMessage(getString(R.string.wait));
		dialog.show();
		
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
		case LOCATIONS_RECEIVED:
			dialog.dismiss();
			List<Location> locations = (List<Location>) e.getData();
			for(Location location : locations){
				locationAdapter.addLocation(location);
			}
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


	

}