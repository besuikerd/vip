package com.eyecall.test;

import java.io.IOException;
import java.net.BindException;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyecall.connection.Connection;
import com.eyecall.connection.Message;
import com.eyecall.protocol.ProtocolField;
import com.eyecall.protocol.ProtocolName;
import com.eyecall.server.Server;

public class ConnectionTest {
	private static final Logger logger = LoggerFactory.getLogger(ConnectionTest.class);
	private static final int PORT = 5000;
	private static final String HOST = "localhost";
	public static String REQUEST_ID = "";
	
	public static void main(String[] args) throws IOException, InterruptedException {
		Thread.currentThread().setName("Main");
		new ServerThread().start();
		Thread.sleep(1000);
		logger.debug("starting up VolunteerThread");
		new VolunteerThread().start();
		Thread.sleep(1000);
		logger.debug("starting up VIPThread");
		
		new VIPThread().start();
	}
	
	static class ServerThread extends Thread{
		@Override
		public void run() {
			setName("Server");
			try {
				new Server(PORT).start();
			} catch (BindException e) {
				e.printStackTrace();
			}
		}
	}
	
	static class VolunteerThread extends Thread{
		@Override
		public void run() {
			setName("Volunteer");
			try {
				synchronized(REQUEST_ID){
					while(REQUEST_ID.equals("")){
						try {
							REQUEST_ID.wait();
						} catch (InterruptedException e) {
						}
					}
				}
				
				Connection c = new Connection(HOST, PORT, new DummyVolunteerProtocolHandler(), VolunteerState.INITIALISATION);
				c.init(false);
				c.send(new Message(ProtocolName.ACCEPT_REQUEST).add(ProtocolField.REQUEST_ID, REQUEST_ID).add(ProtocolField.VOLUNTEER_ID, "abc"));
				
			} catch (UnknownHostException e) {
			}
		}
	}
	
	static class VIPThread extends Thread{
		@Override
		public void run() {
			setName("VIP");
			try {
				Connection c = new Connection(HOST, PORT, new DummyVIPProtocolHandler(), VIPState.IDLE);
				c.init(false);
				c.send(new Message(ProtocolName.REQUEST_HELP).add(ProtocolField.LATITUDE, 0d).add(ProtocolField.LONGITUDE, 0d));
			} catch (UnknownHostException e) {
			}
		}
	}
	
	private static void ssleep(int millis){
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
		}
	}
}
