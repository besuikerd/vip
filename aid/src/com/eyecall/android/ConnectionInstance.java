package com.eyecall.android;

import java.net.UnknownHostException;

import com.eyecall.connection.Connection;
import com.eyecall.volunteer.VolunteerProtocolHandler;
import com.eyecall.volunteer.VolunteerState;

public class ConnectionInstance {
	private static Connection instance;
	
	public static synchronized Connection getInstance(String host, int port) throws UnknownHostException{
		if(instance == null){
			instance = new Connection(host, port, new VolunteerProtocolHandler(), VolunteerState.INITIALISATION);
		}
		return instance;
	}
	
	public static synchronized void clear(){
		instance = null;	
	}
}
