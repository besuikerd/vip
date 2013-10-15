package com.eyecall.server;

import com.eyecall.connection.Message;
import com.eyecall.connection.OutQueue;
import com.eyecall.connection.ProtocolHandler;
import com.eyecall.connection.ServerState;
import com.eyecall.connection.State;


public class ServerProtocolHandler implements ProtocolHandler<ServerState> {
    private Request request;
    
    class QueryHandler {
    }

    @Override
    public State handleMessage(ServerState state, Message m, OutQueue<Message> queue) {
    	// TODO Auto-generated method stub
    	return null;
    }
}
