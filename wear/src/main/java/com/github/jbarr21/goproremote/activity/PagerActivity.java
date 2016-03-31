package com.github.jbarr21.goproremote.activity;

import android.app.AlarmManager;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.GridViewPager;
import android.view.View;
import android.view.WindowInsets;

import com.github.jbarr21.goproremote.R;
import com.github.jbarr21.goproremote.adapter.GoProGridPagerAdapter;
import com.github.jbarr21.goproremote.util.WearNotificationManager;
import com.squareup.otto.Subscribe;
import com.twotoasters.servos.util.otto.BusProvider;

import butterknife.ButterKnife;

public class PagerActivity extends WearableActivity {

    private GoProGridPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager);
        setAmbientEnabled();
        final Resources res = getResources();
        final GridViewPager pager = ButterKnife.findById(this, R.id.pager);
        pager.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                int rowMargin = res.getDimensionPixelOffset(R.dimen.page_row_margin);
                int colMargin = res.getDimensionPixelOffset(insets.isRound() ? R.dimen.page_column_margin_round : R.dimen.page_column_margin);
                pager.setPageMargins(rowMargin, colMargin);
                return insets;
            }
        });
        adapter = new GoProGridPagerAdapter(this, getFragmentManager());
        pager.setAdapter(adapter);
        ((DotsPageIndicator) ButterKnife.findById(this, R.id.indicator)).setPager(pager);
    }

    /**
     * isFinishing = true for swipe to close, false for palm to screen (and no onDestroy())
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            // show notification since app may be exiting
            WearNotificationManager.from(this).showStreamNotification("GoPro Remote", "Ready for control");
        } else {
            // hide notification since app is launching
            WearNotificationManager.from(this).hideStreamNotification();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BusProvider.unregister(this);
    }

    @Subscribe
    public void onStatusChanged(StatusChangedEvent event) {
        if (adapter != null) {
            adapter.setStatus(event.status);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        //refreshDisplayAndSetNextUpdate();
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
        //mAmbientStateAlarmManager.cancel(mAmbientStatePendingIntent);
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        //refreshDisplayAndSetNextUpdate();
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);

        // Described in the following section
        //refreshDisplayAndSetNextUpdate();
    }

    private void refreshDisplayAndSetNextUpdate() {
    /*
        if (isAmbient()) {
            // Implement data retrieval and update the screen for ambient mode
        } else {
            // Implement data retrieval and update the screen for interactive mode
        }

        long timeMs = System.currentTimeMillis();

        // Schedule a new alarm
        if (isAmbient()) {
            // Calculate the next trigger time
            long delayMs = AMBIENT_INTERVAL_MS - (timeMs % AMBIENT_INTERVAL_MS);
            long triggerTimeMs = timeMs + delayMs;

            mAmbientStateAlarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMs,
                    mAmbientStatePendingIntent);

        } else {
            // Calculate the next trigger time for interactive mode
        }
        */
    }

    public static class StatusChangedEvent {
        public CharSequence status;
        public StatusChangedEvent(CharSequence status) {
            this.status = status;
        }
    }
}