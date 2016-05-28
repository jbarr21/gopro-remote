package com.github.jbarr21.goproremote.ui.controls;

import android.os.Bundle;

import com.github.jbarr21.goproremote.R;
import com.github.jbarr21.goproremote.ui.GoProActivity;

public class ControlActivity extends GoProActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
    }
}
