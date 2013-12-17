package com.eyecall.push;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.WindowManager;
import android.widget.Toast;

import com.eyecall.eventbus.Event;
import com.eyecall.eventbus.EventBus;
import com.eyecall.protocol.ProtocolField;
import com.eyecall.protocol.ProtocolName;
import com.eyecall.volunteer.EventTag;
import com.eyecall.volunteer.MainActivity;
import com.eyecall.volunteer.RequestActivity;
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
			Bundle b = intent.getExtras();
			
			String rawName = b.getString(ProtocolField.NAME.getName());
			ProtocolName name = ProtocolName.lookup(rawName);
			switch(name){
			case NEW_REQUEST:
				Intent i = new Intent(this, RequestActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				i.putExtras(intent.getExtras());
				startActivity(i);
				break;
			case CANCEL_REQUEST:
				EventBus.getInstance().post(new Event(EventTag.REQUEST_CANCELLED));
				break;
			default:
				// Nothing
				break;
			}
			
			String lng  = b.getString(ProtocolField.LONGITUDE.getName());
			
			//wake up screen
//			WakeLock lock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "");
	//		lock.acquire();
			
			
		//	lock.release();
		}
		
		GcmBroadcastReceiver.completeWakefulIntent(intent);
		
	}

}
