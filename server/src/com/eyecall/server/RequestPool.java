package com.eyecall.server;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyecall.connection.Connection;
import com.eyecall.connection.Message;

public class RequestPool {
	private static final Logger logger = LoggerFactory.getLogger(RequestPool.class);
	
	private static RequestPool instance;
	
	private Map<String, Request> connections;
	
	private RequestPool(){
		this.connections = new ConcurrentHashMap<String, Request>();
	}
	
	public static RequestPool getInstance(){
		if(instance == null){
			instance = new RequestPool();
		}
		return instance;
	}
	
	public void tunnel(String id, Entity e, Message m){
		logger.debug("tunneling message from {}: {}", e, m);
		if(connections.containsKey(id)){
			Request r = connections.get(id);
			Connection c = null;
			switch(e){
			case VIP:
				c = r.getVipConnection();
			case VOLUNTEER:
				c = r.getVolunteerConnection();
			}
			c.send(m);
		}
	}
	
	public void tunnelUdp(String id, Entity e, Message m) throws IOException{
		if(true) throw new UnsupportedOperationException("not yet implemented");
		if(connections.containsKey(id)){
			Request r = connections.get(id);
			Connection c = null;
			switch(e){
			case VIP:
				c = r.getVipConnection();
			case VOLUNTEER:
				c = r.getVolunteerConnection();
			}
			//c.sendUDP(m);
		}
	}
	
	/**
	 * setup a request
	 * @param c Connection to VIP
	 * @return
	 */
	public Request setup(Connection c){
		String id = new BigInteger(128, new SecureRandom()).toString(16);
		logger.debug("setting up new request with id: {}", id);
		Request r = new Request(id, c);
		connections.put(id, r);
		return r;
	}
	
	/**
	 * attach a volunteer to a request
	 * @param id
	 * @param c
	 */
	public Request attach(String id, Connection c){
		logger.debug("attaching request with id {}...", id);
		Request r = null;
		if(connections.containsKey(id)){
			logger.debug("request exists: {}", id);
			r = connections.get(id);
			if(!r.connected()){
				r.setVolunteerConnection(c);
			}
		} else{
			logger.warn("no request exists with id {}", id);
		}
		return r;
	}
	
	public boolean isConnected(String id){
		boolean isConnected = false;
		if(!connections.containsKey(id)){
			isConnected = connections.get(id).connected();
		}
		logger.debug("{} is connected: {}", id, isConnected);
		return isConnected;
	}
	
	public boolean exists(String id){
		logger.debug("{} exists: {}", id, connections.containsKey(id));
		return connections.containsKey(id);
	}
	
	/**
	 * remove a request
	 * @param id
	 */
	public void remove(String id){
		connections.remove(id);
	}
}
