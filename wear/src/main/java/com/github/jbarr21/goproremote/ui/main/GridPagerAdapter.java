package com.github.jbarr21.goproremote.ui.main;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.text.TextUtils;

import com.github.jbarr21.goproremote.R;
import com.github.jbarr21.goproremote.ui.controls.ControlFragment;
import com.github.jbarr21.goproremote.ui.modes.ModeFragment;
import com.github.jbarr21.goproremote.ui.main.WearActionFragment.OnActionClickedListener;
import com.github.jbarr21.goproremote.ui.notification.WearNotificationManager;

public class GridPagerAdapter extends FragmentGridPagerAdapter {

    private static final int NUM_ROWS = 1;
    private static final int NUM_COLS = 4;

    private OnActionClickedListener wifiActionClickedListener;
    private CharSequence status;

    public GridPagerAdapter(FragmentManager fm) {
        super(fm);
        wifiActionClickedListener = new WifiActionClickListener();
    }

    @Override
    public Fragment getFragment(int row, int col) {
        if (row == 0) {
            switch (col) {
                case 0:
                    CardFragment cardFragment = CardFragment.create("GoPro Remote", !TextUtils.isEmpty(status) ? status : "Ready for control");
                    cardFragment.setCardMarginBottom(8);
                    return cardFragment;
                case 1: return new ControlFragment();
                case 2: return new ModeFragment();
                case 3: return WearActionFragment.newInstance(R.drawable.ic_network_wifi_white_48dp, "Connect to Wi-Fi", wifiActionClickedListener);
            }
        }
        throw new IllegalArgumentException(String.format("Rows other than 0 are not supported (row = %d, col = %d)", row, col));
    }

    @Override
    public int getRowCount() {
        return NUM_ROWS;
    }

    @Override
    public int getColumnCount(int rowNum) {
        return NUM_COLS;
    }

    public void setStatus(CharSequence status) {
        this.status = status;
    }

//    @Override
//    public Drawable getBackgroundForPage(int row, int column) {
//        return appContext.getResources().getDrawable(R.drawable.background);
//    }

    public static class WifiActionClickListener implements OnActionClickedListener {
        @Override
        public void onActionClicked(Context appContext) {
            appContext.sendBroadcast(WearNotificationManager.newConnectWifiIntent(appContext));
        }
    }
}
