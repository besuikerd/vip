package com.eyecall.connection;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Buffers <code>&#60;E&#62;</code>'s to be sent across a Socket. When this 
 * Runnable is fired up, it will send messages added to the queue. The objects
 * will be serialized to JSON prior te being sent. Adding messages to this
 * queue is thread safe. 
 * @author Nicker
 *
 * @param <E> Object type to be sent across the socket
 */
public class OutQueue<E> implements Runnable{
	
	private static Logger logger = LoggerFactory.getLogger(OutQueue.class);
	
	/**
	 * Socket across which <code>&#60;E&#62;</code>'s are being sent
	 */
	private Socket s;
	
	/*
	 * queue that contains the <code>&#60;E&#62;</code>'s
	 */
	private Queue<E> queue;
	private ObjectMapper mapper;
	
	public OutQueue(Socket s) {
		this.s = s;
		this.queue = new LinkedList<E>();
		this.mapper = new ObjectMapper();
		mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
	}
	
	@Override
	public void run() {
		//notify that thread is started
		synchronized(this){
			notifyAll();
		}
		
		while(!s.isClosed()){
			//empty the queue and send the messages through the socket
			synchronized(this){
				while(!queue.isEmpty()){
				
					E message = queue.remove();
					
					logger.debug("sending: {}", message);
					try {
						mapper.writeValue(s.getOutputStream(), message);
					} catch (IOException e) {
						logger.error("Unexpected io exception while writing a message from the queue: {}", e.toString());
						
						//close the socket
						try {
							s.close();
						} catch (IOException e2) {
						}
					}
					
					//notify other threads waiting on this OutQueue 
					synchronized(this){
						notifyAll();
					}
				}
			}
			synchronized(this){
				try {
					wait(100);
					if(!queue.isEmpty())
						logger.debug("queue: {}", queue);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
		}
	}
	
	/**
	 * adds an <code>&#60;E&#62;</code> to this queue. This method is thread
	 * safe.
	 * @param e
	 */
	public void add(E e){
		logger.debug("adding message to queue: {}", e);
		synchronized(this){
			queue.add(e);
			notifyAll();
		}
	}
	
	/**
	 * closes the Socket that this OutQueue uses.
	 * @throws IOException
	 */
	public void close() throws IOException{
		s.close();
	}
	
	/**
	 * returns <code>true</code> if this queue contains no elements
	 * @return <code>true</code> if this queue contains no elements
	 */
	public boolean isEmpty(){
		return queue.isEmpty();	
	}
}