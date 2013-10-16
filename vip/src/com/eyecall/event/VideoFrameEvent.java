package com.eyecall.event;

public class VideoFrameEvent {
	
	private byte[] frame;

	public VideoFrameEvent(byte[] frame) {
		this.frame = frame;
	}
	
	public byte[] getFrame(){
		return frame;
	}

}
