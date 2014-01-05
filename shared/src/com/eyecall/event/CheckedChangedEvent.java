package com.eyecall.event;

import android.widget.RadioGroup;

import com.eyecall.connection.Named;
import com.eyecall.eventbus.Event;

public class CheckedChangedEvent extends Event {

	private RadioGroup group;
	private int checkedId;
	
	public CheckedChangedEvent(RadioGroup group, int checkedId, Named tag, Object data) {
		this(group, checkedId, tag.getName(), data);
	}

	public CheckedChangedEvent(RadioGroup group, int checkedId, String tag, Object data) {
		super(tag, data);
		this.group = group;
		this.checkedId = checkedId;
		this.tag = tag;
		this.data = data;
	}
	
	public RadioGroup getGroup(){
		return group;
	}
	
	public int getCheckedId(){
		return checkedId;
	}

}
