package com.eyecall.connection;


/**
 * This interface handles incoming messages from a {@link Connection}. While
 * handling a Message, a ProtocolHandler can add messages to the OutQueue.
 * A ProtocolHandler obtains the current state of the Connection and can change
 * the Connection's state by returning a different state.
 * @author Nicker
 *
 * @param <E> State type
 */
public interface ProtocolHandler<E extends State> {
	
	
	
	/**
	 * handles an incoming message.
	 * @param state {@link Connection}'s current state
	 * @param m incoming message
	 * @param queue message queue. Messages added to this queue will be sent to
	 * the sender of this incoming message
	 * @return the new state the {@link Connection} should have. Return 
	 * <code>null</code> to let the {@link DefaultProtocolHandler} handle this
	 * message.
	 */
	public State messageSent(E state, Message m);
	public State messageReceived(E state, Message m, OutQueue<Message> queue);
}
