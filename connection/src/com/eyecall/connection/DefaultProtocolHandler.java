package com.eyecall.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyecall.protocol.ProtocolName;

/**
 * Default implementation of a ProtocolHandler. Can be seen as a default error
 * handler to prevent a lot of boilerplate code in each ProtocolHandler to
 * generate error codes for generic errors
 * @author Nicker
 *
 */
public class DefaultProtocolHandler implements ProtocolHandler<State>{

	private static final Logger logger = LoggerFactory.getLogger(DefaultProtocolHandler.class);
	
	@Override
	public State messageSent(State state, Message m) {
		return state;
	}
	
	@Override
	public State messageReceived(State state, Message m, Connection c) {
		
		if(m.getName() == ProtocolName.ERROR.getName()){
			logger.warn("protocol error occurred: [{}]: {}", m.getParam("code", int.class), m.getParam("message"));
		} else{
			logger.warn("unknown message received: {}", m.getName());
		}
		return state;
	}
	
	@Override
	public void onDisconnect(State state) {
	}
}
