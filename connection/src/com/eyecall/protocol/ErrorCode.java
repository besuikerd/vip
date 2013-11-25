package com.eyecall.protocol;

import com.eyecall.connection.Named;

public enum ErrorCode implements Named{
	UNKNOWN_COMMAND(100),
	UNEXPECTED_COMMNAD(101),
	INVALID_ARGUMENTS(102),
	INVALID_VOLUNTEER_ID(103)
	;
	
	private String name;
	private int code;
	private ErrorCode(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static ErrorCode lookup(String name){
		for(ErrorCode p : values()){
			if(p.name.equals(name)){
				return p;
			} 
		}
		return null;
	}
	
	@Override
	public String toString() {
		return getName();
	}

	@Override
	public String getName() {
		return String.valueOf(code);
	}
}
