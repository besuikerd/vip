package com.eyecall.push;

import android.content.Context;

public class GcmRegistrationReceiver extends
        com.google.android.gcm.GCMBroadcastReceiver {

    @Override
    protected String getGCMIntentServiceClassName(Context context) {
        return "com.eyecall.push.GcmRegistrationIntentService";
    }
}