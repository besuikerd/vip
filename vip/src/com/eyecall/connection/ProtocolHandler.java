package com.eyecall.connection;

import java.util.Map;


public interface ProtocolHandler {
    public State handleMessage(State state, String string, Map<String, Object> map);
}
