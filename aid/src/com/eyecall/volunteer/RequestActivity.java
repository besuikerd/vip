package com.eyecall.volunteer;

import java.io.IOException;
import java.net.UnknownHostException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
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

public class RequestActivity extends Activity implements EventListener{
	private Vibrator vibrator;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_request);
		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(new long[]{200, 600}, 0);
		
		EventBus.getInstance().subscribe(this);
		((Button) findViewById(R.id.button_accept)).setOnClickListener(new InputEventListener(EventTag.ACCEPT_REQUEST, null));
		((Button) findViewById(R.id.button_reject)).setOnClickListener(new InputEventListener(EventTag.REJECT_REQUEST, null));
		
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		vibrator.cancel();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		vibrator.vibrate(new long[]{200, 600}, 0);
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
			Toast.makeText(this, "Request was succesfully initialized!", Toast.LENGTH_LONG).show();
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
