package com.eyecall.connection.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyecall.connection.Connection;
import com.eyecall.connection.Message;
import com.eyecall.connection.ProtocolHandler;
import com.eyecall.connection.State;

public class TestProtocolHandler implements ProtocolHandler<TestState>{
	private static final Logger logger = LoggerFactory.getLogger(TestProtocolHandler.class);
	private String name;
	
	@Override
	public State messageSent(TestState state, Message m) {
		return state;
	}
	
	@Override
	public State messageReceived(TestState state, Message m, Connection c) {
		
		switch(state){
		case AWAITING_CONNECTION:
			logger.debug("message received while awaiting connection: {}", m);
			
			if(m.getName().equals("hello")){
				if(m.hasParam("hello")){
					this.name = m.getParam("hello").toString();
					c.send(new Message("hello").add("hello", "otherside"));
					return TestState.CONNECTED;
				}
			}
		case CONNECTED:
			
			if(m.getName().equals("bytearray")){
				logger.debug("bytearray received: {}", m.getParam("data"));
			} else	if(m.hasParam("close") && m.getParam("close", Boolean.class)){
				return TestState.DISCONNECTED;
			} else{
				logger.debug("message received from {} while connected: {}", name, m.toString());
			}
			return TestState.CONNECTED;
			
		case DISCONNECTED:
			logger.debug("message received after disconnect: {}", m.toString());			
			return TestState.DISCONNECTED;
		}
		
		return null;
	}
	@Override
	public void onDisconnect(TestState state) {
		// TODO Auto-generated method stub
		
	}
}
