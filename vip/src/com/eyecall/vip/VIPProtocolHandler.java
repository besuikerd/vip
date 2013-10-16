package com.eyecall.vip;

import java.util.Map;

import com.eyecall.connection.Message;
import com.eyecall.connection.OutQueue;
import com.eyecall.connection.ProtocolHandler;
import com.eyecall.connection.State;

public class VIPProtocolHandler implements ProtocolHandler<VIPState> {

	@Override
	public State handleMessage(VIPState state, Message m, OutQueue<Message> queue) {
		switch(state){
		case IDLE:
		// TODO invullen
			
		case WAITING:
		//	logger.debug("message received from {} while connected: {}", name, m.toString());
			switch(m.getName()){
			case "request_denied":
				queue.add(new Message("request_denied"));
				return VIPState.IDLE;
			case "request_granted":
				queue.add(new Message("request_forwarded"));
				return VIPState.BEING_HELPED;	
			default:
				return null;
			}
			
		case BEING_HELPED:
		//	logger.debug("message received while being helped: {}", m);
			switch(m.getName()){
			case "request_forwarded":
				queue.add(new Message("request_forwarded"));
				return VIPState.WAITING;
			case "other_disconnected":
				queue.add(new Message("other_disconnected"));
				return VIPState.IDLE;
			case "audio_data":
				// TODO add to audiobuffer
				/*if(m.hasParam("audio_data")){
					this.data = (Byte[]) m.getParam("audio_data");
					queue.add(new Message("request_forwarded").add("audio_data", value));
					return VIPState.BEING_HELPED;
				}*/
				
			default:
				return null;
			}
		}
		
		
		return null;
	}	
}
