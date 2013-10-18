package com.eyecall.vip;

import com.eyecall.connection.Message;
import com.eyecall.connection.OutQueue;
import com.eyecall.connection.ProtocolHandler;
import com.eyecall.connection.State;
import com.eyecall.protocol.ProtocolName;

public class VIPProtocolHandler implements ProtocolHandler<VIPState> {

	@Override
	public State messageReceived(VIPState state, Message m, OutQueue<Message> queue) {
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

			default:
				return null;
			}
		default:
			return null;
		}
	}

	@Override
	public State messageSent(VIPState state, Message m) {
		// TODO Auto-generated method stub
		return null;
	}
}
