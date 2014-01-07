package com.eyecall.push;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.eyecall.volunteer.R;
import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class PushRegistration {

	private static final Logger logger = LoggerFactory
			.getLogger(PushRegistration.class);

	public static final String SENDER_KEY = "229276904265";
	private Context context;

	public PushRegistration(Context context) {
		this.context = context;
	}

	public void register() {
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
		if(status == ConnectionResult.SUCCESS){
			// Request id in background
			new AsyncTask<Void, Void, String>() {
	
				private ProgressDialog dialog;
	
				protected void onPreExecute() {
					this.dialog = new ProgressDialog(context);
					dialog.setTitle(R.string.registering);
					dialog.setCancelable(false);
					dialog.setMessage(context.getString(R.string.wait));
					dialog.show();
				};
	
				@Override
				protected String doInBackground(Void... params) {
					logger.debug("Starting registration...");
					GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
					String key = null;
					try {
						// Register via new method
						key = gcm.register(SENDER_KEY);
						logger.debug("GCM key obtained: {}", key);
						if(key!=null && key.length()>0){
//							VolunteerProtocolHandler.sendKeyToServer(key);
						}else{
							logger.error("Key is not valid, not sending to server");
						}
					} catch (IOException e) {
						// Device is too old. Try deprecated method
						logger.warn("Unable to register, error: {}", e);
						logger.warn("Trying deprecated method...");
						
						try{
							GCMRegistrar.checkDevice(context);
							GCMRegistrar.checkManifest(context);
							logger.debug("Device and manifest are valid");
							
							GCMRegistrar.register(context, SENDER_KEY);
							// GcmRegistrationIntentService will receive key
						}catch(Exception e2){
							logger.warn("Device or manifest is not valid: {}", e2);
						}
						
					}				
					logger.debug("... registration end");
					return null;
				}
	
				protected void onPostExecute(String result) {
					dialog.dismiss();
					logger.debug("dialog dismissed");
				};
			}.execute();
		}else{
			// Google Play Services unavailable
			logger.warn("Unable to register, GooglePlayServices unavailable:");
			if(status==ConnectionResult.SERVICE_MISSING){
				logger.warn("SERVICE_MISSING");
			}else if(status==ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED){
				logger.warn("SERVICE_VERSION_UPDATE_REQUIRED");
			}else if(status==ConnectionResult.SERVICE_DISABLED){
				logger.warn("SERVICE_DISABLED");
			}else if(status==ConnectionResult.SERVICE_INVALID){
				logger.warn("SERVICE_INVALID");
			}
		}
	}
}
