package com.eyecall.vip;
import com.eyecall.connection.State;

public enum VIPState implements State {
	IDLE,
	WAITING,
	BEING_HELPED,
	DISCONNECTED(true);

	private boolean isTerminal = false;

	private VIPState(boolean isTerminal) {
		this.isTerminal = isTerminal;
	}
	
	private VIPState() {
	}
	
	@Override
	public boolean isTerminal() {
		return isTerminal;
	}
}
