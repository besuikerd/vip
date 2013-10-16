package com.eyecall.volunteer;

import java.util.Map;

import com.eyecall.connection.Message;
import com.eyecall.connection.OutQueue;
import com.eyecall.connection.ProtocolHandler;
import com.eyecall.connection.State;


public class VolunteerProtocolHandler implements ProtocolHandler<VolunteerState> {
	@Override
	public State handleMessage(VolunteerState state, Message m,
			OutQueue<Message> queue) {
		// TODO Auto-generated method stub
		return null;
	}
}
