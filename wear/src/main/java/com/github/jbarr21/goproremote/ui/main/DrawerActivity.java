package com.github.jbarr21.goproremote.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.drawer.WearableActionDrawer;
import android.support.wearable.view.drawer.WearableDrawerLayout;
import android.support.wearable.view.drawer.WearableNavigationDrawer;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.jbarr21.goproremote.GoProRemoteApp;
import com.github.jbarr21.goproremote.R;
import com.github.jbarr21.goproremote.common.data.GoProMode;
import com.github.jbarr21.goproremote.common.utils.ViewUtils;
import com.github.jbarr21.goproremote.data.RequestHandler;
import com.github.jbarr21.goproremote.ui.GoProActivity;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DrawerActivity extends GoProActivity {
    @Bind(R.id.drawer_layout)   WearableDrawerLayout drawerLayout;
    @Bind(R.id.nav_drawer)      WearableNavigationDrawer navDrawer;
    @Bind(R.id.content)         ViewGroup content;
    @Bind(R.id.title)           TextView title;
    @Bind(R.id.action_drawer)   WearableActionDrawer actionDrawer;

    DotsPageIndicator navDrawerIndicator;

    @Inject RequestHandler requestHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setBackgroundDrawableResource(R.color.window_bg);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        ButterKnife.bind(this);
        GoProRemoteApp.getComponent().inject(this);
        setAmbientEnabled();

        View navDrawerPeekView = getLayoutInflater().inflate(R.layout.view_drawer_dots, navDrawer, false);
        navDrawerIndicator = ButterKnife.findById(navDrawerPeekView, R.id.nav_drawer_indicator);
        navDrawer.setPeekContent(navDrawerPeekView);
        navDrawer.setAdapter(new MainNavDrawerAdapter(screen -> title.setText(screen.name())));
        drawerLayout.peekDrawer(Gravity.TOP);

        setupModeMenu(actionDrawer.getMenu(), GoProMode.values());
        actionDrawer.setOnMenuItemClickListener(item -> {
            if (0 <= item.getItemId() && item.getItemId() < GoProMode.values().length) {
                Toast.makeText(DrawerActivity.this, "Selected " + item.getTitle(), Toast.LENGTH_SHORT).show();
                drawerLayout.closeDrawer(Gravity.BOTTOM);
                requestHandler.onModeSelected(DrawerActivity.this, GoProMode.values()[item.getItemId()]);
                return true;
            }
            return false;
        });

        drawerLayout.unlockDrawer(Gravity.TOP);
        drawerLayout.peekDrawer(Gravity.BOTTOM);
    }

    private void setupModeMenu(Menu menu, GoProMode[] modes) {
        int i = 0;
        for (GoProMode mode : modes) {
            menu.add(Menu.NONE, i, i, getString(mode.getLabelResId()));
            menu.getItem(i).setIcon(ViewUtils.tintedImage(getResources(), mode.getIconResId(), R.color.lighter_ui_element));
            i++;
        }
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        drawerLayout.closeDrawer(Gravity.TOP);
        drawerLayout.closeDrawer(Gravity.BOTTOM);
        getWindow().setBackgroundDrawableResource(android.R.color.black);
        //refreshDisplayAndSetNextUpdate();
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
        getWindow().setBackgroundDrawableResource(R.color.window_bg);
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
}