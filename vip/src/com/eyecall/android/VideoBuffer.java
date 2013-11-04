package com.eyecall.android;

import java.io.IOException;
import java.io.InputStream;


public class VideoBuffer extends Thread {
	private VideoPipe videoPipe;

	public VideoBuffer(VideoPipe videoPipe) {
		this.videoPipe = videoPipe;
	}
	
	@Override
	public void run(){
		try {
			InputStream output = videoPipe.getOutput();
			// Todo : read video frame and send it via UDP
			//output.read
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
}
