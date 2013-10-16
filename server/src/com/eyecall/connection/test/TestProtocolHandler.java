package com.eyecall.connection.test;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyecall.connection.Message;
import com.eyecall.connection.OutQueue;
import com.eyecall.connection.ProtocolHandler;
import com.eyecall.connection.State;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestProtocolHandler implements ProtocolHandler<TestState>{
	private static final Logger logger = LoggerFactory.getLogger(TestProtocolHandler.class);
	private String name;
	
	@Override
	public State handleMessage(TestState state, Message m, OutQueue<Message> queue) {
		
		switch(state){
		case AWAITING_CONNECTION:
			logger.debug("message received while awaiting connection: {}", m);
			
			if(m.getName().equals("hello")){
				if(m.hasParam("hello")){
					this.name = m.getParam("hello").toString();
					queue.add(new Message("hello").add("hello", "otherside"));
					return TestState.CONNECTED;
				}
			}
		case CONNECTED:
			logger.debug("message received from {} while connected: {}", name, m.toString());
			if(m.hasParam("close") && m.getParam("close", Boolean.class)){
				
				return TestState.DISCONNECTED;
			}
			return TestState.CONNECTED;
			
		case DISCONNECTED:
			logger.debug("message received after disconnect: {}", m.toString());
			return TestState.DISCONNECTED;
		}
		
		return null;
	}
}
