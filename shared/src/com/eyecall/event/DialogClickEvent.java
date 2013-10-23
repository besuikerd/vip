package com.eyecall.event;

import com.eyecall.connection.Named;
import com.eyecall.eventbus.Event;

import android.content.DialogInterface;

public class DialogClickEvent extends Event{
	private DialogInterface dialog;
	private int which;
	private Object data;
	
	public DialogClickEvent(DialogInterface dialog, Named tag, int which, Object data) {
		this(dialog, tag.getName(), which, data);
	}

	public DialogClickEvent(DialogInterface dialog, String tag, int which, Object data) {
		super(tag);
		this.dialog = dialog;
		this.which = which;
		this.data = data;
	}

	public DialogInterface getDialog() {
		return dialog;
	}

	public int getWhich() {
		return which;
	}
	
	public Object getData() {
		return data;
	}
}
