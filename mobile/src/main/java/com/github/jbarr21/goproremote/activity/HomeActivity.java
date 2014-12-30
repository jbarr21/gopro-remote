package com.github.jbarr21.goproremote.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;

import com.github.jbarr21.goproremote.R;
import com.github.jbarr21.goproremote.fragment.HomeFragment;

public class HomeActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        setContentView(R.layout.activity_home);
        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag(HomeFragment.class.getSimpleName()) == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new HomeFragment(), HomeFragment.class.getSimpleName())
                    .commit();
        }
    }
}
