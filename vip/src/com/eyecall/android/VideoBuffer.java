package com.eyecall.android;

import java.io.IOException;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;


public class VideoBuffer {
	public VideoBuffer(LocalSocketAddress localSocketAddress) {
		this.localSocketAddress = localSocketAddress;
	}

	private LocalSocket localSocket;
	private LocalSocketAddress localSocketAddress;

	/** 
	 * This VideoBuffer will connect to the corresponding LocalServerSocket
	 * @throws IOException 
	 */
	public void start() throws IOException {
		localSocket = new LocalSocket();
		localSocket.connect(localSocketAddress);
	}
	

	public void stop() throws IOException {
		localSocket.close();		
	}
	
	
}
