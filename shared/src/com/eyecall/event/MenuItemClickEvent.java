package com.eyecall.event;

import android.view.MenuItem;

import com.eyecall.eventbus.Event;

public class MenuItemClickEvent extends Event{
	
	private MenuItem item;

	public MenuItemClickEvent(MenuItem item, String tag, Object data) {
		super(tag, data);
		this.item = item;
	}
	
	public MenuItem getItem() {
		return item;
	}

}
