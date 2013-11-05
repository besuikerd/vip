package com.eyecall.volunteer;

import com.eyecall.connection.Named;

public enum EventTag implements Named{
	ADD_LOCATION("add_location"), 
	REMOVE_LOCATION("remove_location");
	
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
}
