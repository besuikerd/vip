package com.eyecall.volunteer;

import android.util.Log;

import com.eyecall.connection.Message;
import com.eyecall.connection.OutQueue;
import com.eyecall.connection.ProtocolHandler;
import com.eyecall.connection.State;
import com.eyecall.protocol.ProtocolName;

public class VolunteerProtocolHandler implements ProtocolHandler<VolunteerState> {
	
	public State messageSent(VolunteerState state, Message m) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public State messageReceived(VolunteerState state, Message m, OutQueue queue) {
		Log.d(MainActivity.TAG, "Message received: '" + m.getName() + "' State:" + state.toString());
		ProtocolName messageName = ProtocolName.lookup(m.getName());
		switch (state){
		case INITIALISATION:
			//TODO iets met log
			
		case IDLE:
			switch(messageName){
			case NEW_REQUEST:
				//if(m.hasParams("request_id","longitude", "latitude")){
					//TODO pop-up keuze menu
				return VolunteerState.SHOWING_NOTIFICATION;
				//}	
			default:
				return null;
			}
			
		case WAITING_FOR_KEY:
			switch(messageName){
			case ASSIGN_KEY:
				if(m.hasParam("key")){
					//TODO shared preferences in context maken
					//Editor edit = sp.edit();
					//edit.putString("Volunteer key", (String)m.getParam("key"));
					//edit.commit();
					return VolunteerState.IDLE;
				}
			default:
				return null;
			}
		case HELPING:
			switch(messageName){
			case OTHER_DISCONNECTED:
				//TODO pop-up in scherm weergeven
				
				return VolunteerState.IDLE;
			case UPDATE_LOCATION:
				//if(m.hasParams("longitude", "latitude")){
					return VolunteerState.HELPING;
				//}
			case MEDIA_DATA:
				if(m.hasParam("data")){
				//TODO data op scherm tonen	
				return VolunteerState.HELPING;
				}
			default:
				return null;	
			}
		case SHOWING_NOTIFICATION:
			switch(messageName){
			case CANCEL_REQUEST:
				if(m.hasParam("request_id")){
					//TODO gui verdwijnt
					return VolunteerState.IDLE;
				}
			default:
				return null;
			}
			
		case WAITING_FOR_ACKNOWLEDGEMENT:
			switch(messageName){
			case CANCEL_REQUEST:
				if(m.hasParam("request_id")){
					//TODO gui verdwijnt
					return VolunteerState.IDLE;
				}
			case ACKNOWLEDGE_HELP:
				if(m.hasParam("request_id")){
					return VolunteerState.HELPING;
				}
			default:
				return null;
				}
			}
		return null;
	}
 }

