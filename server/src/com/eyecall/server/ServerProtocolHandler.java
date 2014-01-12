package com.eyecall.server;

import java.io.IOException;
import java.util.List;

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
import com.google.android.gcm.server.Sender;


public class ServerProtocolHandler implements ProtocolHandler<ServerState> {
	
	/*public ServerProtocolHandler(Volunteer volunteer){
		
	}*/
	
	private static final Logger logger = LoggerFactory.getLogger(ServerProtocolHandler.class);
    
	/**
	 * The request corresponding to this connection, could be null
	 */
	private Request request;
    
    public static Sender getGCMSender(){
    	return new Sender(Constants.API_KEY);
    }
    
    /**
     * Send a GCM message to the volunteer that the request is cancelled
     * @param volunteer
     */
    public static void sendRequestCancelled(Volunteer volunteer, Request request){
    	sendRequestCancelled(volunteer.getId(), request);
    }
    
    public static void sendRequestCancelled(String volunteerId, Request request) {
    	com.google.android.gcm.server.Message message = new com.google.android.gcm.server.Message.Builder()
		.addData(ProtocolField.NAME.getName(), ProtocolName.CANCEL_REQUEST.getName())
		.addData(ProtocolField.REQUEST_ID.getName(), request.getId())
		.build();
		try {
			getGCMSender().sendNoRetry(message, volunteerId);
		} catch (IOException e) {
		}
	}

    /**
     * Send request denied to the vip (nobody is willing/able to help)
     * @param vipConnection
     * @param request
     */
	public static void sendRequestDenied(Connection vipConnection, Request request) {
		vipConnection.send(new Message(ProtocolName.REQUEST_DENIED).add(ProtocolField.REQUEST_ID, request.getId()));
	}

	/**
     * Send A new request via GCM to the volunteer
     * @param volunteer
     * @param longitude
     * @param latitude
     */
    public static void sendNewRequest(Volunteer volunteer, Request request){
    	com.google.android.gcm.server.Message message = new com.google.android.gcm.server.Message.Builder()
    	.addData(ProtocolField.NAME.getName(), ProtocolName.NEW_REQUEST.getName())
    	.addData(ProtocolField.REQUEST_ID.getName(), request.getId())
		.addData(ProtocolField.LATITUDE.getName(), request.getLatitude().toString())
		.addData(ProtocolField.LONGITUDE.getName(), request.getLongitude().toString())
		.build();
		try {
			getGCMSender().sendNoRetry(message, volunteer.getId());
		} catch (IOException e) {
		}
    }

    
    @Override
    public State messageSent(ServerState state, Message m) {
    	switch(ProtocolName.lookup(m.getName())){
    	case REQUEST_GRANTED:
    		return ServerState.CALLING;
		default:
			break;
    	}
    	return state;
    }
    
    @Override
    public State messageReceived(ServerState state, Message m, Connection c) {
    	logger.debug("Server received message: {}", m);
    	
    	RequestPool pool = RequestPool.getInstance();
    	Database db = Database.getInstance();
    	logger.debug("Message received of type (" + m.getName() + "): " + ProtocolName.lookup(m.getName()));
    	
    	
    	
		switch(state){
    	
    	case WAITING:
    		
    		switch(ProtocolName.lookup(m.getName())){
    		
    		case REGISTER:
    			
    			String id = m.getParamString(ProtocolField.VOLUNTEER_ID);
    			
    			
    			
    			//create new volunteer with generated id
    			Volunteer v = db.query("FROM Volunteer where id=?", Volunteer.class, id);
    			
    			if(v == null){
    				//save volunteer in the database
    				v = new Volunteer(id);
        			if(Database.getInstance().insertTransaction(v)){
        				//acknowledge key
        				c.send(new Message(ProtocolName.ACKNOWLEDGE_KEY).add(ProtocolField.KEY, id));
        			} else{
        				c.send(new Message(ProtocolName.REJECT_KEY).add(ProtocolField.KEY, id));
        			}
    			} else{
    				c.send(new Message(ProtocolName.ACKNOWLEDGE_KEY).add(ProtocolField.KEY, id));
    			}
    			
    			
    			//disconnect the connection
    			return ServerState.DISCONNECTED;
    			
    		case VERIFY:
    			
    			id = m.getParamString(ProtocolField.VOLUNTEER_ID);
    			v = Database.getInstance().query("FROM Volunteer where id=?", Volunteer.class, id);
    			c.send(new Message(v == null ? ProtocolName.KEY_UNKNOWN : ProtocolName.KEY_EXISTS).add(ProtocolField.VOLUNTEER_ID, id));
    			
    			return ServerState.DISCONNECTED;
    			
    		case REJECT_REQUEST:
    			request = RequestPool.getInstance().getPendingRequest(m.getParam(ProtocolField.REQUEST_ID).toString());
    			if(request!=null) {
    				logger.debug("found request to reject. rejecting...");
    				request.rejectPendingVolunteer(m.getParam(ProtocolField.VOLUNTEER_ID).toString());
    			} else{
    				logger.debug("cannot find request to reject");
    			}
    			return ServerState.WAITING;
    		case ACCEPT_REQUEST:
    			id = m.getParam(ProtocolField.REQUEST_ID).toString();
    			if(pool.exists(id)){
    				if(!pool.isConnected(id)){
    					
    					// attach this connection to the request
    					request = pool.attach(id, c);
    					
    					// attach volunteer id to request
    					request.setVolunteerId(m.getParam(ProtocolField.VOLUNTEER_ID).toString());
    					
    					// Remove volunteer from pending volunteers
    					request.removePendingVolunteer(m.getParam(ProtocolField.VOLUNTEER_ID).toString());
    					
    					// send request granted message to VIP
    					request.getVipConnection().send(new Message(ProtocolName.REQUEST_GRANTED).add(ProtocolField.ADDRESS, request.getVolunteerConnection().getSocket().getInetAddress().getHostAddress()));
    					
    					// send request acknowledged message to Volunteer
    					request.getVolunteerConnection().send(new Message(ProtocolName.ACKNOWLEDGE_HELP).add(ProtocolField.REQUEST_ID, request.getId()).add(ProtocolField.ADDRESS, request.getVipConnection().getSocket().getInetAddress().getHostAddress()));
    					
    					// send cancel to other volunteers
    					request.sendCancelToPendingVolunteers();
    					
    					// Dont clear pending volunteers for redirection
    					
    					return ServerState.CALLING;
    				}else{
    					// Already being helped
    					sendRequestCancelled(m.getParam(ProtocolField.VOLUNTEER_ID).toString(), request);
    				}
    			}
    			return state;
    		case REQUEST_HELP:
    			logger.debug("request_help received");
    			
    			// Create a new request
    			request = pool.setup(c, m.getParamString(ProtocolField.LONGITUDE), m.getParamString(ProtocolField.LATITUDE));
    			
    			// And start
    			request.start();
    			
    			return ServerState.FINDING_VOLUNTEERS;
    		case CANCEL_REQUEST:
    			//TODO cancel pending requests and such
    			return ServerState.DISCONNECTED;
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
    				location.setRadius(((Double) m.getParam(ProtocolField.RADIUS)).doubleValue());
    				logger.debug("Radius: {}", ((Double) m.getParam(ProtocolField.RADIUS)).doubleValue());
    				Database.getInstance().insertTransaction(location);
    			}else{
    				// Remove
    				String locationId = m.getParamString(ProtocolField.LOCATION_ID);
    				// Execute query
    				Session session = Database.getInstance().startSession();
    				Query q = session.createQuery(Constants.DELETE_QUERY);
    				q.setString("locationId", locationId);
    				logger.debug("locationId: {}", locationId);
    				q.executeUpdate();
    				session.close();
    			}
    			return ServerState.DISCONNECTED;
    		default:
    			break;
    		}
    		
    		
    		break;
    	case FINDING_VOLUNTEERS:
    			switch(ProtocolName.lookup(m.getName())){
    			case CANCEL_REQUEST:
    				request.sendCancelToPendingVolunteers();
    				request.invalidate();
    				request.rejectPendingVolunteers();
    				request.close();
    				return ServerState.DISCONNECTED;
				default:
					break;
    			}
    		break;
    	case CALLING:
    		switch(ProtocolName.lookup(m.getName())){
    		case UPDATE_LOCATION:

    			if(request != null){
    				pool.tunnel(request.getId(), Entity.VOLUNTEER, m);
    			}
    			return state;
    			
    		case MEDIA_READY:
    			if(request != null){
    				pool.tunnel(request.getId(), c.equals(request.getVolunteerConnection()) ? Entity.VIP : Entity.VOLUNTEER, m);
    			}
    			return state;
    			
    		case DISCONNECT:
    			if(request != null){
    				pool.tunnel(request.getId(), c.equals(request.getVolunteerConnection()) ? Entity.VIP : Entity.VOLUNTEER, new Message(ProtocolName.OTHER_DISCONNECTED));
    			}
    			return ServerState.DISCONNECTED;
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
