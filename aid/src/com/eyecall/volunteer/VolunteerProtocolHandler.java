package com.eyecall.volunteer;

import java.util.Map;

<<<<<<< HEAD
import android.content.SharedPreferences.Editor;

=======
>>>>>>> refs/heads/master
import com.eyecall.connection.Message;
import com.eyecall.connection.OutQueue;
import com.eyecall.connection.ProtocolHandler;
import com.eyecall.connection.State;
import com.eyecall.vip.VIPState;

public class VolunteerProtocolHandler implements ProtocolHandler {
	public State handleMessage(VolunteerState state, Message m, OutQueue<Message> queue){
		switch (state){
			case INITIALISATION:
				//TODO iets met log
				
			case IDLE:
				switch(m.getName()){
				case "new_request":
					if(m.hasParams("request_id","longitude", "latitude")){
						//TODO pop-up keuze menu
					return VolunteerState.SHOWING_NOTIFICATION;
					}	
				default:
					return null;
				}
				
			case WAITING_FOR_KEY:
				switch(m.getName()){
				case "assign_key":
					if(m.hasParam("key")){
						//TODO shared preferences in context maken
						Editor edit = sp.edit();
						edit.putString("Volunteer key", (String)m.getParam("key"));
						edit.commit();
						return VolunteerState.IDLE;
					}
				default:
					return null;
				}
			case HELPING:
				switch(m.getName()){
				case "other_disconnected":
					//TODO pop-up in scherm weergeven
					
					return VolunteerState.HELPING;
				case"update_location":
					if(m.hasParams("longitude", "latitude")){
						return VolunteerState.HELPING;
					}
				case "media_data":
					if(m.hasParam("data")){
					//TODO data op scherm tonen	
					return VolunteerState.HELPING;
					}
				default:
					return null;	
				}
			case SHOWING_NOTIFICATION:
				switch(m.getName()){
				case "cancel_request":
					if(m.hasParam("request_id")){
						//TODO gui verdwijnt
						return VolunteerState.IDLE;
					}
				default:
					return null;
				}
				
			case WAITING_FOR_ACKNOWLEDGEMENT:
				switch(m.getName()){
				case "cancel_request":
					if(m.hasParam("request_id")){
						//TODO gui verdwijnt
						return VolunteerState.IDLE;
					}
				case "acknowledge_help":
					if(m.hasParam("request_id")){
						return VolunteerState.HELPING;
					}
				default:
					return null;
					}
				}
		}

	@Override
	public State handleMessage(State state, Message m, OutQueue queue) {
		// TODO Auto-generated method stub
		return null;
	}
    }
