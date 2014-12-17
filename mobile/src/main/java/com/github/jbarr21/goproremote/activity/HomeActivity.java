package com.github.jbarr21.goproremote.activity;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;

import com.github.jbarr21.goproremote.R;
import com.github.jbarr21.goproremote.data.ConfigStorage;
import com.github.jbarr21.goproremote.fragment.HomeFragment;

public class HomeActivity extends ActionBarActivity {

    private WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag(HomeFragment.class.getSimpleName()) == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new HomeFragment(), HomeFragment.class.getSimpleName())
                    .commit();
        }
    }

    private void connectToGoProWifi() {
        ConfigStorage configStorage = new ConfigStorage(this);
        String ssid = configStorage.getWifiSsid();
        String key = configStorage.getWifiPassword();

        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", ssid);
        wifiConfig.preSharedKey = String.format("\"%s\"", key);

        //remember id
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        int netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();
    }
}
