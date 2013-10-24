package com.eyecall.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyecall.connection.Message;
import com.eyecall.connection.OutQueue;
import com.eyecall.connection.ProtocolHandler;
import com.eyecall.connection.State;
import com.eyecall.database.Database;
import com.eyecall.database.Volunteer;
import com.eyecall.protocol.ProtocolField;
import com.eyecall.protocol.ProtocolName;


public class ServerProtocolHandler implements ProtocolHandler<ServerState> {
	
	private static final Logger logger = LoggerFactory.getLogger(ServerProtocolHandler.class);
    private Request request;

    
    @Override
    public State messageSent(ServerState state, Message m) {
    	return state;
    }
    
    @Override
    public State messageReceived(ServerState state, Message m, OutQueue<Message> queue) {
    	switch(state){
    	
    	case WAITING:
    		
    		switch(ProtocolName.lookup(m.getName())){
    		
    		case OBTAIN_KEY:
    			
    			
    			//create new volunteer with generated id
    			Volunteer v = new Volunteer();
    			
    			logger.debug("key {} assigned to volunteer", v.getId());
    			
    			//save volunteer in the database
    			Database.getInstance().insertTransaction(v);
    			
    			//add assign_key message to outqueue
    			queue.add(new Message(ProtocolName.ASSIGN_KEY).add(ProtocolField.KEY, v.getId()));
    			
    			//disconnect the connection
    			return ServerState.DISCONNECTED;
    		default:
    			break;
    		}
    	
    	
		default: 
			return null;
    	}
    }
}
