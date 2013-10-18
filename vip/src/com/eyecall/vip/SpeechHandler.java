package com.eyecall.vip;

import java.util.Queue;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

public class SpeechHandler implements OnInitListener {

	private TextToSpeech textToSpeech;
	private Context context;
	
	private Queue<String> queue;
	
	public SpeechHandler(Context context){
		this.context = context;
	}
	
	public void speak(String message){
		queue.add(message);
	}
	
	@Override
	public void onInit(int arg0) {
		// TODO Auto-generated method stub
		
	}

}
