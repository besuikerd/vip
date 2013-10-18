package com.eyecall.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	public State handleMessage(State state, Message m, OutQueue<Message> queue) {
		
		if(m.getName() == ProtocolHandler.ERROR){
			logger.warn("protocol error occurred: [{}]: {}", m.getParam("code", int.class), m.getParam("message"));
		} else{
			logger.warn("unknown message sent: {}", m.getName());
		}
		return state;
	}
	
}
