package com.eyecall.protocol;

import com.eyecall.connection.Named;

public enum ProtocolField implements Named{
	//TODO add all protocol fields
	
	ACTION("action"), 
	ACTION_ADD("add"), 
	ACTION_DELETE("delete"),
	ERROR_CODE("error_code"),
	ERROR_MESSAGE("error_message"),
	KEY("key"),
	LATITUDE("latitude"),
	LONGITUDE("longitude"), 
	RADIUS("radius"),
	REQUEST_ID("request_id"), 
	TYPE("type"), 
	TYPE_NON_PREFERRED("non-preferred"), 
	TYPE_PREFERRED("preferred"), 
	VOLUNTEER_ID("volunteer_id"), 
	LOCATIONS("locations")
	
	;
	
	public static ProtocolField lookup(String name){
		for(ProtocolField p : values()){
			if(p.name.equals(name)){
				return p;
			} 
		}
		return null;
	}
	private String name;

	private ProtocolField(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
