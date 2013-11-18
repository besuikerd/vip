package com.eyecall.volunteer;

import com.eyecall.connection.Named;

public enum EventTag implements Named{
	ADD_LOCATION("add_location"), 
	REMOVE_LOCATION("remove_location"), 
	CANCEL_LOCATION_ADD("cancel_location_add"), 
	SAVE_LOCATION("save_location"),
	
	ACCEPT_REQUEST("accept_request"),
	REJECT_REQUEST("reject_request"),
	
	REQUEST_ACKNOWLEDGED("request_acknowledged"),
	REQUEST_DENIED("request_denied"),
	
	ID_ACCEPTED("id_accepted"),
	ID_REJECTED("id_rejected");
	
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
