package com.eyecall.aid;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		/* BUTTONS */
		Button toevoegen = (Button) findViewById(R.id.button1);
		Button mijnLocaties = (Button) findViewById(R.id.button3);
		
		toevoegen.setOnClickListener(new OnClickListener(){
			@Override 
			public void onClick(View arg0) {
				// TODO acties van locatie toevoegen/verwijderen
				Intent i = new Intent(getApplicationContext(),MainActivity.class);
			}
		});
		
		mijnLocaties.setOnClickListener(new OnClickListener(){
			@Override 
			public void onClick(View arg0) {
				Intent i = new Intent(getApplicationContext(),LocationsActivity.class);
			}
		});
		
		/* DIALOGS */
		// TODO verplaatsen naar dialoggedeelte
		new AlertDialog.Builder(this).setTitle("Heeft u kunnen helpen?").setView(getLayoutInflater().inflate(R.layout.dialog_success,null));
		new AlertDialog.Builder(this).setTitle("Wilt u vaker oproepen in de buurt van deze locatie ontvangen?").setView(getLayoutInflater().inflate(R.layout.dialog_preferred,null));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}