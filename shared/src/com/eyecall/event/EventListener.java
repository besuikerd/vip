package com.eyecall.event;

import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;

import com.eyecall.connection.Named;

import de.greenrobot.event.EventBus;

public class EventListener implements OnClickListener, android.content.DialogInterface.OnClickListener{
	
	private String tag;
	private Object data;
	
	public EventListener(String tag, Object data) {
		this.data = data;
		this.tag = tag;
		
	}

	public EventListener(Named n, Object data) {
		this(n.getName(), data);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		EventBus.getDefault().post(new DialogClickEvent(dialog, tag, which, data));
	}

	@Override
	public void onClick(View v) {
		EventBus.getDefault().post(new ClickEvent(v, tag, data));
	}

}
