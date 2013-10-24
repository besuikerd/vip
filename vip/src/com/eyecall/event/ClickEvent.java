package com.eyecall.event;

import com.eyecall.connection.Named;
import com.eyecall.eventbus.Event;

import android.view.View;

public class ClickEvent extends Event{
	private View view;
	private String tag;
	private Object data;
	
	public ClickEvent(View view, Named tag, Object data){
		this(view, tag.getName(), data);
	}
	
	public ClickEvent(View view, String tag, Object data) {
		super(tag);
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