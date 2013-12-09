package com.eyecall.protocol;

import com.eyecall.connection.Named;

public enum ProtocolField implements Named{
	//TODO add all protocol fields
	
	KEY("key"), 
	LONGITUDE("longitude"), 
	LATITUDE("latitude"),
	REQUEST_ID("request_id"),
	VOLUNTEER_ID("volunteer_id"),
	LOCATIONS("locations"),
	ERROR_CODE("error_code"),
	ERROR_MESSAGE("error_message"),
	
	ACTION("action"), 
	ACTION_ADD("add"), 
	ACTION_DELETE("delete"),
	RADIUS("radius"),
	TYPE("type"), 
	TYPE_NON_PREFERRED("non-preferred"), 
	TYPE_PREFERRED("preferred"), 
	LOCATION_ID("location_id"), 
	
	;
	
	private String name;
	private ProtocolField(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static ProtocolField lookup(String name){
		for(ProtocolField p : values()){
			if(p.name.equals(name)){
				return p;
			} 
		}
		return null;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
