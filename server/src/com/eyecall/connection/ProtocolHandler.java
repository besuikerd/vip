package com.eyecall.connection;

import java.util.Map;

public interface ProtocolHandler {
	
	public static final String KEY_NAME = "name";
	
	public State handleMessage(State state, String name, Map<String, Object> params);
}
