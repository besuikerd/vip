package com.eyecall.connection;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * An abstraction of a connection between two sockets. A connection holds an 
 * internal state. All messages passed across the connection should be handled
 * by implementing a {@link ProtocolHandler}. Messages can be sent through the
 * connection by using the {@link #send(Message)} function. Do note that this
 * method call is non blocking. A connection has an internal message buffer
 * that it stores messages to send in. This message buffer is cleared on a
 * seperate thread.
 * 
 * To start up the sending and receiving of messages, the connection needs to
 * be initiated by calling the {@link #init()} method.
 * @author Nicker
 *
 */


//prevent warnings being generated, java generic system is flawed
@SuppressWarnings({"rawtypes", "unchecked"})
public class Connection {
	
	/**
	 * length of UDP packet buffer (16kb)
	 */
	public static final int UDP_BUFFER_SIZE = 16*1024;
	
	public static final long RESULT_TIMEOUT = 10000;
	
	private static final Logger logger = LoggerFactory.getLogger(Connection.class);
	
	
	/**
	 * socket over which messages are passed
	 */
	private Socket s;
	
	/**
	 * socket over which UDP packets are sent
	 */
	private DatagramSocket udpSocket;
	
	/**
	 * ProtocolHandler handling incoming messages
	 */
	private ProtocolHandler handler;
	
	/**
	 * internal state of the connection
	 */
	private State state;
	
	/**
	 * queue that buffers messages
	 */
	private OutQueue<Message> messages;
	
	private Message latestMessage;
	
	private int port;
	private InetAddress host;
	
	/**
	 * construct a new Connection
	 * @param s socket over which messages are passed
	 * @param handler ProtocolHandler handling incoming messages
	 * @param state starting state of the connection
	 */
	public Connection(Socket s, ProtocolHandler handler, State state) {
		this.s = s;
		this.handler = handler;
		this.state = state;
		if(s != null){
			this.port = s.getPort();
			this.host = s.getInetAddress();
		}
	}
	
	

	public Connection(String host, int port, ProtocolHandler handler, State state) throws UnknownHostException {
		this(null, handler, state);
		this.port = port;
		this.host = InetAddress.getByName(host);
	}
	
	public Connection(String host, int port) throws UnknownHostException{
		this(host, port, new StatelessProtocolHandler(), StatelessProtocolHandler.statelessState());
	}
	
	public Connection(Socket s){
		this(s, new StatelessProtocolHandler(), StatelessProtocolHandler.statelessState());
	}
	
	private Connection getConnection(){
		return this;
	}

	/**
	 * send a message over this connection. This method is non-blocking; the
	 * message is buffered before being sent.
	 * @param m
	 */
	public void send(Message m){
		messages.add(m);
		synchronized(state){
			State newState = handler.messageSent(state, m);
			if(newState == null){
				newState = new DefaultProtocolHandler().messageSent(state, m);
			}
			state = newState;
		}
		if(state.isTerminal()){
			try {
				close();
			} catch (IOException e) {
			}
		}
	}
	
	
	/**
	 * sends a message over this connection and wait for a result. Note that 
	 * this method is blocking. Only allowed when the connection is a stateless
	 * connection (No implementation of ProtocolHandler given). Timeout to wait
	 * for is set in {@link #RESULT_TIMEOUT}.
	 * @return
	 */
	@Deprecated //not working
	public Message sendForResult(Message m) throws IOException{
		if(handler instanceof StatelessProtocolHandler){
			send(m);
			Message result = null;
			logger.debug("message {} sent. waiting for result...", m);
			synchronized(this){
				try {
					wait(RESULT_TIMEOUT);
				} catch (InterruptedException e) {
				}
				logger.debug("out of lock, did we receive a message? {}", latestMessage != null);
				result = latestMessage;
				latestMessage = null;
				if(result == null){
					throw new IOException("Message not received after timeout");
				}
			}
			return result;
		} else{
			throw new IllegalStateException("Not allowed wait for results in a stateful connection");
		}
	}
	
	protected void setLatestMessage(Message latestMessage) {
		this.latestMessage = latestMessage;
	}
	
	/**
	 * send a message with UDP instead of TCP
	 */
	public void sendUDP(Message m) throws IOException{
		byte[] data = new ObjectMapper().writeValueAsBytes(m);
		if(udpSocket == null){
			udpSocket = new DatagramSocket(port, host);
		}
		udpSocket.send(new DatagramPacket(data, data.length, host, port));
	}
	
	/**
	 * closes this connection and corresponding Socket. blocks until OutQueue
	 * is being emptied
	 * @throws IOException
	 */
	public void close() throws IOException{
		
		//block until OutQueue is empty
		synchronized(messages){
			while(!messages.isEmpty()){
				try {
					messages.wait();
				} catch (InterruptedException e) {
				}
			}
		}
		
		s.getOutputStream().flush();
		
		//close the Socket
		s.close();
		logger.debug("Connection closed gracefully");
	}
	
	/**
	 * Initiates this Connection. Fires up ConnectionHandler thread
	 */
	public void init(boolean useUDP){
		//block until ConnectionHandler thread is started up
		CountDownLatch latch = new CountDownLatch(1);
		ConnectionHandler handler = new ConnectionHandler(latch);
		Thread connectionHandlerThread = new Thread(handler);
		connectionHandlerThread.start();
		try {
			latch.await();
		} catch (InterruptedException e1) {
		}
		
		
		
		if(useUDP){
			try {
				udpSocket = new DatagramSocket(port, host);
			} catch (SocketException e) {
				e.printStackTrace();
				logger.warn("unable to bind to UDP socket");
				return;
			}
			
		
		
			//block until UDPReader thread is started up
			UDPReader reader = new UDPReader();
			Thread UDPReaderThread = new Thread(reader);
			UDPReaderThread.start();
			while(!UDPReaderThread.isAlive()){
				synchronized(reader){
					try{
						reader.wait();
					} catch(InterruptedException e){
						
					}
				}
			}
		}
	}
	
	private class UDPReader implements Runnable{
		
		@Override
		public void run() {
			if(udpSocket == null){
				try {
					udpSocket = new DatagramSocket(port, host);
				} catch (SocketException e) {
					e.printStackTrace();
				}
			}
			
			//notify that the thread is started
			synchronized(this){
				notifyAll();
			}
			
			ObjectMapper mapper = new ObjectMapper();
			
			while(!state.isTerminal()){
				byte[] buff = new byte[UDP_BUFFER_SIZE];
				DatagramPacket packet = new DatagramPacket(buff, buff.length);
				try {
					udpSocket.receive(packet);
					byte[] data = new byte[packet.getLength()];
					System.arraycopy(packet.getData(), 0, data, 0, packet.getLength());
					messages.add(mapper.readValue(data, Message.class));
				} catch (IOException e) {
				}
			}
		}
	}
	
	/**
	 * This Thread responsible of handling the InputStream of the Socket. 
	 * Before this thread is fired up, it fires up the OutQueue thread first. 
	 * It maps incoming JSON messages to {@link Message} objects. This message
	 * is passed to the {@link ProtocolHandler} of this Connection that 
	 * processes the message. When a state change is needed, this class performs
	 * that state change.
	 * @author Nicker
	 *
	 */
	private class ConnectionHandler implements Runnable{
		
		private CountDownLatch latch;
		
		public ConnectionHandler(CountDownLatch latch) {
			this.latch = latch;
		}



		@Override
		public void run() {
			//initiate socket if this hasn't been done yet.
			if(s == null){
				try {
					s = new Socket(host, port);
				} catch(IOException e){
					logger.warn("unable to instantiate socket from {}:{}", host, port);
				}
			}
			
			messages = new OutQueue<Message>(s);
			
			latch.countDown();
			
			if(s == null){
				return;
			}
			
			//initiate and configure ObjectMapper
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
			
			//fire up outputqueue thread
			CountDownLatch latch = new CountDownLatch(1);
			
			messages.start(latch);
			
			//block until outQueue thread is started up
			try {
				latch.await();
			} catch (InterruptedException e2) {
			}
			
			//initiate message iterator that iterates over the InputStream
			Iterator<Message> iterator = null;
			try {
				iterator = mapper.readValues(new JsonFactory().createParser(s.getInputStream()), Message.class);
			} catch (IOException e) {
				logger.warn("unexpected IOException while reading message: {}", e.toString());
				//close socket
				try {
					s.close();
				} catch (IOException e1) {
				}
			}
			
			//iterate over the messages and handle the incoming messages
			
			try{
				if(iterator != null){
					while(iterator.hasNext()){
						Message m = iterator.next();
						logger.debug("message received in state {}: {}", state, m);
						State newState = handler.messageReceived(state, m, getConnection()); 
						if(newState == null){
							newState = new DefaultProtocolHandler().messageReceived(state, m, getConnection());
						}
						state = newState;
						
						//close connection if state is terminal
						if(state.isTerminal()){
							try {
								close();
							} catch (IOException e) {
							}
						}
					}
				}
			//eww nasty solution by Jackson ObjectMapper. Throws a RuntimeException when the socket is closed...
			} catch(RuntimeException e){
				logger.debug("message iterator seems to be done. Is the socket closed?: {}", e.toString());
			}
			//close the socket
			try {
				s.close();
			} catch (IOException e) {
			}
			
			//handle disconnect
			handler.onDisconnect(state);
		}
	}
}
