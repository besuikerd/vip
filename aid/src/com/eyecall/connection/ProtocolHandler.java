package com.eyecall.connection;

import java.util.Map;


public interface ProtocolHandler {
    public State handleMessage(State s, String s2, Map<String, Object> o);
}
