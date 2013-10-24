package com.eyecall.eventbus;

public class Event {
	private String tag;

	public Event(String tag) {
		this.tag = tag;
	}

	public String getTag() {
		return tag;
	}
}
