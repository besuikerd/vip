package com.eyecall.event;

import com.eyecall.eventbus.Event;

public class VideoFrameEvent extends Event{
	
	private byte[] frame;

	public VideoFrameEvent(String tag, byte[] frame) {
		super(tag);
		this.frame = frame;
	}
	
	public byte[] getFrame(){
		return frame;
	}

}
