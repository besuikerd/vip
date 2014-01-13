package com.eyecall.vip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.util.Log;

import com.eyecall.connection.Connection;
import com.eyecall.connection.Message;
import com.eyecall.connection.ProtocolHandler;
import com.eyecall.connection.State;
import com.eyecall.eventbus.Event;
import com.eyecall.eventbus.EventBus;
import com.eyecall.protocol.ProtocolField;
import com.eyecall.protocol.ProtocolName;

public class VIPProtocolHandler implements ProtocolHandler<VIPState> {
	private static final Logger logger = LoggerFactory.getLogger(VIPProtocolHandler.class);

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
			case CANCEL_REQUEST:
				return VIPState.DISCONNECTED;
			default:
				return null;
			}
		case BEING_HELPED:
			switch(messageName){
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

	@Override
	public State messageReceived(VIPState state, Message m, Connection c) {
		Log.d(MainActivity.TAG, "Message received: '" + m.getName() + "' State:" + state.toString());
		ProtocolName messageName = ProtocolName.lookup(m.getName());
		switch (state) {
		case IDLE:
			// VIP shouldn't receive message if it is idle
			return null;
		case WAITING:
			switch (messageName) {
			case REQUEST_DENIED:
				// TODO melding geven dmv audio
				
				EventBus.getInstance().post(new Event(EventTag.REQUEST_DENIED));
				return VIPState.DISCONNECTED;
			case REQUEST_GRANTED:
				// Post an event
				EventBus.getInstance().post(new Event(EventTag.REQUEST_GRANTED));
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
				EventBus.getInstance().post(new Event(EventTag.SOUND_CONNECTION_LOST));
				EventBus.getInstance().post(new Event(EventTag.DISCONNECT));
				return VIPState.DISCONNECTED;
			case MEDIA_READY:
				EventBus.getInstance().post(new Event(EventTag.MEDIA_READY, m.getParam(ProtocolField.IP)));
				return state;
			default:
				return null;
			}
		default:
			return null;
		}
	}

	@Override
	public void onDisconnect(VIPState state) {
		ConnectionInstance.clear();
	}
}
