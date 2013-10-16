package com.eyecall.vip;

import com.eyecall.connection.Message;
import com.eyecall.connection.OutQueue;
import com.eyecall.connection.ProtocolHandler;
import com.eyecall.connection.State;


public class VIPProtocolHandler implements ProtocolHandler<VIPState> {

	@Override
	public State handleMessage(VIPState state, Message m, OutQueue<Message> queue) {
		switch (state){
		case IDLE:
			return null;
		case WAITING:
			if(m.getName().equals(ProtocolHandler.REQUEST_GRANTED)){
				return VIPState.BEING_HELPED;
			}else if(m.getName().equals(ProtocolHandler.REQUEST_DENIED)){
				return VIPState.IDLE;
			}
			return null;
		case BEING_HELPED:
			if(m.getName().equals(ProtocolHandler.OTHER_DISCONNECTED)){
				return VIPState.IDLE;
			}else if(m.getName().equals(ProtocolHandler.REQUEST_FORWARDED)){
				return VIPState.WAITING;
			}else if(m.getName().equals(ProtocolHandler.AUDIO_DATA)){
				return VIPState.BEING_HELPED;
			}
			return null;
		default:
			return null;
		}
	}

}
