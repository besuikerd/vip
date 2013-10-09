package com.eyecall.connection;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Connection implements Runnable{
	private Socket s;
	private ProtocolHandler handler;
	private State state;
	
	
	@Override
	public void run() {
		ObjectMapper mapper = new ObjectMapper();
		
		while(!s.isClosed()){
			try {
				Map<String, Object> m = mapper.readValue(s.getInputStream(), new TypeReference<Map<String, Object>>() {});
				
				if(!m.containsKey(ProtocolHandler.KEY_NAME)){
					//TODO send error message (name_unknown)
				} else{
					
					//handle message and change state to return value
					state = handler.handleMessage(state, m.get(ProtocolHandler.KEY_NAME).toString(), m);
				}
				
			} catch (JsonParseException e) {
				//TODO send error message
			} catch (IOException e) {
				
				//close socket
				try {
					s.close();
				} catch (IOException e1) {
				}
			}
		}
	}
	
	
	public void disconnect() throws IOException{
		s.close();
	}
}
