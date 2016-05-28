package com.github.jbarr21.goproremote.ui;

import android.os.Bundle;

import com.github.jbarr21.goproremote.ui.notification.WearNotificationManager;

public class LaunchActivity extends GoProActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WearNotificationManager wearNotificationManager = WearNotificationManager.from(this);
        wearNotificationManager.showStatusNotification("GoPro Remote", "Ready for control");
        finish();
    }
}
