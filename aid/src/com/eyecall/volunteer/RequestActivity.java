package com.eyecall.volunteer;

import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.eyecall.android.ConnectionInstance;
import com.eyecall.connection.Connection;
import com.eyecall.connection.Message;
import com.eyecall.eventbus.Event;
import com.eyecall.eventbus.EventBus;
import com.eyecall.eventbus.EventListener;
import com.eyecall.eventbus.InputEventListener;
import com.eyecall.protocol.ProtocolField;
import com.eyecall.protocol.ProtocolName;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class RequestActivity extends FragmentActivity implements EventListener{
	private static final Logger logger = LoggerFactory.getLogger(RequestActivity.class);
	
	private Vibrator vibrator;
	private GoogleMap map;
	private Marker marker;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_request);
		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(new long[]{200, 600}, 0);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
	            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
	            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		
		
		EventBus.getInstance().subscribe(this);
		((Button) findViewById(R.id.button_accept)).setOnClickListener(new InputEventListener(EventTag.ACCEPT_REQUEST, null));
		((Button) findViewById(R.id.button_reject)).setOnClickListener(new InputEventListener(EventTag.REJECT_REQUEST, null));
		setupMap();
	}
	
	private void setupMap(){
		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_map)).getMap();
		if(map != null){
			//map.setMyLocationEnabled(true);
			Bundle extras = getIntent().getExtras();
			double lat = Double.parseDouble(extras.getString(ProtocolField.LATITUDE.getName()));
			double lng = Double.parseDouble(extras.getString(ProtocolField.LONGITUDE.getName()));
			logger.debug("showing coordinates: ({},{})", lat, lng);
			
			LatLng pos = new LatLng(lat, lng);
			marker = map.addMarker(new MarkerOptions().title("Bla").position(pos));
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 18f));
			
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		vibrator.cancel();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//TODO enable vibration
		//vibrator.vibrate(new long[]{2000, 250}, 0);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		vibrator.cancel();
	}

	@Override
	public void onEvent(Event e) {
		switch(EventTag.lookup(e.getTag())){
		case ACCEPT_REQUEST:
			Connection c;
			try {
				c = ConnectionInstance.getInstance(Constants.SERVER_URL, Constants.SERVER_PORT);
				
				Bundle extras = getIntent().getExtras();
				String requestId = extras.getString(ProtocolField.REQUEST_ID.getName());
				String volunteerId = PreferenceManager.getDefaultSharedPreferences(this).getString(ProtocolField.VOLUNTEER_ID.getName(), null);
				
				if(requestId != null && volunteerId != null){
					c.send(new Message(ProtocolName.ACCEPT_REQUEST)
					.add(ProtocolField.REQUEST_ID, requestId)
					.add(ProtocolField.VOLUNTEER_ID, volunteerId));
				}
			} catch (UnknownHostException e1) {
			}
			
			finish();
			break;
			
		case REJECT_REQUEST:
			finish();
			break;
			
		case REQUEST_ACKNOWLEDGED:
			//TODO start new call activity
			logger.debug("request acknowledged!");
			Intent i = new Intent(this, SupportActivity.class);
			i.putExtras(getIntent().getExtras());
			startActivity(i);
			break;
		case REQUEST_DENIED:
			//TODO show dialog that request was already fulfilled
			finish();
			break;
			
		default:
			break;
		}
	}
}
