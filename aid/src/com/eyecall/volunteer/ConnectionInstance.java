package com.eyecall.volunteer;

import java.io.IOException;
import java.net.UnknownHostException;

import com.eyecall.connection.Connection;


public class ConnectionInstance {
	private static Connection instance;
	
	public static synchronized Connection getInstance(String host, int port) throws UnknownHostException{
		if(instance == null || instance.isClosed()){
			instance = new Connection(host, port, new VolunteerProtocolHandler(), VolunteerState.IDLE);
			instance.init(false);
		}
		return instance;
	}
	
	public static synchronized Connection getInstance() throws UnknownHostException{
		return getInstance(Constants.SERVER_URL, Constants.SERVER_PORT);
	}
	
	public static synchronized Connection getExistingInstance(){
		return instance;
	}
	
	public static synchronized boolean hasInstance(){
		return instance != null;
	}
	
	public static synchronized Connection recreateConnection(){
		try {
			if(instance != null){
				instance.close();
			}
			instance = null;
			return getInstance();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static synchronized void clear(){
		instance = null;	
	}
}
