package com.eyecall.server;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyecall.connection.Connection;
import com.eyecall.connection.Message;
import com.eyecall.connection.ProtocolHandler;
import com.eyecall.connection.State;
import com.eyecall.database.Database;
import com.eyecall.database.Location;
import com.eyecall.database.Volunteer;
import com.eyecall.protocol.ErrorCode;
import com.eyecall.protocol.ProtocolField;
import com.eyecall.protocol.ProtocolName;
import com.eyecall.test.ConnectionTest;
import com.google.android.gcm.server.Sender;


public class ServerProtocolHandler implements ProtocolHandler<ServerState> {
	
	private static final Logger logger = LoggerFactory.getLogger(ServerProtocolHandler.class);
    private Request request;

    
    @Override
    public State messageSent(ServerState state, Message m) {
    	switch(ProtocolName.lookup(m.getName())){
    	case REQUEST_GRANTED:
    		return ServerState.CALLING;
    	}
    	return state;
    }
    
    @Override
    public State messageReceived(ServerState state, Message m, Connection c) {
    	logger.debug("Server received message: {}", m);
    	
    	RequestPool pool = RequestPool.getInstance();
    	
    	logger.debug("Message received of type (" + m.getName() + "): " + ProtocolName.lookup(m.getName()));
    	
    	switch(state){
    	
    	case WAITING:
    		
    		switch(ProtocolName.lookup(m.getName())){
    		
    		case REGISTER:
    			
    			String id = m.getParamString(ProtocolField.VOLUNTEER_ID);
    			
    			//create new volunteer with generated id
    			Volunteer v = new Volunteer(id);
    			
    			//save volunteer in the database
    			if(Database.getInstance().insertTransaction(v)){
    				//acknowledge key
        			
    				c.send(new Message(ProtocolName.ACKNOWLEDGE_KEY).add(ProtocolField.KEY, id));
    			} else{
    				c.send(new Message(ProtocolName.REJECT_KEY).add(ProtocolField.KEY, id));
    			}
    			//disconnect the connection
    			return ServerState.DISCONNECTED;
    		case REJECT_REQUEST:
    			break;
    		case ACCEPT_REQUEST:
    			id = m.getParam(ProtocolField.REQUEST_ID).toString();
    			if(pool.exists(id)){
    				if(!pool.isConnected(id)){
    					
    					//attach this connection to the request
    					this.request = pool.attach(id, c);
    					
    					//attach volunteer id to request
    					request.setVolunteerId(m.getParam(ProtocolField.VOLUNTEER_ID).toString());
    					
    					//send request granted message to VIP
    					request.getVipConnection().send(new Message(ProtocolName.REQUEST_GRANTED));
    					
    					//send request acknowledged message to Volunteer
    					request.getVolunteerConnection().send(new Message(ProtocolName.ACKNOWLEDGE_HELP).add(ProtocolField.REQUEST_ID, request.getId()));
    					//TODO send cancel_request to other volunteers
    					return ServerState.CALLING;
    				}
    			}
    			return state;
    		case REQUEST_HELP:
    			logger.debug("request_help received");
    			this.request = pool.setup(c);
    			
    			//TODO remove this piece of code
    			synchronized(ConnectionTest.REQUEST_ID){
    				String old = ConnectionTest.REQUEST_ID;
    				ConnectionTest.REQUEST_ID = request.getId();
    				old.notifyAll();
    			}
    			
    			//TODO change this query to a better one
    			String query = "SELECT v FROM Volunteer v";
    			
    			final List<Volunteer> potentialVolunteers = Database.getInstance().queryForList(query, Volunteer.class);
    			
    			logger.debug("potential volunteers: {}", potentialVolunteers);
    			
    			request.addPendingVolunteers(potentialVolunteers);
    			
    			
    			Sender sender = new Sender(Constants.API_KEY);
    			for(Volunteer volunteer : potentialVolunteers){
    				com.google.android.gcm.server.Message message = new com.google.android.gcm.server.Message.Builder()
    				.addData(ProtocolField.REQUEST_ID.getName(), request.getId())
    				.addData(ProtocolField.LATITUDE.getName(), m.getParamString(ProtocolField.LATITUDE))
    				.addData(ProtocolField.LONGITUDE.getName(), m.getParamString(ProtocolField.LONGITUDE))
    				.build();
    				try {
						sender.sendNoRetry(message, volunteer.getId());
					} catch (IOException e) {
					}
    			}
    			
    			
    			TimerTask t = new TimerTask(){
    				@Override
    				public void run() {
    					for(Volunteer volunteer : potentialVolunteers){
    						if(!volunteer.getId().equals(request.getVolunteerId())){
    							//TODO send reject request messages?
    						}
    					}
    					request.rejectPendingVolunteers();
    					
    					//TODO resend to other group of volunteers
    					/*
    					if(!request.connected()){
    						new Timer().schedule(this, Constants.REQUEST_DELAY);
    					}
    					*/
    				}
    			};
    			new Timer().schedule(t, Constants.REQUEST_DELAY);
    			
    			
    			//TODO find list of volunteers, send out push messages and schedule a task to look for more volunteers
    			return ServerState.FINDING_VOLUNTEERS;
    		case GET_LOCATIONS:
    			String volunteerId = (String) m.getParam(ProtocolField.VOLUNTEER_ID);
    			List<Location> locations = Database.getInstance().queryForList("FROM Location WHERE volunteer.id=?", Location.class, volunteerId);
    			
    			
    			Message msg = new Message(ProtocolName.LOCATIONS);
    			msg.add(ProtocolField.LOCATIONS, locations);
    			c.send(msg);
    			return ServerState.DISCONNECTED;
    		case UPDATE_PREFERED_LOCATION:
    			// Get data
    			String action = m.getParamString(ProtocolField.ACTION);
    			volunteerId = m.getParamString(ProtocolField.VOLUNTEER_ID);
    			
    			// Check if volunteerId is valid
    			Volunteer volunteer = Database.getInstance().query("SELECT v FROM Volunteer v WHERE v.id=?", Volunteer.class, volunteerId);
    			if(volunteer==null){
    				c.send(new Message(ProtocolName.ERROR).add(ProtocolField.ERROR_CODE, ErrorCode.INVALID_VOLUNTEER_ID.getCode()).add(ProtocolField.ERROR_MESSAGE, ""));
    				return ServerState.DISCONNECTED;
    			}
    			
    			
    			if(action.equals(ProtocolField.ACTION_ADD.getName())){
    				// Add
    				Location location = new Location();
    				location.setVolunteer(volunteer);
    				location.setLongitude((float) ((Double) m.getParam(ProtocolField.LONGITUDE)).doubleValue());
    				location.setLatitude( (float) ((Double) m.getParam(ProtocolField.LATITUDE) ).doubleValue());
    				location.setPreferred(m.getParam(ProtocolField.TYPE).equals(ProtocolField.TYPE_PREFERRED.getName()));
    				//location.setRadius((Integer) m.getParam(ProtocolField.RADIUS));
    				
    				Database.getInstance().insertTransaction(location);
    			}else{
    				// Remove
    				String locationId = m.getParamString(ProtocolField.LOCATION_ID);
    				//Location location = new Location();
    				//location.setVolunteer(volunteer);
    				//location.setLongitude((float) ((Double) m.getParam(ProtocolField.LONGITUDE)).doubleValue());
    				//location.setLatitude( (float) ((Double) m.getParam(ProtocolField.LATITUDE) ).doubleValue());
    				//location.setPreferred(m.getParam(ProtocolField.TYPE).equals(ProtocolField.TYPE_PREFERRED.getName()));
    				//location.setRadius((Integer) m.getParam(ProtocolField.RADIUS));
    				
    				String deleteQuery = "delete from Location where id=:locationId";
    				
    				
    				// Afrondingsdingetjes
    				// "0.0" staat in database als "0"
    				/*String longitude, latitude;
    				if(((Double) m.getParam(ProtocolField.LONGITUDE)).doubleValue()%1.0 > 0.0){
    					longitude = ((Double) m.getParam(ProtocolField.LONGITUDE)).doubleValue() + "%";
    				}else{
    					longitude = (int)(((Double) m.getParam(ProtocolField.LONGITUDE)).doubleValue()) + "%";
    				}
    				if(((Double) m.getParam(ProtocolField.LATITUDE)).doubleValue()%1.0 > 0.0){
    					latitude = ((Double) m.getParam(ProtocolField.LATITUDE)).doubleValue() + "%";
    				}else{
    					latitude = (int)(((Double) m.getParam(ProtocolField.LATITUDE)).doubleValue()) + "%";
    				} */
    				
    				// Execute query
    				Session session = Database.getInstance().startSession();
    				Query q = session.createQuery(deleteQuery);
    				q.setString("locationId", locationId);
    				logger.debug("locationId: {}", locationId);
    				//q.setString("longitude",  longitude);
    				//q.setString("latitude",   latitude);
    				/*logger.debug("QueryString: {}", q.getQueryString());
    				logger.debug("volunteerId: {}", volunteer.getId());
    				logger.debug("longitude: {}",  longitude);
    				logger.debug("latitude: {}",   latitude);*/
    				q.executeUpdate();
    				session.close();
    				/*Database.getInstance().query(deleteQuery, Object.class, 
    						volunteer.getId(), 
    						(float) ((Double) m.getParam(ProtocolField.LONGITUDE)).doubleValue(),
    						(float) ((Double) m.getParam(ProtocolField.LATITUDE) ).doubleValue()
    						);*/
    			}
    			return ServerState.DISCONNECTED;
    		default:
    			break;
    		}
    		
    		
    		break;
    	case FINDING_VOLUNTEERS:
    			switch(ProtocolName.lookup(m.getName())){
    			case CANCEL_REQUEST:
    				RequestPool.getInstance().remove("");
    				//TODO send cancel_request to list of volunteers
    				return ServerState.DISCONNECTED;
				default:
					break;
    			}
    		break;
    	case CALLING:
    		switch(ProtocolName.lookup(m.getName())){
    		case UPDATE_LOCATION:
    		case AUDIO_DATA:
    			if(request != null){
    				pool.tunnel(request.getId(), Entity.VOLUNTEER, m);
    			}
    			return state;
    		case MEDIA_DATA:
    			if(request != null){
    				pool.tunnel(request.getId(), Entity.VIP, m);
    			}
    			return state;
    		default:
    			return null;
    		}
		default: 
			return null;
    	}
    	return null;
    }
    
    @Override
    public void onDisconnect(ServerState state) {
    	if(request != null){
    		RequestPool.getInstance().remove(request.getId());
    	}
    }
}
