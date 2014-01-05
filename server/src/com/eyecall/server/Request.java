package com.eyecall.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyecall.connection.Connection;
import com.eyecall.database.Database;
import com.eyecall.database.Volunteer;


public class Request {
	private static final Logger logger = LoggerFactory.getLogger(Request.class);
	private String id;
	private Connection vipConnection;
	private Connection volunteerConnection;
	private String volunteerId;
	private List<Volunteer> pendingVolunteers;
	private List<Volunteer> rejectedVolunteers;
	private Double longitude;
	private Double latitude;
	private Timer timer;
	
	/**
	 * check if this request if still valid
	 */
	private boolean valid;
	
	public Request(String id, Connection vipConnection, Double longitude, Double latitude) {
		this.id = id;
		this.vipConnection = vipConnection;
		this.longitude = longitude;
		this.latitude = latitude;
		this.pendingVolunteers = new ArrayList<Volunteer>();
		this.rejectedVolunteers = new ArrayList<Volunteer>();
		this.valid = true;
	}
	
	public void attach(Connection c){
		this.volunteerConnection = c;
	}
	
	public void invalidate(){
		valid = false;
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
		logger.debug(id + " Adding new pending volunteers. count: {}", volunteers.size());
		this.pendingVolunteers.addAll(volunteers);
	}
	
	public void removePendingVolunteer(String id) {
		logger.debug(id + " Removing volunteer from pendingVolunteers {}", id);
		Volunteer removing = null;
		for(Volunteer volunteer : pendingVolunteers){
			if(volunteer.getId().equals(id)){
				// Don't remove here -> ConcurrentModification
				removing = volunteer;
			}
		}
		
		if(removing!=null) pendingVolunteers.remove(removing);
	}

	public void rejectPendingVolunteers(){
		logger.debug(id + " Rejecting all pending volunteers...");
		this.rejectedVolunteers.addAll(this.pendingVolunteers);
		this.pendingVolunteers.clear();
	}
	
	public void rejectPendingVolunteer(String id) {
		logger.debug(this.id + " Rejecting volunteer {}", id);
		Volunteer removing = null;
		for(Volunteer volunteer : pendingVolunteers){
			if(volunteer.getId().equals(id)){
				// Don't remove here -> ConcurrentModification
				removing = volunteer;
			}
		}
		
		if(removing!=null){
			pendingVolunteers.remove(removing);
			rejectedVolunteers.add(removing);
		}
		
		if(pendingVolunteers.size()==0){
			logger.debug(this.id + " Pending volunteers is now 0. Stopping timer and findind new volunteers");
			timer.cancel();
			// Find new ones, send request etc bla bla bla.
			start();
		}
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
		timer = new Timer();
		new RequestTimerTask(this, timer);
	}
	
	public void findNewVolunteers() {
		Session session = Database.getInstance().startSession();
		Query query = session.createSQLQuery(Constants.VOLUNTEER_QUERY);
		query.setDouble("latitude", latitude);
		query.setDouble("longitude", longitude);
		List<?> result = query.list();
		List<Volunteer> potentialVolunteers = new ArrayList<Volunteer>();
		int count = 0;
		for(Object id : result){
			if(count<Constants.REQUEST_GROUP_SIZE){
				Volunteer volunteer = new Volunteer();
				volunteer.setId(id.toString());
				if(!rejectedVolunteers.contains(volunteer) && RequestPool.getInstance().isFree(volunteer)){
					potentialVolunteers.add(volunteer);
					count++;
				}				
			}
		}
		session.close();
		
		// List<Volunteer> potentialVolunteers = Database.getInstance().queryForList(Constants.VOLUNTEER_QUERY, Volunteer.class);
		logger.debug(id + " potential volunteers: {}", potentialVolunteers);
		this.addPendingVolunteers(potentialVolunteers);
	}

	public void sendRequestToPendingVolunteers(){
		logger.debug(id + " Sending request to pending volunteers...");
		for(Volunteer volunteer : this.getPendingVolunteers()){
			ServerProtocolHandler.sendNewRequest(volunteer, this);
		}
	}
	
	public void sendCancelToPendingVolunteers(){
		logger.debug(id + " Sending cancel request to pending volunteers...");
		for(Volunteer volunteer : this.getPendingVolunteers()){
			ServerProtocolHandler.sendRequestCancelled(volunteer, this);
		}
	}

	public void sendRequestDenied() {
		logger.debug(id + " Sending request denied to VIP...");
		ServerProtocolHandler.sendRequestDenied(vipConnection, this);
	}

	public Double getLatitude() {
		return latitude;
	}

	public Double getLongitude() {
		return longitude;
	}
	
	public boolean isValid() {
		return valid;
	}

	public void close() {
		RequestPool.getInstance().remove(this.id);
	}
}
