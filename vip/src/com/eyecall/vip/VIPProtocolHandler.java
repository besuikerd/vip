package com.eyecall.vip;

import android.location.Location;
import android.util.Log;

import com.eyecall.connection.Message;
import com.eyecall.connection.OutQueue;
import com.eyecall.connection.ProtocolHandler;
import com.eyecall.connection.State;
import com.eyecall.protocol.ProtocolField;
import com.eyecall.protocol.ProtocolName;

public class VIPProtocolHandler implements ProtocolHandler<VIPState> {
	
	public void sendHelpRequest(Location location){
		if(location!=null){
			MainActivity.connection.send(new Message(ProtocolName.REQUEST_HELP).add(ProtocolField.LONGITUDE, location.getLongitude()).add(ProtocolField.LATITUDE, location.getLatitude()));
		}else{
			MainActivity.connection.send(new Message(ProtocolName.REQUEST_HELP).add(ProtocolField.LONGITUDE, 0.0D).add(ProtocolField.LATITUDE, 0.0D));
		}
		// State is changed when messageSent is called
	}
	
	@Override
	public State messageReceived(VIPState state, Message m, OutQueue<Message> queue) {
		Log.d(MainActivity.TAG, "Message received: '" + m.getName() + "' State:" + state.toString());
		ProtocolName messageName = ProtocolName.lookup(m.getName());
		switch (state) {
		case IDLE:
			// TODO invullen

		case WAITING:
			// logger.debug("message received from {} while connected: {}",
			// name, m.toString());
			switch (messageName) {
			case REQUEST_DENIED:
				// TODO melding geven dmv audio
				return VIPState.DISCONNECTED;
			case REQUEST_GRANTED:
				// Open help activity
				MainActivity.getInstance().openHelpActivity();
				// TODO melding geven op scherm
				return VIPState.BEING_HELPED;
			default:
				return null;
			}

		case BEING_HELPED:
			// logger.debug("message received while being helped: {}", m);
			switch (messageName) {
			case REQUEST_FORWARDED:
				// TODO melding geven over het forwarden
				return VIPState.WAITING;
			case OTHER_DISCONNECTED:
				return VIPState.DISCONNECTED;
			case AUDIO_DATA:
				// TODO add to audiobuffer
				/*
				 * if(m.hasParam("audio_data")){ this.data = (Byte[])
				 * m.getParam("audio_data"); queue.add(new
				 * Message("request_forwarded").add("audio_data", value));
				 * return VIPState.BEING_HELPED; }
				 */
				return VIPState.BEING_HELPED;
			default:
				return null;
			}
		default:
			return null;
		}
	}

	@Override
	public State messageSent(VIPState state, Message m) {
		Log.d(MainActivity.TAG, "Message sent: '" + m.getName() + "' State:" + state.toString());
		ProtocolName messageName = ProtocolName.lookup(m.getName());
		switch (state){
		case IDLE:
			switch(messageName){
			case REQUEST_HELP:
				return VIPState.WAITING;
			default:
				return null;
			}
		case WAITING:
			switch(messageName){
			case DISCONNECT:
				return VIPState.DISCONNECTED;
			default:
				return null;
			}
		case BEING_HELPED:
			switch(messageName){
			case MEDIA_DATA:
				return VIPState.BEING_HELPED;
			case UPDATE_LOCATION:
				return VIPState.BEING_HELPED;
			case DISCONNECT:
				return VIPState.DISCONNECTED;
			default:
				return null;
			}
		default:
			return null;
		}
	}
}
