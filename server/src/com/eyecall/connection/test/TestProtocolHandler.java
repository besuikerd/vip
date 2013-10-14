package com.eyecall.connection.test;

import java.util.Map;

import com.eyecall.connection.ProtocolHandler;
import com.eyecall.connection.State;

public class TestProtocolHandler implements ProtocolHandler{
	@Override
	public State handleMessage(State state, String name,
			Map<String, Object> params) {
		TestState s = (TestState) state;
		
		
		switch(s){
		case AWAITING_CONNECTION:
			
			if(params.containsKey("name")){
				
			} else{
				
				return TestState.AWAITING_CONNECTION;
			}
			
			
			break;
		case CONNECTED:
			break;
		case DISCONNECTED:
			break;
		}
		
		return null;
	}
}
