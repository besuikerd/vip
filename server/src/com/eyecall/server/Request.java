package com.eyecall.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyecall.connection.Connection;
import com.eyecall.database.Database;
import com.eyecall.database.Volunteer;
import com.eyecall.protocol.ProtocolField;


public class Request {
	private static final Logger logger = LoggerFactory.getLogger(Request.class);
	private String id;
	private Connection vipConnection;
	private Connection volunteerConnection;
	private String volunteerId;
	private List<Volunteer> pendingVolunteers;
	private List<Volunteer> rejectedVolunteers;
	private String longitude;
	private String latitude;
	
	public Request(String id, Connection vipConnection, String longitude, String latitude) {
		this.id = id;
		this.vipConnection = vipConnection;
		this.longitude = longitude;
		this.latitude = latitude;
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

	/**
	 * Request will be handled: volunteers are searched for, messages are sent, timeout is set etc
	 */
	public void start() {
		// Step 1 : Find volunteers
		findNewVolunteers();
		
		// Step 2 : Send request
		sendRequestToPendingVolunteers();
		
		// Step 3 : Loop untill no volunteers available or request accepted
		TimerTask t = new RequestTimerTask(this);
		new Timer().schedule(t, Constants.REQUEST_TIMEOUT);
		
	}
	
	public void findNewVolunteers() {
		List<Volunteer> potentialVolunteers = Database.getInstance().queryForList(Constants.VOLUNTEER_QUERY, Volunteer.class);
		logger.debug("potential volunteers: {}", potentialVolunteers);
		this.addPendingVolunteers(potentialVolunteers);
	}

	public void sendRequestToPendingVolunteers(){
		for(Volunteer volunteer : this.getPendingVolunteers()){
			ServerProtocolHandler.sendNewRequest(volunteer, this);
		}
	}
	
	public void sendCancelToPendingVolunteers(){
		for(Volunteer volunteer : this.getPendingVolunteers()){
			ServerProtocolHandler.sendRequestCancelled(volunteer, this);
		}
	}

	public String getLatitude() {
		return latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void close() {
		RequestPool.getInstance().remove(this.id);
	}
}
