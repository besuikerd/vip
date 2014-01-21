package com.eyecall.server;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyecall.database.Volunteer;

/**
 * This class is responsible for sending the request to a new group
 * of volunteers and for sending a cancel to the old group periodically.
 */
public class RequestTimerTask extends TimerTask {
	private static final Logger logger = LoggerFactory.getLogger(RequestTimerTask.class);
    
	private Request request;

	private Timer timer;
	
	private int counter = Constants.REQUEST_COUNT;

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
			for(Volunteer v : request.getPendingVolunteers()){
				ServerProtocolHandler.sendRequestCancelled(v, request);
			}
			// Reject pending
			request.rejectPendingVolunteers();
			
			// Find new group
			request.findNewVolunteers();
			
			if(request.getPendingVolunteers().size()>0){
				// Send to new group
				request.sendRequestToPendingVolunteers();
			
				RequestTimerTask task = new RequestTimerTask(request, timer);
				task.counter--;
				// Reschedule
				if(task.counter > 0){
					timer.schedule(task, Constants.REQUEST_TIMEOUT);
				}
				
				// And done :)
			}
			// If size==0, request is already invalidated and closed by findNewVolunteers()
		}
	}
}
