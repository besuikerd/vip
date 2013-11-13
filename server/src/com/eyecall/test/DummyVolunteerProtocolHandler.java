package com.eyecall.test;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyecall.connection.Connection;
import com.eyecall.connection.Message;
import com.eyecall.connection.ProtocolHandler;
import com.eyecall.connection.State;
import com.eyecall.protocol.ProtocolName;

public class DummyVolunteerProtocolHandler implements
		ProtocolHandler<VolunteerState> {

	private static final Logger logger = LoggerFactory.getLogger(DummyVolunteerProtocolHandler.class);
	
	@Override
	public State messageSent(VolunteerState state, Message m) {
		switch(ProtocolName.lookup(m.getName())){
		case ACCEPT_REQUEST:
			return VolunteerState.WAITING_FOR_ACKNOWLEDGEMENT;
		}
		return state;
	}

	@Override
	public State messageReceived(VolunteerState state, Message m, Connection c) {
		ProtocolName name = ProtocolName.lookup(m.getName()); 
		switch(state){
		case WAITING_FOR_ACKNOWLEDGEMENT:
			switch(name){
			case ACKNOWLEDGE_HELP:
				logger.debug("help request acknowledged by server");
				return VolunteerState.HELPING;
			default:
				break;
			}
		case HELPING:
			switch(name){
			default:
				logger.debug("message received while helping: {}", m);
				return state;
			}
		default:
			break;
		}
		
		return null;
	}

	@Override
	public void onDisconnect(VolunteerState state) {
		// TODO Auto-generated method stub

	}

}
