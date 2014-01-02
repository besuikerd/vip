package com.eyecall.event;

import android.widget.SeekBar;

import com.eyecall.eventbus.Event;
import com.fasterxml.jackson.databind.util.Named;

public class ProgressChangedEvent extends Event {

	private SeekBar seekBar;
	private int progress;
	private boolean fromUser;
	
	public ProgressChangedEvent(SeekBar seekBar, int progress, boolean fromUser, Named tag, Object data) {
		this(seekBar, progress, fromUser, tag.getName(), data);
	}

	public SeekBar getSeekBar() {
		return seekBar;
	}

	public int getProgress() {
		return progress;
	}

	public boolean isFromUser() {
		return fromUser;
	}

	public ProgressChangedEvent(SeekBar seekBar, int progress, boolean fromUser, String tag, Object data) {
		super(tag, data);
		this.seekBar = seekBar;
		this.progress = progress;
		this.fromUser = fromUser;
	}

}
