package com.eyecall.connection;

public class StatelessProtocolHandler implements ProtocolHandler{

	@Override
	public State messageSent(State state, Message m) {
		return state;
	}

	@Override
	public State messageReceived(State state, Message m, Connection c) {
		synchronized(c){
		}
		return state;
	}

	@Override
	public void onDisconnect(State state) {
	}
	
	public static State statelessState() {
		return new State(){
		
			@Override
			public boolean isTerminal() {
				return false;
			}
		};
	}
}
