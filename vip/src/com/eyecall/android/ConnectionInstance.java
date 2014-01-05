package com.eyecall.android;

import java.io.IOException;
import java.net.UnknownHostException;

import com.eyecall.connection.Connection;
import com.eyecall.vip.Constants;
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
	
	public static synchronized Connection getInstance() throws UnknownHostException{
		return getInstance(Constants.SERVER_URL, Constants.SERVER_PORT);
	}
	
	public static synchronized Connection getExistingInstance(){
		return instance;
	}
	
	public static synchronized boolean hasInstance(){
		return instance != null;
	}
	
	public static Connection recreateConnection() throws IOException{
		clear();
		return getInstance();
	}
	
	public static synchronized void clear(){
		if(instance != null){
			try {
				instance.close();
			} catch (IOException e) {
			}
			instance = null;
		}
	}
}
