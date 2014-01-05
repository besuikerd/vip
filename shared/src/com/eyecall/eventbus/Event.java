package com.eyecall.eventbus;

import com.eyecall.connection.Named;

public class Event {
	protected String tag;
	protected Object data;

	public Event(String tag) {
		this(tag, null);
	}
	
	public Event(String tag, Object data) {
		this.tag = tag;
		this.data = data;
	}
	
	public Event(Named named){
		this(named, null);
	}
	
	public Event(Named named, Object data){
		this(named.getName(), data);
	}

	public String getTag() {
		return tag;
	}
	
	public Object getData() {
		return data;
	}
}
