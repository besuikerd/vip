package com.eyecall.server;

import java.util.Map;

import com.eyecall.connection.State;


public class ServerProtocolHandler implements com.eyecall.connection.ProtocolHandler {
    private Request request;
    
    class QueryHandler {
    }

	@Override
	public State handleMessage(State state, String name,
			Map<String, Object> params) {
		// TODO Auto-generated method stub
		return null;
	}
}
