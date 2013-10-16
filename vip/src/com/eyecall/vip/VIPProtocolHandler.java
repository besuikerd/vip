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
			return null;
		case BEING_HELPED:
			return null;
		default:
			return null;
		}
	}

}
