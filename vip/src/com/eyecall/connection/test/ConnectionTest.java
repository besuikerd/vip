package com.eyecall.connection.test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.eyecall.connection.Connection;
import com.eyecall.connection.Message;


public class ConnectionTest {
	public static void main(String[] args) throws IOException, InterruptedException {
		new Thread(){
			public void run() {
				try {
					
					//open socket
					@SuppressWarnings("resource")
					ServerSocket srv = new ServerSocket(5000);
					Socket s = srv.accept();
					
					//wrap Connection around the Socket
					Connection c = new Connection(s, new TestProtocolHandler(), TestState.AWAITING_CONNECTION);
					//initialize the connection
					c.init();
				} catch (IOException e) {
					e.printStackTrace();
				}
			};
		}.start();
		
		//allow ServerSocket Thread to initialize
		Thread.sleep(100);
		
		//connect to the ServerSocket
		Socket s = new Socket("localhost", 5000);
		
		//wrap a Connection around the Socket
		Connection c = new Connection(s, new TestProtocolHandler(), TestState.AWAITING_CONNECTION);
		
		//initialize the connection
		c.init();
		
		//send the first message
		c.send(new Message("hello").add("hello", "pieter"));
		
		//send a few more messages
		for(int i = 0 ; i < 10 ; i++){
			c.send(new Message("periodic_message").add("testModel", new TestModel("kees", "keesserson", "kees@keesmail.com")));
			c.send(new Message("bytearray").add("data", new byte[]{1,2,3,4,5,6}));
			Thread.sleep(100);
		}
		
		//send a close message
		c.send(new Message("close").add("close", true));
		
		//message after disconnect message, socket is still opened
		c.send(new Message("after_disconnect"));
		
		//close the socket (blocks until OutQueue is being emptied)
		c.close();
	}
}
