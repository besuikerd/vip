package com.eyecall.volunteer;

import java.util.Map;

import com.eyecall.connection.Message;
import com.eyecall.connection.ProtocolHandler;
import com.eyecall.connection.State;
import com.eyecall.vip.VIPState;


public class VolunteerProtocolHandler implements ProtocolHandler<VolunteerState> {
	public State handleMessage(State s, String s2, Map<String, Object> o){
		switch(state){
		case IDLE:
		// TODO invullen
			
		case WAITING:
		//	logger.debug("message received from {} while connected: {}", name, m.toString());
			switch(m.getName()){
			case "request_denied":
				queue.add(new Message("request_denied"));
				return VIPState.IDLE;
			case "request_granted":
				queue.add(new Message("request_forwarded"));
				return VIPState.BEING_HELPED;	
			default:
				return null;
			}
		}
    }
}
