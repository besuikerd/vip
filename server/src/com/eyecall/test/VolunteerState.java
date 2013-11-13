package com.eyecall.test;

import com.eyecall.connection.State;

public enum VolunteerState implements State {
	INITIALISATION,
	WAITING_FOR_KEY,
	IDLE,
	HELPING,
	WAITING_FOR_ACKNOWLEDGEMENT,
	SHOWING_NOTIFICATION,
	DISCONNECTED(true);
	
	private boolean isTerminal;
	
	private VolunteerState() {
		isTerminal = false;
	}
	
	private VolunteerState(boolean isTerminal){
		this.isTerminal = isTerminal;
	}
	
	@Override
	public boolean isTerminal() {
		return isTerminal;
	}
	
	
}

