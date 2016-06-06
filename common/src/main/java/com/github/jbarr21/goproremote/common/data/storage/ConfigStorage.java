package com.github.jbarr21.goproremote.common.data.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import javax.inject.Inject;

public class ConfigStorage {

    private static final String KEY_WIFI_SSID = "ssid";
    private static final String KEY_WIFI_PASSWORD = "password";

    private static final String DEFAULT_PASSWORD = "goprohero";

    private SharedPreferences prefs;

    @Inject
    public ConfigStorage(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    public String getWifiSsid() {
        return prefs.getString(KEY_WIFI_SSID, null);
    }

    public boolean saveWifiSsid(String ssid) {
        return prefs.edit()
                .putString(KEY_WIFI_SSID, ssid)
                .commit();
    }

    public String getWifiPassword() {
        return prefs.getString(KEY_WIFI_PASSWORD, null);
    }

    public String getWifiPasswordOrDefault() {
        return prefs.getString(KEY_WIFI_PASSWORD, DEFAULT_PASSWORD);
    }

    public boolean saveWifiPassword(String password) {
        return prefs.edit()
                .putString(KEY_WIFI_PASSWORD, password)
                .commit();
    }
}
