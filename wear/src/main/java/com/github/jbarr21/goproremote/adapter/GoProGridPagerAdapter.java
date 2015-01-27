package com.github.jbarr21.goproremote.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.text.TextUtils;

import com.github.jbarr21.goproremote.R;
import com.github.jbarr21.goproremote.fragment.ControlFragment;
import com.github.jbarr21.goproremote.fragment.ModeFragment;
import com.github.jbarr21.goproremote.fragment.WearActionFragment;
import com.github.jbarr21.goproremote.fragment.WearActionFragment.OnActionClickedListener;
import com.github.jbarr21.goproremote.util.WearNotificationManager;

public class GoProGridPagerAdapter extends FragmentGridPagerAdapter {

    private final Context appContext;
    private CharSequence status;

    public GoProGridPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.appContext = context.getApplicationContext();
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

//    @Override
//    public Drawable getBackgroundForPage(int row, int column) {
//        return appContext.getResources().getDrawable(R.drawable.background);
//    }

    @Override
    public int getRowCount() {
        return 1;
    }

    @Override
    public int getColumnCount(int rowNum) {
        return 4;
    }

    public void setStatus(CharSequence status) {
        this.status = status;
    }

    private OnActionClickedListener wifiActionClickedListener = (appContext) -> {
        appContext.sendBroadcast(WearNotificationManager.newConnectWifiIntent(appContext));
    };
}
