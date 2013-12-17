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

	public RequestTimerTask(Request request){
		this.request = request;
	}

	@Override
	public void run() {
		logger.debug("Request timeout... Running RequestTimerTask");
		if(!request.connected()){
			logger.debug("Request timeout... Finding new volunteers");
			// Reject pending
			request.rejectPendingVolunteers();
			
			// Find new group
			request.findNewVolunteers();
			
			if(request.getPendingVolunteers().size()>0){
				// Send to new group
				request.sendRequestToPendingVolunteers();
			
				// Reschedule
				new Timer().schedule(new RequestTimerTask(request), Constants.REQUEST_TIMEOUT);
				
				// And done :)
			}else{
				// Nobody found. Deny request
				logger.debug("Request timeout... No new volunteers found");
				request.sendRequestDenied();
			}
		}
	}
}
