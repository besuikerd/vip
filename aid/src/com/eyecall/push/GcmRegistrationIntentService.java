package com.eyecall.push;

import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.content.Intent;

import com.eyecall.connection.Connection;
import com.eyecall.volunteer.Constants;
import com.eyecall.volunteer.VolunteerProtocolHandler;
import com.eyecall.volunteer.VolunteerState;
import com.google.android.gcm.GCMBaseIntentService;

public class GcmRegistrationIntentService extends GCMBaseIntentService{
	
private static final Logger logger = LoggerFactory.getLogger(GcmIntentService.class);
	
	public GcmRegistrationIntentService() {
		super("com.eyecall.push.GcmRegistrationIntentService");
	}

	@Override
	protected void onError(Context arg0, String arg1) {
		logger.error("onError: " + arg1);
	}

	@Override
	protected void onMessage(Context arg0, Intent arg1) {
		logger.debug("onMessage");
	}

	@Override
	protected void onRegistered(Context context, String key) {
		logger.debug("GCM key obtained: {}", key);
		// Send key to server
		Connection c;
		try {
			c = new Connection(Constants.SERVER_URL,
					Constants.SERVER_PORT,
					new VolunteerProtocolHandler(),
					VolunteerState.INITIALISATION);
			c.init(false);
			VolunteerProtocolHandler.sendKeyToServer(c, key);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
		logger.debug("onUnregistered: " + arg1);		
	}

}
