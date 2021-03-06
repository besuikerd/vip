package com.eyecall.eventbus;

import android.content.DialogInterface;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.eyecall.connection.Named;
import com.eyecall.event.CheckedChangedEvent;
import com.eyecall.event.ClickEvent;
import com.eyecall.event.DialogClickEvent;
import com.eyecall.event.MenuItemClickEvent;
import com.eyecall.event.ProgressChangedEvent;

public class InputEventListener implements OnClickListener, android.content.DialogInterface.OnClickListener, OnCheckedChangeListener, OnSeekBarChangeListener, OnMenuItemClickListener{
	
	private String tag;
	private Object data;
	
	public InputEventListener(String tag, Object data) {
		this.data = data;
		this.tag = tag;
	}

	public InputEventListener(Named n, Object data) {
		this(n.getName(), data);
	}
	
	public InputEventListener(String tag) {
		this(tag, null);
	}
	
	public InputEventListener(Named tag) {
		this(tag.getName());
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		EventBus.getInstance().post(new DialogClickEvent(dialog, tag, which, data));
	}

	@Override
	public void onClick(View v) {
		EventBus.getInstance().post(new ClickEvent(v, tag, data));
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		EventBus.getInstance().post(new CheckedChangedEvent(group, checkedId, tag, data));
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		EventBus.getInstance().post(new ProgressChangedEvent(seekBar, progress, fromUser, tag, data));
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		EventBus.getInstance().post(new MenuItemClickEvent(item, tag, data));
		return false;
	}

}
