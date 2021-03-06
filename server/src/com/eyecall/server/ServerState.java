package com.eyecall.server;

import com.eyecall.connection.State;

public enum ServerState implements State{
	WAITING,
	FINDING_VOLUNTEERS,
	CALLING,
	DISCONNECTED(true)
;
	
	
	private boolean isTerminal = false;

	private ServerState(boolean isTerminal) {
		this.isTerminal = isTerminal;
	}
	
	private ServerState() {
	}
	
	@Override
	public boolean isTerminal() {
		return isTerminal;
	}
}
