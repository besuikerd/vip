package com.eyecall.server;

import com.eyecall.connection.Connection;


public class Request {
	private String id;
	private Connection vipConnection;
	private Connection volunteerConnection;
	private String volunteerId;
	
	public Request(String id, Connection vipConnection) {
		this.id = id;
		this.vipConnection = vipConnection;
	}
	
	public void attach(Connection c){
		this.volunteerConnection = c;
	}
	
	public void setVolunteerConnection(Connection volunteerConnection) {
		this.volunteerConnection = volunteerConnection;
	}
	
	public Connection getVipConnection() {
		return vipConnection;
	}
	
	public Connection getVolunteerConnection() {
		return volunteerConnection;
	}
	
	public void setVolunteerId(String volunteerId) {
		this.volunteerId = volunteerId;
	}
	
	public String getVolunteerId() {
		return volunteerId;
	}
	
	public String getId() {
		return id;
	}
	
	/**
	 * check if both ends are set up
	 * @return
	 */
	public boolean connected(){
		return vipConnection != null && volunteerConnection != null;
	}
}
