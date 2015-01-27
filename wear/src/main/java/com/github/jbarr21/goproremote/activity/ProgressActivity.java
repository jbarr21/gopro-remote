package com.github.jbarr21.goproremote.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.github.jbarr21.goproremote.R;
import com.github.jbarr21.goproremote.common.GoProCommandResponse;
import com.github.jbarr21.goproremote.util.NavUtils;
import com.squareup.otto.Subscribe;
import com.twotoasters.servos.util.otto.BusProvider;

public class ProgressActivity extends Activity {

    public static final String EXTRA_COMMAND_REQUEST_ID = "request_id";

    private long requestId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);
        requestId = getIntent().getLongExtra(EXTRA_COMMAND_REQUEST_ID, 0);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onGoProCommandResponse(GoProCommandResponseEvent event) {
        if (requestId == event.response.getRequestId()) {
            NavUtils.showResultAnimation(this, event.response.isSuccess(), event.response.getMessage());
        }
    }

    public static class GoProCommandResponseEvent {
        public GoProCommandResponse response;
        public GoProCommandResponseEvent(GoProCommandResponse response) {
            this.response = response;
        }
    }
}
