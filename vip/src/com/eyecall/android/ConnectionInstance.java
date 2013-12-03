package com.eyecall.android;

import java.net.UnknownHostException;

import com.eyecall.connection.Connection;
import com.eyecall.vip.VIPProtocolHandler;
import com.eyecall.vip.VIPState;

public class ConnectionInstance {
	private static Connection instance;
	
	public static synchronized Connection getInstance(String host, int port) throws UnknownHostException{
		if(instance == null || instance.isClosed()){
			instance = new Connection(host, port, new VIPProtocolHandler(), VIPState.IDLE);
			instance.init(false);
		}
		return instance;
	}
	
	public static synchronized Connection getInstance(){
		return instance;
	}
	
	public static synchronized void clear(){
		instance = null;	
	}
}
