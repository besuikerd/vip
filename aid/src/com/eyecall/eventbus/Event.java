package com.eyecall.eventbus;

import com.eyecall.connection.Named;

public class Event {
	private String tag;

	public Event(String tag) {
		this.tag = tag;
	}
	
	public Event(Named named){
		this.tag = named.getName();
	}

	public String getTag() {
		return tag;
	}
}
