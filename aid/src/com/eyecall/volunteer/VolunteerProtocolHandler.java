package com.eyecall.volunteer;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyecall.android.ConnectionInstance;
import com.eyecall.connection.Connection;
import com.eyecall.connection.Message;
import com.eyecall.connection.ProtocolHandler;
import com.eyecall.connection.State;
import com.eyecall.eventbus.Event;
import com.eyecall.eventbus.EventBus;
import com.eyecall.protocol.ErrorCode;
import com.eyecall.protocol.ProtocolField;
import com.eyecall.protocol.ProtocolName;
import com.google.android.gms.maps.model.LatLng;

public class VolunteerProtocolHandler implements ProtocolHandler<VolunteerState> {
	
	private static final Logger logger = LoggerFactory
			.getLogger(VolunteerProtocolHandler.class);
	
	/**
	 * 
	 * @param connection Connection to send message via
	 * @param id Id of this volunteer
	 * @param location Location to remove
	 */
	public static void removeLocation(Connection connection, String id, Location location){
		connection.send(new Message(
				ProtocolName.UPDATE_PREFERED_LOCATION)
		.add(ProtocolField.VOLUNTEER_ID, id)
		.add(ProtocolField.ACTION, ProtocolField.ACTION_DELETE.getName())
		.add(ProtocolField.LOCATION_ID, location.getId())
		//.add(ProtocolField.LATITUDE, location.getLatitude())
		//.add(ProtocolField.LONGITUDE, location.getLongitude())
		//.add(ProtocolField.TYPE, location.isPreferred() ? ProtocolField.TYPE_PREFERRED.getName() : ProtocolField.TYPE_NON_PREFERRED.getName())
				);
	}
	
	/**
	 * 
	 * @param connection Connection to send message via
	 * @param id Id of this volunteer
	 * @param location Location to add
	 */
	public static void addLocation(Connection connection, String id, Location location){
		logger.debug("Sending location addition...");
		connection.send(new Message(
				ProtocolName.UPDATE_PREFERED_LOCATION)
		.add(ProtocolField.VOLUNTEER_ID, id)
		.add(ProtocolField.ACTION, ProtocolField.ACTION_ADD.getName())
		.add(ProtocolField.LATITUDE, location.getLatitude())
		.add(ProtocolField.LONGITUDE, location.getLongitude())
		.add(ProtocolField.RADIUS, location.getRadius())
		.add(ProtocolField.TYPE, location.isPreferred() ? ProtocolField.TYPE_PREFERRED.getName() : ProtocolField.TYPE_NON_PREFERRED.getName())
				);
	}
	
	public static void requestLocations(Connection connection, String id){
		connection.send(new Message(
				ProtocolName.GET_LOCATIONS)
		.add(ProtocolField.VOLUNTEER_ID, id)
				);
	}
	
	public static void sendKeyToServer(String key){
		// send registry key to server
		Connection c;
		try {
			logger.debug("Sending key to server...: {}", key);
			c = new Connection(Constants.SERVER_URL,
					Constants.SERVER_PORT,
					new VolunteerProtocolHandler(),
					VolunteerState.INITIALISATION);
			c.init(false);
			c.send(new Message(ProtocolName.REGISTER).add(
					ProtocolField.VOLUNTEER_ID, key));
			logger.debug("Succes!");
		} catch (UnknownHostException e) {
			logger.error("Unable to connect to server: {}}", e);
		}
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
		logger.debug("Message received: '" + m.getName() + "' State:" + state.toString());
		ProtocolName messageName = ProtocolName.lookup(m.getName());
		
		if(messageName.equals(ProtocolName.ERROR)){
			int errorCode = (Integer) m.getParam(ProtocolField.ERROR_CODE);
			if(errorCode == ErrorCode.INVALID_VOLUNTEER_ID.getCode()){
				logger.debug("Error code received: Invalid volunteer id. Posting event...");
				EventBus.getInstance().post(new Event(EventTag.ID_INVALID));
				return state;
			}
	
		}
		
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
				String raw = m.getParam(ProtocolField.LOCATIONS).toString();
				logger.debug("Locations received! raw json: {}", raw);
				
				// Get input
				List<Object> input = (List<Object>) m.getParam(ProtocolField.LOCATIONS);
				
				// Define variables
				List<Location> locations = new ArrayList<Location>();
				Map<String, Object> itemMap;
				
				for(Object item : input){
					itemMap = (Map<String, Object>) item;
					Location location = new Location();
					location.setId((Integer)itemMap.get("id"));
					location.setLatitude( (float) ((Double)itemMap.get("latitude") ).doubleValue());
					location.setLongitude((float) ((Double)itemMap.get("longitude")).doubleValue());
					location.setPreferred(((Boolean)itemMap.get("preferred")).booleanValue());
					location.setRadius((Double)itemMap.get("radius"));
					logger.debug("Location parsed: {}", location.toString());
					locations.add(location);
				}
				
				EventBus.getInstance().post(new Event(EventTag.LOCATIONS_RECEIVED, locations));
				
				return VolunteerState.IDLE;
			default:
				return null;
			}
			
		
		case HELPING:
			switch(messageName){
			case OTHER_DISCONNECTED:
				EventBus.getInstance().post(new Event(EventTag.DISCONNECTED));
				return VolunteerState.DISCONNECTED;
			case UPDATE_LOCATION:
//				if(m.hasParams("longitude", "latitude")){
					double lng = m.getParam(ProtocolField.LONGITUDE, Double.class);
					double lat = m.getParam(ProtocolField.LATITUDE, Double.class);
					EventBus.getInstance().post(new Event(EventTag.LOCATION_UPDATE, new LatLng(lat, lng)));
					return VolunteerState.HELPING;
				//}
			case MEDIA_READY:
				EventBus.getInstance().post(new Event(EventTag.MEDIA_READY, m.getParam(ProtocolField.IP)));
				return state;
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
					EventBus.getInstance().post(new Event(EventTag.REQUEST_CANCELLED));
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
			
			
		case WAITING_FOR_VERIFICATION:
			switch(messageName){
			case KEY_EXISTS:
				EventBus.getInstance().post(new Event(EventTag.KEY_VERIFIED));
				return VolunteerState.DISCONNECTED;
			case KEY_UNKNOWN:
				EventBus.getInstance().post(new Event(EventTag.KEY_UNKNOWN));
				return VolunteerState.DISCONNECTED;
			}
			}
		
		return null;
	}

	@Override
	public void onDisconnect(VolunteerState state) {
		ConnectionInstance.clear();
	}
 }

