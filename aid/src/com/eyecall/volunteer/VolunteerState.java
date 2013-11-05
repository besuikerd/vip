package com.eyecall.volunteer;

import com.eyecall.connection.State;

public enum VolunteerState implements State {
	INITIALISATION,
	WAITING_FOR_KEY,
	IDLE,
	HELPING,
	WAITING_FOR_ACKNOWLEDGEMENT,
	SHOWING_NOTIFICATION;

	@Override
	public boolean isTerminal() {
		// TODO Auto-generated method stub
		return false;
	}	
}

