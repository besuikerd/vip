package com.eyecall.connection.test;

import com.eyecall.connection.State;

public enum TestState implements State{
	AWAITING_CONNECTION,
	CONNECTED,
	DISCONNECTED;

	@Override
	public boolean isTerminal() {
		return true;
	}
}
