package com.eyecall.server;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyecall.connection.Connection;

public class Server {
	private static final Logger logger = LoggerFactory.getLogger(Server.class);
	
	/** Connection instance for test purpose */
	private ServerSocket serverSocket;
	public ServerSocket getServerSocket(){
		return serverSocket;
	}
	
	
	private int port;
	
	public Server(int port){
		this.port = port;
	}
	
	
	public void start() throws BindException{
		try{
			serverSocket = new ServerSocket(port);
			logger.debug("Server started listening to port {}", port);
		} catch(BindException e){
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(serverSocket != null){
			while(true){
				try{
					new Connection(serverSocket.accept(), new ServerProtocolHandler(), ServerState.WAITING).init(true);
				} catch(IOException e){
					e.printStackTrace();
					try {
						serverSocket.close();
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
