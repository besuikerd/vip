package com.eyecall.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.eyecall.connection.Connection;
import com.eyecall.database.Volunteer;


public class Request {
	private String id;
	private Connection vipConnection;
	private Connection volunteerConnection;
	private String volunteerId;
	private List<Volunteer> pendingVolunteers;
	private List<Volunteer> rejectedVolunteers;
	
	public Request(String id, Connection vipConnection) {
		this.id = id;
		this.vipConnection = vipConnection;
		this.pendingVolunteers = new ArrayList<Volunteer>();
		this.rejectedVolunteers = new ArrayList<Volunteer>();
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
	
	public void addPendingVolunteers(Collection<Volunteer> volunteers){
		this.pendingVolunteers.addAll(volunteers);
	}
	
	public void rejectPendingVolunteers(){
		this.rejectedVolunteers.addAll(this.pendingVolunteers);
		this.pendingVolunteers.clear();
	}
	
	public List<Volunteer> getPendingVolunteers() {
		return pendingVolunteers;
	}
	
	public List<Volunteer> getRejectedVolunteers() {
		return rejectedVolunteers;
	}
	
	/**
	 * check if both ends are set up
	 * @return
	 */
	public boolean connected(){
		return vipConnection != null && volunteerConnection != null;
	}
}
