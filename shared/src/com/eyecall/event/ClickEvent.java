package com.eyecall.event;

import android.view.View;

public class ClickEvent {
	private View view;
	private String tag;
	private Object data;
	
	public ClickEvent(View view, String tag, Object data) {
		this.view = view;
		this.tag = tag;
		this.data = data;
	}
	
	public Object getData() {
		return data;
	}
	
	public String getTag() {
		return tag;
	}
	
	public View getView() {
		return view;
	}
}