package com.eyecall.test;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyecall.connection.Connection;
import com.eyecall.connection.Message;
import com.eyecall.connection.ProtocolHandler;
import com.eyecall.connection.State;
import com.eyecall.protocol.ProtocolField;
import com.eyecall.protocol.ProtocolName;

public class DummyVIPProtocolHandler implements ProtocolHandler<VIPState> {

	private static final Logger logger = LoggerFactory.getLogger(DummyVIPProtocolHandler.class);
	
	@Override
	public State messageSent(VIPState state, Message m) {
		switch(ProtocolName.lookup(m.getName())){
		case REQUEST_HELP:
			logger.debug("request sent. Waiting for response..");
			return VIPState.WAITING;
		default:
			break;
		}
		return null;
	}

	@Override
	public State messageReceived(VIPState state, Message m, Connection c) {
		switch(ProtocolName.lookup(m.getName())){
		case REQUEST_GRANTED:
			logger.debug("request granted, changing state to \"BEING_HELPED\"");
			
			final Connection fc = c;
			
			new Timer().scheduleAtFixedRate(new TimerTask() {
				
				
				@Override
				public void run() {
					fc.send(new Message(ProtocolName.UPDATE_LOCATION).add(ProtocolField.LONGITUDE, 0f).add(ProtocolField.LATITUDE, 0f));
				}
			}, 0, 1000);
			
			return VIPState.BEING_HELPED;
		default:
			break;
		}
		return null;
	}

	@Override
	public void onDisconnect(VIPState state) {
		// TODO Auto-generated method stub

	}

}
