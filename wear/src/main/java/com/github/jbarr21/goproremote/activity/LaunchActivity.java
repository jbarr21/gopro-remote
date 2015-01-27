package com.github.jbarr21.goproremote.activity;

import android.app.Activity;
import android.os.Bundle;

import com.github.jbarr21.goproremote.util.WearNotificationManager;

public class LaunchActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WearNotificationManager wearNotificationManager = WearNotificationManager.from(this);
        wearNotificationManager.showStatusNotification("GoPro Remote", "Ready for control");
        finish();
    }
}
