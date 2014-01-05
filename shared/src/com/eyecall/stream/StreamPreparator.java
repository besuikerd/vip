package com.eyecall.stream;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.media.MediaPlayer;

/**
 * prepares a stream to start streaming. The surface needs to be created and the
 * streaming side must message that it is ready
 * 
 * @author Nicker
 * 
 */
public class StreamPreparator {
	private static final Logger logger = LoggerFactory.getLogger(StreamPreparator.class);
	
	private CountDownLatch latch;
	private boolean surfaceReady;
	private boolean streamerReady;
	
	private String source;
	private MediaPlayer player;
	
	public StreamPreparator(MediaPlayer player) {
		latch = new CountDownLatch(2);
		this.player = player;
	}
	
	public void start(){
		new PreparationThread().start();
	}
	
	public void surfaceReady(){
		logger.debug("surface ready!");
		if(!surfaceReady){
			
			latch.countDown();
		}
		surfaceReady = true;
	}
	
	public void streamerReady(String source){
		logger.debug("streamer ready!");
		this.source = source;
		if(!streamerReady){
			latch.countDown();
		}
		streamerReady = true;
	}
	
	private class PreparationThread extends Thread{
		@Override
		public void run() {
			try {
				latch.await();
			} catch (InterruptedException e) {
			}
			
			logger.debug("preparator is ready!");
			
			try {
				player.setDataSource(source);
				player.prepareAsync();
			} catch (IOException e) {
			}
		}
	}
}
