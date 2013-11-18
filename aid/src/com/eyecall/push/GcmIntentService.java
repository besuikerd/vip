package com.eyecall.push;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.eyecall.volunteer.MainActivity;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmIntentService extends IntentService{
	
	private static final Logger logger = LoggerFactory.getLogger(GcmIntentService.class);
	
	public GcmIntentService() {
		super("com.eyecall.push.GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		logger.debug("Gcm intentservice started");
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		String messageType = gcm.getMessageType(intent);
		if(messageType.equals(GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR)){
			logger.warn("Message type was send error");
		} else if(messageType.equals(GoogleCloudMessaging.MESSAGE_TYPE_DELETED)){
			logger.warn("Message type was deleted");
		} else if(messageType.equals(GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE)){
			logger.debug("Received: {}", extras.getString("hoi"));
			if(extras.containsKey("hoi")){
				Intent i = new Intent(this, MainActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(i);
				Toast.makeText(this, extras.getString("hoi"), Toast.LENGTH_LONG).show();
			}
		}
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

}
