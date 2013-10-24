package com.eyecall.server;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;

import com.eyecall.connection.Connection;

public class Server {
	
	private int port;	
	
	public Server(int port){
		this.port = port;
	}
	
	
	public void start() throws BindException{
		ServerSocket s = null;
		try{
			s = new ServerSocket(port);
		} catch(BindException e){
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(s != null){
			while(true){
				try{
					new Connection(s.accept(), new ServerProtocolHandler(), ServerState.WAITING).init();
				} catch(IOException e){
					e.printStackTrace();
					try {
						s.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}
	
	public static void main(String[] args){
		if(args.length == 1){
			int port = -1;
			try{
				port = Integer.parseInt(args[0]);
			} catch(NumberFormatException e){
				System.err.printf("Argument \"%s\" is not a number", args[0]);
				System.exit(1);
			}
			if(port < 0 || port > 65536){
				System.err.println("port number must be between 0 and 65536");
				System.exit(1);
			}
			try{
				new Server(port).start();
			} catch(BindException e){
				System.err.printf("Could not bind to port %d", port);
			}
		} else{
			System.err.println("Usage: [port]");
			System.exit(1);
		}
	}
}
