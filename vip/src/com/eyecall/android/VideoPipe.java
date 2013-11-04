package com.eyecall.android;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;

/**
 * Class representing a pipe for video data
 */
public class VideoPipe {
	private LocalServerSocket localServerSocket;
	private LocalSocket inSocket, outSocket;
	private String address;
	
	public VideoPipe(String address){
		this.address = address;
	}
	
	public void setup() throws IOException{
		localServerSocket = new LocalServerSocket(address);
		inSocket = new LocalSocket();
		inSocket.connect(new LocalSocketAddress(address));
		outSocket = localServerSocket.accept();
	}
	
	public void close() throws IOException{
		inSocket.close();
		outSocket.close();
		localServerSocket.close();
		localServerSocket = null;
		inSocket = null;
		outSocket = null;
	}
	
	/**
	 * Returns the InputStream for the output of this pipe
	 * (output of this pipe is input for receiver)
	 * @return The output of this pipe
	 * @throws IOException
	 */
	public InputStream getOutput() throws IOException{
		return outSocket.getInputStream();
	}
	
	/**
	 * Returns the OutputStream for the input of this pipe
	 * (input of this pipe is output for sender)
	 * @return The input of this pipe
	 * @throws IOException
	 */
	public OutputStream getInput() throws IOException{
		return inSocket.getOutputStream();
	}
	
	public FileDescriptor getInputFileDescriptor(){
		return inSocket.getFileDescriptor();
	}
	
	public FileDescriptor getOutputFileDescriptor(){
		return outSocket.getFileDescriptor();
	}
	
}
