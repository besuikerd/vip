package com.eyecall.vip;

import java.util.ArrayDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

public class Speech implements OnInitListener {
	private static final Logger logger = LoggerFactory.getLogger(Speech.class);
	
	private TextToSpeech speech;
	private static Speech instance;

	private ArrayDeque<String> queue;

	private boolean initialized;
	
	private Speech(Context context){
		logger.debug("Speech created");
		this.queue = new ArrayDeque<String>();
		this.initialized = false;
		speech = new TextToSpeech(context, this);
	}
	
	public static Speech getInstance(Context context){
		logger.debug("speech instance: {}", instance);
		if(instance==null) instance = new Speech(context);
		return instance;
	}
	
	public void close() {
		speech.shutdown();
	}
	
	private void run(){
		if(initialized){
			speak();
		}
	}
	
	private void speak(){
		while(!queue.isEmpty()){
			logger.debug("Speaking: {}", queue.peek());
			speech.speak(queue.poll(), TextToSpeech.QUEUE_ADD, null);
		}
	}
	
	@Override
	public void onInit(int arg) {
		if(arg==TextToSpeech.SUCCESS){
			logger.info("Speech initialized!");
			initialized = true;
			speak();
		}else{
			logger.warn("Unable to init speech!");
		}
		close();
	}

	public void requestingHelp(){
		logger.info("Speech adding to queue: {}",  "requesting help");
		queue.add("requesting help");
		run();
	}

	public void connected(){
	}
}
