package com.eyecall.event;

import com.eyecall.connection.Named;
import com.eyecall.eventbus.Event;

import android.view.View;

public class ClickEvent extends Event{
	private View view;
	
	public ClickEvent(View view, Named tag, Object data){
		this(view, tag.getName(), data);
	}
	
	public ClickEvent(View view, String tag, Object data) {
		super(tag, data);
		this.view = view;
		this.tag = tag;
		this.data = data;
	}
	
	public View getView() {
		return view;
	}
}
