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
import com.eyecall.database.Volunteer;

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
		logger.debug("tunneling message to {}: {}", e, m);
		if(connections.containsKey(id)){
			Request r = connections.get(id);
			Connection c = null;
			switch(e){
			case VIP:
				c = r.getVipConnection();
				break;
			case VOLUNTEER:
				c = r.getVolunteerConnection();
				break;
			}
			
			
			c.send(m);
		}
	}
	
	@SuppressWarnings("unused")
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
	public Request setup(Connection c, String longitude, String latitude) {
		try{
			return setup(c, Double.valueOf(longitude), Double.valueOf(latitude));
		}catch(NumberFormatException e){
			return null;
		}
	}

	/**
	 * setup a request
	 * @param c Connection to VIP
	 * @return
	 */	
	public Request setup(Connection c, double longitude, double latitude) {
		String id = new BigInteger(128, new SecureRandom()).toString(16);
		logger.debug("setting up new request with id: {}", id);
		Request r = new Request(id, c, longitude, latitude);
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
				logger.debug("successfully attached volunteer!");
				r.setVolunteerConnection(c);
			} else{
				logger.debug("failed to attach volunteer");
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

	/**
	 * Checks if the given volunteer is currently helping a VIP or is in the 
	 * pendingVolunteers list of a request. 
	 * @param volunteer
	 * @return false if volunteer is currently helping or is in the pendingVolunteers list, true otherwise.
	 */
	public boolean isFree(Volunteer volunteer) {
		for(Request request : connections.values()){
			if(request.getPendingVolunteers().contains(volunteer) || (request.getVolunteerId()!=null && request.getVolunteerId().equals(volunteer.getId()))){
				return false;
			}
		}
		return true;
	}

	public Request getPendingRequest(String volunteerId) {
		Volunteer volunteer = new Volunteer(volunteerId);
		for(Request request : connections.values()){
			if(request.getPendingVolunteers().contains(volunteer)) return request;
		}
		return null;
	}
}
