package com.eyecall.connection.test;

import com.eyecall.connection.State;

public enum TestState implements State{
	AWAITING_CONNECTION,
	CONNECTED,
	DISCONNECTED(true);


	private boolean isTerminal = false;
	
	private TestState() {
	}
	
	private TestState(boolean isTerminal) {
		this.isTerminal = isTerminal;
	}


	@Override
	public boolean isTerminal() {
		return isTerminal;
	}
}
