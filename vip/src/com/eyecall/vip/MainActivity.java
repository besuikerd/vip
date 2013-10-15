package com.eyecall.vip;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


public class MainActivity extends Activity {
	
	public static final String TAG = "Eyecall VIP";
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.openHelpActivity();
	}
	
	public void openHelpActivity(){
		Intent intent = new Intent(getApplicationContext(), HelpActivity.class);
		this.startActivity(intent);
	}
	
}
