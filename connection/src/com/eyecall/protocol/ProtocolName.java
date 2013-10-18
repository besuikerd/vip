package com.eyecall.protocol;

import com.eyecall.connection.Named;

public enum ProtocolName implements Named{
	
	//TODO add all protocol names
	
	OBTAIN_KEY("obtain_key"),
	ASSIGN_KEY("assign_key")
	
	
	;
	
	
	
	private String name;
	private ProtocolName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static ProtocolName lookup(String name){
		for(ProtocolName p : values()){
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
