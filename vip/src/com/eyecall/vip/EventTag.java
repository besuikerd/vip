package com.eyecall.vip;

import com.eyecall.connection.Named;

public enum EventTag implements Named{
	REQUEST_BUTTON_PRESSED("request_button"),
	VIDEO_FRAME("video_frame"), 
	REQUEST_GRANTED("request_granted"), 
	REQUEST_DENIED("request_denied"),
	SURFACE_CREATED("surface_created"),
	
	DISCONNECT("disconnect"),
	MEDIA_READY("media_ready"),
	
	BUTTON_DISCONNECT("button_disconnect"),
	
	SOUND_CONNECTION_LOST("sound_connection_lost"),
	SOUND_CONNECTED("sound_connected"),
	SOUND_DISCONNECTED("sound_disconnected"),
	;
	
	private String tag;
	
	private EventTag(String tag){
		this.tag = tag;
	}
	
	@Override
	public String toString(){
		return getName();
	}
	
	@Override
	public String getName(){
		return this.tag;
	}
	
	public static EventTag lookup(String tag){
		for(EventTag t : values()){
			if(t.getName().equals(tag)){
				return t;
			}
		}
		return null;
	}
}
