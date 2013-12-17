package com.eyecall.vip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;

import com.eyecall.android.ConnectionInstance;
import com.eyecall.connection.Message;
import com.eyecall.eventbus.Event;
import com.eyecall.eventbus.EventBus;
import com.eyecall.eventbus.EventListener;
import com.eyecall.eventbus.InputEventListener;
import com.eyecall.protocol.ProtocolName;

public class HelpActivity extends Activity implements EventListener{
	private static final Logger logger = LoggerFactory.getLogger(HelpActivity.class);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		((Button) findViewById(R.id.button_disconnect)).setOnClickListener(new InputEventListener(EventTag.DISCONNECT, null));
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		EventBus.getInstance().subscribe(this);
	}
	
	@Override
	public void onEvent(Event e) {
		switch(EventTag.lookup(e.getTag())){
		case DISCONNECT:
			if(ConnectionInstance.hasInstance()){
				ConnectionInstance.getExistingInstance().send(new Message(ProtocolName.DISCONNECT));
			}
			finish();
			break;
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		EventBus.getInstance().unsubscribe(this);
	}
}
