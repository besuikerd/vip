package com.eyecall.server;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for sending the request to a new group
 * of volunteers and for sending a cancel to the old group periodically.
 */
public class RequestTimerTask extends TimerTask {
	private static final Logger logger = LoggerFactory.getLogger(RequestTimerTask.class);
    
	private Request request;

	private Timer timer;

	public RequestTimerTask(Request request, Timer timer){
		this.request = request;
		this.timer = timer;
		logger.debug("Starting RequestTimerTask... (timeout={})", Constants.REQUEST_TIMEOUT);
		timer.schedule(this, Constants.REQUEST_TIMEOUT);
	}

	@Override
	public void run() {
		logger.debug("Request timeout... Running RequestTimerTask");
		if(!request.connected() && request.isValid()){
			logger.debug("Request timeout... Finding new volunteers");
			// Reject pending
			request.rejectPendingVolunteers();
			
			// Find new group
			request.findNewVolunteers();
			
			if(request.getPendingVolunteers().size()>0){
				// Send to new group
				request.sendRequestToPendingVolunteers();
			
				// Reschedule
				timer.schedule(new RequestTimerTask(request, timer), Constants.REQUEST_TIMEOUT);
				
				// And done :)
			}else{
				// Nobody found. Deny request
				logger.debug("Request timeout... No new volunteers found");
				request.sendRequestDenied();
				request.invalidate();
			}
		}
	}
}
