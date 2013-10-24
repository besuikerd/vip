package com.eyecall.event;

import com.eyecall.connection.Named;
import com.eyecall.eventbus.Event;

public class RequestGrantedEvent extends Event {

	public RequestGrantedEvent(String tag) {
		super(tag);
	}

	public RequestGrantedEvent(Named named) {
		this(named.getName());
	}

}
