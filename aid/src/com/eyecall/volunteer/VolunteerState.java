package com.eyecall.volunteer;

import com.eyecall.connection.State;

public enum VolunteerState implements State {
	
	INITIALISATION,
	WAITING_FOR_KEY,
	IDLE,
	HELPING,
	WAITING_FOR_ACKNOWLEDGEMENT,
	WAITING_FOR_VERIFICATION,
	SHOWING_NOTIFICATION,
	DISCONNECTED(true)
	
	;
	
	private boolean isTerminal;
	
	private VolunteerState(boolean isTerminal) {
		this.isTerminal = isTerminal;
	}

	private VolunteerState() {
		this(false);
	}

	@Override
	public boolean isTerminal() {
		return isTerminal;
	}	
}

