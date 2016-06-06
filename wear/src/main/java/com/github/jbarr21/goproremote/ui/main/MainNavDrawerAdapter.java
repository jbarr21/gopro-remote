package com.github.jbarr21.goproremote.ui.main;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.wearable.view.drawer.WearableNavigationDrawer.WearableNavigationDrawerAdapter;

import com.github.jbarr21.goproremote.GoProRemoteApp;
import com.github.jbarr21.goproremote.R;


public class MainNavDrawerAdapter extends WearableNavigationDrawerAdapter {
    private final ScreenSelectedListener screenSelectedListener;

    public MainNavDrawerAdapter(ScreenSelectedListener screenSelectedListener) {
        this.screenSelectedListener = screenSelectedListener;
    }

    @Override
    public void onItemSelected(int index) {
        screenSelectedListener.onScreenSelected(Screen.values()[index]);
    }

    @Override
    public Drawable getItemDrawable(int index) {
        @DrawableRes int resId = 0;
        switch (Screen.values()[index]) {
            case POWER_TOGGLE:  resId = R.drawable.ic_power_settings_new_white_48dp; break;
            case RECORD_TOGGLE: resId = R.drawable.ic_videocam_white_48dp; break;
            case INFO:          resId = R.drawable.ic_info_white_48dp; break;
            case WIFI:          resId = R.drawable.ic_network_wifi_white_48dp; break;
        }
        Resources res = GoProRemoteApp.getInstance().getResources();
        return resId != 0 ? res.getDrawable(resId) : null;
    }

    @Override
    public String getItemText(int index) {
        switch (Screen.values()[index]) {
            case POWER_TOGGLE:  return "Power Toggle";
            case RECORD_TOGGLE: return "Record Toggle";
            case INFO:          return "Info";
            case WIFI:          return "Wi-Fi";
            default:            return null;
        }
    }

    @Override
    public int getCount() {
        return Screen.values().length;
    }

    public enum Screen {
        POWER_TOGGLE, RECORD_TOGGLE, INFO, WIFI
    }

    public interface ScreenSelectedListener {
        void onScreenSelected(Screen screen);
    }
}
