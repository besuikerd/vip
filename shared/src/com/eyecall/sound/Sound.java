package com.eyecall.sound;

import org.slf4j.LoggerFactory;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;

import com.eyecall.shared.R;

public enum Sound {
	CONNECTED(R.raw.connected),
	DISCONNECTED(R.raw.disconnected),
	CONNECTION_LOST(R.raw.connection_lost),
	
	;
	
	private int id;
	
	private Sound(int id) {
		this.id = id;
	}
	
	public void play(Context context){
		getInstance(context).play(id, 1f, 1f, 0, 0, 1f);
	}
	
	private static SoundPool instance;
	
	public static SoundPool getInstance(Context ctx){
		if(instance == null){
			instance = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
			
			instance.setOnLoadCompleteListener(new OnLoadCompleteListener() {
				
				@Override
				public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
					LoggerFactory.getLogger(Sound.class).debug("load complete!");
				}
			});
			
			for(Sound s : values()){
				s.id = instance.load(ctx, s.id, 0);
			}
			
		}
		return instance;
	}
	


}
