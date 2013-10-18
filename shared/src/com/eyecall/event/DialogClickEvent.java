package com.eyecall.event;

import android.content.DialogInterface;

public class DialogClickEvent {
	private DialogInterface dialog;
	private String tag;
	private int which;
	private Object data;

	public DialogClickEvent(DialogInterface dialog, String tag, int which, Object data) {
		this.dialog = dialog;
		this.tag = tag;
		this.which = which;
		this.data = data;
	}

	public DialogInterface getDialog() {
		return dialog;
	}

	public String getTag() {
		return tag;
	}

	public int getWhich() {
		return which;
	}
	
	public Object getData() {
		return data;
	}
}
