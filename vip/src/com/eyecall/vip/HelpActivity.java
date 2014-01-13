package com.eyecall.vip;

import java.io.IOException;

import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.rtsp.RtspServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.widget.Button;

import com.eyecall.connection.Connection;
import com.eyecall.connection.Message;
import com.eyecall.eventbus.Event;
import com.eyecall.eventbus.EventBus;
import com.eyecall.eventbus.EventListener;
import com.eyecall.eventbus.InputEventListener;
import com.eyecall.protocol.ProtocolField;
import com.eyecall.protocol.ProtocolName;
import com.eyecall.sound.Sound;

public class HelpActivity extends Activity implements EventListener, SurfaceHolder.Callback, OnPreparedListener, SurfaceTextureListener{
	private static final Logger logger = LoggerFactory.getLogger(HelpActivity.class);
	
	private SurfaceView surfaceView;
	private TextureView textureView;
	
	private MediaPlayer player;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		
		player = new MediaPlayer();
		player.reset();
		player.setOnPreparedListener(this);
		
		textureView = (TextureView) findViewById(R.id.textureView);
		textureView.setSurfaceTextureListener(this);
		
		surfaceView = (SurfaceView) findViewById(R.id.surface);
		surfaceView.getHolder().addCallback(this);
		
		
		((Button) findViewById(R.id.button_disconnect)).setOnClickListener(new InputEventListener(EventTag.BUTTON_DISCONNECT, null));
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		EventBus.getInstance().subscribe(this);
	}
	
	@Override
	public void onEvent(Event e) {
		switch(EventTag.lookup(e.getTag())){
		case DISCONNECT:
			if(ConnectionInstance.hasInstance()){
				ConnectionInstance.getExistingInstance().send(new Message(ProtocolName.DISCONNECT));
			}
			
			finish();
			break;
			
		case BUTTON_DISCONNECT:
			Sound.DISCONNECTED.play(this);
			EventBus.getInstance().post(new Event(EventTag.DISCONNECT));
			break;
		case MEDIA_READY:
			String ip = e.getData().toString();
			logger.debug("preparing audio with {}...", ip);
			try{
				player.setDataSource(String.format("rtsp://%s:8086", ip));
				player.prepareAsync();
			} catch(IOException ex){
				logger.debug("failed to prepare audio: {}", ex.getMessage());
				ex.printStackTrace();
			}
			break;
		case SOUND_CONNECTION_LOST:
			Sound.CONNECTION_LOST.play(this);
			break;
		case SOUND_DISCONNECTED:
			Sound.DISCONNECTED.play(this);
			
		
			
		default:
			break;
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopService(new Intent(this, RtspServer.class));
		if(player != null && player.isPlaying()){
			player.stop();
			player.release();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		EventBus.getInstance().unsubscribe(this);
	}

	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
			int height) {
		
	}

	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
		return false;
	}

	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
			int height) {
		
	}

	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {
		
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		logger.debug("mediaplayer prepared! starting...");
		mp.start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		logger.debug("surface created!!");
		new Thread(){
			public void run() {
				try {
					SessionBuilder.getInstance()
							.setSurfaceHolder(((SurfaceView) findViewById(R.id.surface)).getHolder())
							.setContext(getApplicationContext())
							.setAudioEncoder(SessionBuilder.AUDIO_AAC)
							.setVideoEncoder(SessionBuilder.VIDEO_H264)
							.build();
							startService(new Intent(getApplicationContext(), RtspServer.class));
							
							Connection c;
							if((c = ConnectionInstance.getExistingInstance()) != null){
								WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
								@SuppressWarnings("deprecation")
								String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
								c.send(new Message(ProtocolName.MEDIA_READY).add(ProtocolField.IP, ip));
							} else{
								logger.warn("hmm no existing instance for connection?");
							}
				} catch (IOException e) {
					e.printStackTrace();
				}
			};
		}.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		
	}
}
