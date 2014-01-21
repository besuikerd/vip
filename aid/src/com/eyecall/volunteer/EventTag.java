package com.eyecall.volunteer;

import com.eyecall.connection.Named;

public enum EventTag implements Named{
	ACCEPT_REQUEST("accept_request"), 
	ADD_LOCATION("add_location"), 
	CANCEL_LOCATION_ADD("cancel_location_add"),
	EDIT_LOCATION("edit_location"), 
	ID_ACCEPTED("id_accepted"),
	ID_INVALID("id_invalid"),
	ID_REJECTED("id_rejected"),
	LOCATION_ADDED("location_added"),
	LOCATION_UPDATE("location_update"),
	LOCATIONS_RECEIVED("locations_received"),
	REFRESH_LOCATIONS("refresh_locations"), 
	REJECT_REQUEST("reject_request"), 
	REMOVE_LOCATION("remove_location"),
	REMOVE_LOCATION_CONFIRM("remove_location_confirm"), 
	REQUEST_ACKNOWLEDGED("request_acknowledged"),
	BUTTON_DISCONNECT("button_disconnect"),
	
	LOCATION_HELP("location_help"),
	
	//REQUEST_DENIED("request_denied"), 
	SAVE_LOCATION("save_location"), 
	REQUEST_CANCELLED("request_cancelled"), 
	LOCATION_PREFERRED_CHANGED("loc_pref_change"), 
	LOCATION_RADIUS_CHANGED("loc_rad_change"),
	DISCONNECTED("disconnected"),
	MEDIA_READY("media_ready"),
	
	NOTHING("")
	;
	
	
	public static EventTag lookup(String tag){
		for(EventTag t : values()){
			if(t.getName().equals(tag)){
				return t;
			}
		}
		return NOTHING;
	}
	
	private String tag;
	
	private EventTag(String tag){
		this.tag = tag;
	}
	
	@Override
	public String getName(){
		return this.tag;
	}
	
	@Override
	public String toString(){
		return getName();
	}
}
