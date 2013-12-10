package com.eyecall.server;

import java.util.Timer;
import java.util.TimerTask;

import com.eyecall.database.Volunteer;

/**
 * This class is responsible for sending the request to a new group
 * of volunteers and for sending a cancel to the old group periodically.
 */
public class RequestTimerTask extends TimerTask {
	
	private Request request;

	public RequestTimerTask(Request request){
		this.request = request;
	}

	@Override
	public void run() {
		if(!request.connected()){
			// Reject pending
			request.rejectPendingVolunteers();
			
			// Find new group
			request.findNewVolunteers();
			
			// Send to new group
			request.sendRequestToPendingVolunteers();
		
			// Reschedule
			new Timer().schedule(this, Constants.REQUEST_TIMEOUT);
			
			// And done :)
		}
	}
}
