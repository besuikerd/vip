package com.eyecall.push;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.eyecall.connection.Connection;
import com.eyecall.connection.Message;
import com.eyecall.protocol.ProtocolField;
import com.eyecall.protocol.ProtocolName;
import com.eyecall.volunteer.Constants;
import com.eyecall.volunteer.R;
import com.eyecall.volunteer.VolunteerProtocolHandler;
import com.eyecall.volunteer.VolunteerState;
import com.google.android.gcm.GCMRegistrar;
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
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		if (!prefs.contains(ProtocolField.VOLUNTEER_ID.getName())) {
			// get GCM key and register to server
			final GoogleCloudMessaging gcm = GoogleCloudMessaging
					.getInstance(context);

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
					try {
						// register at GCM
						GCMRegistrar.checkDevice(context);
						GCMRegistrar.checkManifest(context);
						String key = GCMRegistrar.getRegistrationId(context);
						if (key.equals("")) {
							GCMRegistrar.register(context, SENDER_KEY);
							key = GCMRegistrar.getRegistrationId(context);
							logger.debug("key obtained: {}", key);
						} else {
							logger.debug("Already registered");
						}
						// send registry key to server
						Connection c = new Connection(Constants.SERVER_URL,
								Constants.SERVER_PORT,
								new VolunteerProtocolHandler(),
								VolunteerState.INITIALISATION);
						c.init(false);
						c.send(new Message(ProtocolName.REGISTER).add(
								ProtocolField.VOLUNTEER_ID, key));
					} catch (IOException e) {
						logger.warn("Unable to register, error: {}", e);
					}
					return null;
				}

				protected void onPostExecute(String result) {
					dialog.dismiss();
					logger.debug("dialog dismissed");
				};
			}.execute();
		}
	}
}
