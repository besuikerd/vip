package com.eyecall.protocol;

import com.eyecall.connection.Named;

public enum ProtocolField implements Named{
	//TODO add all protocol fields
	
	KEY("key")
	
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
