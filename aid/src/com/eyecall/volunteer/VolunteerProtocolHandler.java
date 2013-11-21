package com.eyecall.volunteer;

import java.util.List;

import android.util.Log;

import com.eyecall.android.ConnectionInstance;
import com.eyecall.connection.Connection;
import com.eyecall.connection.Message;
import com.eyecall.connection.ProtocolHandler;
import com.eyecall.connection.State;
import com.eyecall.eventbus.Event;
import com.eyecall.eventbus.EventBus;
import com.eyecall.protocol.ProtocolField;
import com.eyecall.protocol.ProtocolName;

public class VolunteerProtocolHandler implements ProtocolHandler<VolunteerState> {
	
	/**
	 * 
	 * @param connection Connection to send message via
	 * @param id Id of this volunteer
	 * @param location Location to remove
	 */
	public static void removeLocation(Connection connection, String id, Location location){
		connection.send(new Message(
				ProtocolName.UPDATE_PREFFERED_LOCATION)
		.add(ProtocolField.VOLUNTEER_ID, id)
		.add(ProtocolField.ACTION, ProtocolField.ACTION_DELETE.getName())
		.add(ProtocolField.LATITUDE, location.getLatitude())
		.add(ProtocolField.LONGITUDE, location.getLongitude())
		.add(ProtocolField.TYPE, location.isPreferred() ? ProtocolField.TYPE_PREFERRED.getName() : ProtocolField.TYPE_NON_PREFERRED.getName())
				);
	}
	
	/**
	 * 
	 * @param connection Connection to send message via
	 * @param id Id of this volunteer
	 * @param location Location to add
	 */
	public static void addLocation(Connection connection, String id, Location location){
		connection.send(new Message(
				ProtocolName.UPDATE_PREFFERED_LOCATION)
		.add(ProtocolField.VOLUNTEER_ID, id)
		.add(ProtocolField.ACTION, ProtocolField.ACTION_ADD.getName())
		.add(ProtocolField.LATITUDE, location.getLatitude())
		.add(ProtocolField.LONGITUDE, location.getLongitude())
		.add(ProtocolField.TYPE, location.isPreferred() ? ProtocolField.TYPE_PREFERRED.getName() : ProtocolField.TYPE_NON_PREFERRED.getName())
				);
	}
	
	public static void requestLocations(Connection connection, String id){
		connection.send(new Message(
				ProtocolName.GET_LOCATIONS)
		.add(ProtocolField.VOLUNTEER_ID, id)
				);
	}
	
	public State messageSent(VolunteerState state, Message m) {
		switch(ProtocolName.lookup(m.getName())){
		case ACCEPT_REQUEST:
			return VolunteerState.WAITING_FOR_ACKNOWLEDGEMENT; 
		case REGISTER:
			return VolunteerState.WAITING_FOR_KEY;
		default: 
			return state;
		}
	}

	@Override
	public State messageReceived(VolunteerState state, Message m, Connection c) {
		Log.d(MainActivity.TAG, "Message received: '" + m.getName() + "' State:" + state.toString());
		ProtocolName messageName = ProtocolName.lookup(m.getName());
		switch (state){
		case INITIALISATION:
			//TODO iets met log
			
		case WAITING_FOR_KEY:
			switch(messageName){
			case ACKNOWLEDGE_KEY:
				EventBus.getInstance().post(new Event(EventTag.ID_ACCEPTED, m.getParam(ProtocolField.KEY)));
				break;
			case REJECT_KEY:
				EventBus.getInstance().post(new Event(EventTag.ID_REJECTED, m.getParam(ProtocolField.KEY)));
				break;
			}
			break;
		
		case IDLE:
			switch(messageName){
			case NEW_REQUEST:
				//if(m.hasParams("request_id","longitude", "latitude")){
					//TODO pop-up keuze menu
				return VolunteerState.SHOWING_NOTIFICATION;
				//}	
			case LOCATIONS:
				//m.g
				
				
				return VolunteerState.IDLE;
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
					EventBus.getInstance().post(new Event(EventTag.REQUEST_DENIED));
					return VolunteerState.IDLE;
				}
			case ACKNOWLEDGE_HELP:
				if(m.hasParam("request_id")){
					EventBus.getInstance().post(new Event(EventTag.REQUEST_ACKNOWLEDGED));
					return VolunteerState.HELPING;
				}
			default:
				return null;
				}
			}
		return null;
	}

	@Override
	public void onDisconnect(VolunteerState state) {
		ConnectionInstance.clear();
	}
 }

