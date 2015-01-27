package com.github.jbarr21.goproremote.fragment;

import android.content.Context;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.github.jbarr21.goproremote.R;
import com.github.jbarr21.goproremote.api.Apis;
import com.github.jbarr21.goproremote.api.GoProApi;
import com.github.jbarr21.goproremote.common.GoProMode;
import com.github.jbarr21.goproremote.data.ConfigStorage;
import com.github.jbarr21.goproremote.common.GoProState;
import com.github.jbarr21.goproremote.util.GoProNotificationManager;
import com.github.jbarr21.goproremote.util.GoProUtils;
import com.github.jbarr21.goproremote.util.SnackbarEventListener;
import com.github.jbarr21.goproremote.util.WifiUtils;
import com.marvinlabs.widget.floatinglabel.edittext.FloatingLabelEditText;
import com.melnykov.fab.FloatingActionButton;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.Snackbar.SnackbarDuration;
import com.nispok.snackbar.SnackbarManager;
import com.twotoasters.servos.util.StreamUtils;
import com.twotoasters.servos.util.butterknife.EnabledSetter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.InjectViews;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.android.content.ContentObservable;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class HomeFragment extends Fragment implements FloatingLabelEditText.EditTextListener {

    private static final boolean ANIMATE = true;
    private static final boolean DONT_ANIMATE = false;

    @InjectView(R.id.toolbar) Toolbar toolbar;
    @InjectView(R.id.notificationSwitch) SwitchCompat notificationSwitch;
    @InjectView(R.id.ssid) FloatingLabelEditText ssidInput;
    @InjectView(R.id.password) FloatingLabelEditText passwordInput;
    @InjectView(R.id.mode) RadioGroup modeGroup;
    @InjectView(R.id.fab) FloatingActionButton fab;
    @InjectViews({ R.id.mode_video, R.id.mode_photo, R.id.mode_burst, R.id.mode_timelapse }) List<RadioButton> modeButtons;

    private HashMap<Integer, GoProMode> radioButtonToModeMap;

    private Context appContext;
    private GoProNotificationManager notificationManager;
    private ConfigStorage configStorage;
    private Subscription wifiChangedSubscription;
    private GoProApi goProApi;
    private GoProMode mode;
    private boolean isRecording;
    private boolean isPasswordVisible;

    public HomeFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        appContext = getActivity().getApplicationContext();
        notificationManager = GoProNotificationManager.from(appContext);
        configStorage = new ConfigStorage(appContext);
        goProApi = Apis.getGoProApi();
        mode = GoProMode.VIDEO;
        isRecording = false;

        setupViews((ActionBarActivity) getActivity());
        updateCameraCurrentState();

        if (!WifiUtils.isConnectedToGoProWifi(getActivity())) {
            connectToGoProWifi();
        }

        // TODO: move to show when WiFi connects and hide when disconnect
        //notificationManager.showStartNotification();
        // TODO: use messages to send to wearable to launch its own activity rather than starting notification
    }


    private void setupViews(@NonNull ActionBarActivity activity) {
        ButterKnife.inject(this, getView());
        buildRadioButtonToModeMap();
        activity.setSupportActionBar(toolbar);
        ssidInput.setInputWidgetText(configStorage.getWifiSsid());
        ssidInput.setEditTextListener(this);
        passwordInput.setInputWidgetText(configStorage.getWifiPassword());
        passwordInput.setEditTextListener(this);
        setupDisclosePasswordButton();
        updatePowerToggle(false, DONT_ANIMATE);
    }

    private void buildRadioButtonToModeMap() {
        radioButtonToModeMap = new LinkedHashMap<>();
        for (int i = 0; i < modeButtons.size(); i++) {
            radioButtonToModeMap.put(modeButtons.get(i).getId(), GoProMode.values()[i]);
        }
    }

    private void setupDisclosePasswordButton() {
        EditText editText = passwordInput.getInputWidget();
        editText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_visibility_grey600_24dp, 0);
        editText.setOnTouchListener(new OnTouchListener() {
            private int previousInputType;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final EditText editText = passwordInput.getInputWidget();
                final int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        previousInputType = editText.getInputType();
                        isPasswordVisible = true;
                        setInputType(editText, EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD, true);
                        break;
                    case MotionEvent.ACTION_UP: // fall through
                    case MotionEvent.ACTION_CANCEL:
                        isPasswordVisible = false;
                        setInputType(editText, previousInputType, true);
                        previousInputType = -1;
                        break;
                }

                return false;
            }
        });
    }

    private void setInputType(EditText editText, int inputType, boolean keepState) {
        int selectionStart = -1;
        int selectionEnd = -1;
        if (keepState) {
            selectionStart = editText.getSelectionStart();
            selectionEnd = editText.getSelectionEnd();
        }
        editText.setInputType(inputType);
        if (keepState) {
            editText.setSelection(selectionStart, selectionEnd);
        }

        // TODO: add pressed state
        //updateDisclosePasswordButton();
    }

    private void updateCameraCurrentState() {
        AppObservable.bindFragment(this, GoProUtils.fetchCameraState(goProApi))
                .subscribe(response -> {
                    try {
                        GoProState state = GoProState.from(StreamUtils.streamToBytes(response.getBody().in()));
                        Timber.d("GoPro state: %s", state.toString());
                        updateUi(state, DONT_ANIMATE);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, throwable -> {
                    updateUi(null, DONT_ANIMATE);
                    Timber.e(throwable, "Error updating current camera state");
                });
    }

    // TODO: auto-connect to GoPro Wi-Fi
    private void connectToGoProWifi() {
        showSnackbar(Snackbar.with(appContext)
                .text(String.format("Connecting to %s...", configStorage.getWifiSsid()))
                .duration(SnackbarDuration.LENGTH_INDEFINITE));;

        subscribeWifiListener();
        WifiUtils.addGoProWifiNetwork(appContext)
                .subscribeOn(Schedulers.newThread())
                .subscribe();
    }

    private void subscribeWifiListener() {
        IntentFilter intentFilter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        wifiChangedSubscription = AppObservable.bindFragment(this, ContentObservable.fromBroadcast(appContext, intentFilter)
                .filter(intent -> ((NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO)).isConnected())
                .map(intent -> ((WifiInfo) intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO)))
                .filter(wifiInfo -> WifiUtils.ssidFromWifiInfo(wifiInfo).equals(configStorage.getWifiSsid()))
                .timeout(10, TimeUnit.SECONDS))
                .subscribe(intent -> {
                    showSnackbar(Snackbar.with(appContext)
                            .text(String.format("Connected to %s", configStorage.getWifiSsid()))
                            .duration(SnackbarDuration.LENGTH_SHORT));
                }, throwable -> {
                    showSnackbar(Snackbar.with(appContext)
                            .text("Could not connect to GoPro Wi-Fi")
                            .actionLabel("Retry")
                            .actionColorResource(android.R.color.holo_red_light)
                            .actionListener(snackbar -> connectToGoProWifi())
                            .swipeToDismiss(false)
                            .duration(SnackbarDuration.LENGTH_INDEFINITE));
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (wifiChangedSubscription != null && !wifiChangedSubscription.isUnsubscribed()) {
            wifiChangedSubscription.unsubscribe();
        }
        ButterKnife.reset(this);
    }

    @SuppressWarnings("unused")
    @OnCheckedChanged(R.id.notificationSwitch)
    public void onNotificationToggled(boolean isChecked) {
        updatePowerToggle(isChecked, ANIMATE);
        if (isChecked) {
            goProApi.powerOn().subscribe(); // TODO: init default mode
        } else {
            goProApi.powerOff().subscribe();
        }
    }

    private void updatePowerToggle(boolean isPowerOn, boolean animate) {
        ButterKnife.apply(modeButtons, new EnabledSetter(), isPowerOn);
        if (notificationSwitch.isChecked() != isPowerOn) {
            notificationSwitch.setChecked(isPowerOn);
        }

        if (isPowerOn) {
            fab.show(animate);
        } else {
            fab.hide(animate);
        }
    }

    public void updateUi(GoProState state, boolean animate) {
        if (state == null) state = GoProState.CAMERA_OFF;

        if (state != GoProState.CAMERA_OFF) {
            mode = state.getCurrentMode();
            isRecording = state.isRecording();
        } else {
            mode = GoProMode.VIDEO;
            isRecording = false;
        }

        updatePowerToggle(state.isPowerOn(), animate);
        ((RadioButton) modeGroup.getChildAt(mode.ordinal())).setChecked(true);
        updateFabIcon();
    }

    @SuppressWarnings("unused")
    @OnClick({ R.id.mode_video, R.id.mode_photo, R.id.mode_burst, R.id.mode_timelapse })
    public void onModeSelected() {
        mode = radioButtonToModeMap.get(modeGroup.getCheckedRadioButtonId());
        updateFabIcon();
        switch (mode) {
            case VIDEO:     goProApi.setVideoMode().subscribe(); break;
            case PHOTO:     goProApi.setPhotoMode().subscribe(); break;
            case BURST:     goProApi.setBurstMode().subscribe(); break;
            case TIMELAPSE: goProApi.setTimelapseMode().subscribe(); break;
        }
    }

    @Override
    public void onTextChanged(FloatingLabelEditText source, String text) {
        if (source == ssidInput) {
            configStorage.saveWifiSsid(text);
        } else if (source == passwordInput) {
            configStorage.saveWifiPassword(text);
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.fab)
    public void onFabClicked(View view) {
        // TODO: rollback mode based on callback failure
        switch (mode) {
            case PHOTO:
                goProApi.takePhoto().subscribe();
                showSnackbar(newSnackbar("Took picture"));
                break;

            case BURST:
                goProApi.startRecording().subscribe();
                showSnackbar(newSnackbar("Triggered photo burst"));
                break;

            case VIDEO:     // fall through
            case TIMELAPSE:
                isRecording = !isRecording;
                if (isRecording) {
                    goProApi.startRecording().subscribe();
                    showSnackbar(newSnackbar("Started recording"));
                } else {
                    goProApi.stopRecording().subscribe();
                    showSnackbar(newSnackbar("Stopped recording"));
                }
                break;
        }
        updateFabIcon();
    }

    private void updateFabIcon() {
        int resId;
        if (isRecording) {
            resId = R.drawable.ic_stop_white_24dp;
        } else {
            switch (mode) {
                case VIDEO: resId = R.drawable.ic_videocam_white_24dp; break;
                case PHOTO: resId = R.drawable.ic_photo_camera_white_24dp; break;
                case BURST: resId = R.drawable.ic_photo_library_white_24dp; break;
                case TIMELAPSE: resId = R.drawable.ic_timer_white_24dp; break;
                default: return;
            }
        }
        fab.setImageResource(resId);
    }

    private void showSnackbar(Snackbar snackbar) {
        if (getActivity() != null) {
            SnackbarManager.show(snackbar, getActivity());
        }
    }

    private Snackbar newSnackbar(@NonNull CharSequence text) {
        return Snackbar.with(appContext)
                .duration(SnackbarDuration.LENGTH_SHORT)
                .eventListener(new SnackbarEventListener(appContext, fab))
                .text(text);
    }
}
