package com.eyecall.volunteer;

import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.WindowManager;
import android.widget.Button;

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
	private double latitude;
	private double longitude;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_request);
		//vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		//vibrator.vibrate(new long[]{200, 600}, 0);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
	            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
	            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		((Button) findViewById(R.id.button_accept)).setOnClickListener(new InputEventListener(EventTag.ACCEPT_REQUEST, null));
		((Button) findViewById(R.id.button_reject)).setOnClickListener(new InputEventListener(EventTag.REJECT_REQUEST, null));
		setupMap();
	}
	
	
	
	private void setupMap(){
		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_map)).getMap();
		if(map != null){
			//map.setMyLocationEnabled(true);
			Bundle extras = getIntent().getExtras();
			latitude = Double.parseDouble(extras.getString(ProtocolField.LATITUDE.getName()));
			longitude = Double.parseDouble(extras.getString(ProtocolField.LONGITUDE.getName()));
			logger.debug("showing coordinates: ({},{})", latitude, longitude);
			
			LatLng pos = new LatLng(latitude, longitude);
			marker = map.addMarker(new MarkerOptions().title("Vla").position(pos));
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 18f));
			
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(vibrator != null){
			vibrator.cancel();
		}
		EventBus.getInstance().unsubscribe(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		EventBus.getInstance().subscribe(this);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(vibrator != null){
			vibrator.cancel();
		}
	}

	@Override
	public void onEvent(Event e) {
		Connection c;
		Bundle extras;
		String requestId;
		String volunteerId;
		switch(EventTag.lookup(e.getTag())){
		case ACCEPT_REQUEST:
			logger.debug("accepting request...");
			c = ConnectionInstance.recreateConnection();
			
			extras = getIntent().getExtras();
			requestId = extras.getString(ProtocolField.REQUEST_ID.getName());
			volunteerId = PreferenceManager.getDefaultSharedPreferences(this).getString(ProtocolField.VOLUNTEER_ID.getName(), null);
			
			if(requestId != null && volunteerId != null){
				c.send(new Message(ProtocolName.ACCEPT_REQUEST)
				.add(ProtocolField.REQUEST_ID, requestId)
				.add(ProtocolField.VOLUNTEER_ID, volunteerId));
			}
			break;
			
		case REJECT_REQUEST:
			try {
				
				c = ConnectionInstance.getInstance(Constants.SERVER_URL, Constants.SERVER_PORT);
				
				extras = getIntent().getExtras();
				requestId = extras.getString(ProtocolField.REQUEST_ID.getName());
				volunteerId = PreferenceManager.getDefaultSharedPreferences(this).getString(ProtocolField.VOLUNTEER_ID.getName(), null);
				
				logger.debug("Sending request rejected requestid={}, volunteerid={}", requestId, volunteerId);
				
				if(requestId != null && volunteerId != null){
					c.send(new Message(ProtocolName.REJECT_REQUEST)
					.add(ProtocolField.REQUEST_ID, requestId)
					.add(ProtocolField.VOLUNTEER_ID, volunteerId));
				}
			} catch (UnknownHostException e1) {
			}
			
			finish();
			break;
			
		case REQUEST_ACKNOWLEDGED:
			//TODO start new call activity
			logger.debug("request acknowledged!");
			Intent i = new Intent(this, SupportActivity.class);
			Bundle b = new Bundle();
			b.putDouble(ProtocolField.LATITUDE.getName(), latitude);
			b.putDouble(ProtocolField.LONGITUDE.getName(), longitude);
			i.putExtras(b);
			startActivity(i);
			finish();
			break;
		//case REQUEST_DENIED:
			//
			
			//break;
		case REQUEST_CANCELLED:
			new AlertDialog.Builder(this)
			.setIcon(android.R.drawable.ic_dialog_info)
			.setTitle(R.string.request_denied_title)
			.setMessage(R.string.request_denied)
			.setPositiveButton(R.string.ok, null)
			.show();
			finish();
			break;
		default:
			break;
		}
	}
}
