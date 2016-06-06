package com.github.jbarr21.goproremote.ui;

import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.f2prateek.rx.receivers.RxBroadcastReceiver;
import com.github.jbarr21.goproremote.GoProRemoteApp;
import com.github.jbarr21.goproremote.R;
import com.github.jbarr21.goproremote.common.data.GoProMode;
import com.github.jbarr21.goproremote.common.data.GoProState;
import com.github.jbarr21.goproremote.common.data.api.GoProApi;
import com.github.jbarr21.goproremote.common.data.api.GoProUtils;
import com.github.jbarr21.goproremote.common.data.storage.ConfigStorage;
import com.github.jbarr21.goproremote.common.utils.GoProStateParser;
import com.github.jbarr21.goproremote.common.utils.RxUtils;
import com.github.jbarr21.goproremote.common.utils.WifiUtils;
import com.twotoasters.servos.util.butterknife.EnabledSetter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static com.github.jbarr21.goproremote.common.utils.ViewUtils.tintedImage;

public class HomeActivity extends AppCompatActivity {

    private static final boolean ANIMATE = true;
    private static final boolean DONT_ANIMATE = false;

    @Bind(R.id.coordinator)             CoordinatorLayout coordinator;
    @Bind(R.id.toolbar)                 Toolbar toolbar;
    @Bind(R.id.notificationSwitch)      SwitchCompat notificationSwitch;
    @Bind(R.id.ssid)                    EditText ssidInput;
    @Bind(R.id.password)                EditText passwordInput;
    @Bind(R.id.mode)                    RadioGroup modeGroup;
    @Bind(R.id.fab)                     FloatingActionButton fab;

    @Bind({ R.id.mode_video, R.id.mode_photo, R.id.mode_burst, R.id.mode_timelapse })
    List<RadioButton> modeButtons;

    private HashMap<Integer, GoProMode> radioButtonToModeMap;

    @Inject ConfigStorage configStorage;
    @Inject GoProApi goProApi;

    private Subscription wifiChangedSubscription;
    private GoProMode mode;
    private boolean isRecording;
    private boolean isPasswordVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GoProRemoteApp.getComponent().inject(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        setContentView(R.layout.activity_home);

        mode = GoProMode.VIDEO;
        isRecording = false;

        setupViews(this);

        if (WifiUtils.isConnectedToGoProWifi(this)) {
            // re-inject to update OkHttpClient
            GoProRemoteApp.getComponent().inject(this);
        } else {
            connectToGoProWifi();
        }

        updateCameraCurrentState();

        // TODO: move to show when WiFi connects and hide when disconnect
        //notificationManager.showStartNotification();
        // TODO: use messages to send to wearable to launch its own activity rather than starting notification
    }

    private void setupViews(@NonNull AppCompatActivity activity) {
        ButterKnife.bind(this);
        buildRadioButtonToModeMap();
        activity.setSupportActionBar(toolbar);

        ssidInput.setText(configStorage.getWifiSsid());
        passwordInput.setText(configStorage.getWifiPassword());
        passwordInput.setCompoundDrawables(null, null, tintedImage(R.drawable.ic_visibility_white_24dp, R.color.appColorAccent), null);
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
        EditText editText = passwordInput;
        editText.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, tintedImage(R.drawable.ic_visibility_white_24dp, R.color.appColorAccent), null);
        editText.setOnTouchListener(new View.OnTouchListener() {
            private int previousInputType;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final EditText editText = passwordInput;
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
        if (TextUtils.isEmpty(configStorage.getWifiSsid())) {
            return;
        }

        GoProUtils.fetchCameraState(goProApi)
                .compose(RxUtils.applyApiRequestSchedulers())
                .subscribe(response -> {
                    try {
                        GoProState state = GoProStateParser.from(response);
                        Timber.d("GoPro state: %s", state.toString());
                        updateUi(state, DONT_ANIMATE);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, throwable -> {
                    //updateUi(null, DONT_ANIMATE);
                    Timber.e(throwable, "Error updating current camera state");
                });
    }

    // TODO: auto-connect to GoPro Wi-Fi
    private void connectToGoProWifi() {
        showSnackbar(Snackbar.make(coordinator, String.format("Connecting to %s...", configStorage.getWifiSsid()), Snackbar.LENGTH_INDEFINITE));

        subscribeWifiListener();
        WifiUtils.addGoProWifiNetwork(this)
                .subscribeOn(Schedulers.newThread())
                .subscribe(it -> Timber.i("Added gopro wifi"), throwable -> Timber.e(throwable, "Could not add gopro wifi"));
    }

    private void subscribeWifiListener() {
        IntentFilter intentFilter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        RxUtils.unsubscribeSafely(wifiChangedSubscription);
        wifiChangedSubscription = RxBroadcastReceiver.create(this, intentFilter)
                .doOnNext(intent -> Timber.i("WiFi changed intent for SSID: %s", WifiUtils.ssidFromWifiChangedIntent(intent)))
                .filter(intent -> configStorage.getWifiSsid() != null && configStorage.getWifiSsid().equals(WifiUtils.ssidFromWifiChangedIntent(intent)))
                .map(intent -> (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO))
                .doOnNext(networkInfo -> Timber.i("Network state is %s [isConnected = %b]", networkInfo.getState(), networkInfo.isConnected()))
                .filter(networkInfo -> networkInfo.isConnected())
                .first()
                .doOnNext(it -> {
                    if (VERSION.SDK_INT >= VERSION_CODES.M) {
                        // re-inject to update OkHttpClient
                        GoProRemoteApp.getComponent().inject(this);
                    }
                })
                .timeout(10, TimeUnit.SECONDS)
                .subscribe(intent -> {
                    showSnackbar(Snackbar.make(coordinator, String.format("Connected to %s", configStorage.getWifiSsid()), Snackbar.LENGTH_SHORT));
                }, throwable -> {
                    Timber.e(throwable, "Could not connect to GoPro Wi-Fi");
                    showSnackbar(Snackbar.make(coordinator, "Could not connect to GoPro Wi-Fi", Snackbar.LENGTH_INDEFINITE)
                            .setAction("Retry", view -> connectToGoProWifi())
                            .setActionTextColor(getResources().getColor(android.R.color.holo_red_light)));
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RxUtils.unsubscribeSafely(wifiChangedSubscription);
        ButterKnife.unbind(this);
    }

    @SuppressWarnings("unused")
    @OnCheckedChanged(R.id.notificationSwitch)
    public void onNotificationToggled(boolean isChecked) {
        updatePowerToggle(isChecked, ANIMATE);
        if (isChecked) {
            goProApi.powerOn()
                    .onErrorResumeNext(throwable -> throwable instanceof HttpException && ((HttpException) throwable).code() == 403 ? Observable.empty() : Observable.error(throwable))
                    .compose(RxUtils.applyApiRequestSchedulers())
                    .subscribe(
                            it -> Timber.d("success"),
                            e -> showSnackbar(newSnackbar("Failed to turn on GoPro"))); // TODO: init default mode
        } else {
            goProApi.powerOff()
                    .onErrorResumeNext(throwable -> throwable instanceof HttpException && ((HttpException) throwable).code() == 403 ? Observable.empty() : Observable.error(throwable))
                    .compose(RxUtils.applyApiRequestSchedulers())
                    .subscribe(
                            it -> Timber.d("success"),
                            e -> showSnackbar(newSnackbar("Failed to turn off GoPro")));
        }
    }

    private void updatePowerToggle(boolean isPowerOn, boolean animate) {
        ButterKnife.apply(modeButtons, new EnabledSetter(), isPowerOn);
        if (notificationSwitch.isChecked() != isPowerOn) {
            notificationSwitch.setChecked(isPowerOn);
        }

        if (isPowerOn) {
            fab.show();
        } else {
            fab.hide();
        }
    }

    public void updateUi(GoProState state, boolean animate) {
        if (state == null) {
            state = GoProState.CAMERA_OFF;
        }

        if (state != GoProState.CAMERA_OFF) {
            mode = state.currentMode();
            isRecording = state.recording();
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
        Observable actionObservable = null;
        switch (mode) {
            case VIDEO:     actionObservable = goProApi.setVideoMode(); break;
            case PHOTO:     actionObservable = goProApi.setPhotoMode(); break;
            case BURST:     actionObservable = goProApi.setBurstMode(); break;
            case TIMELAPSE: actionObservable = goProApi.setTimelapseMode(); break;
        }

        if (actionObservable != null) {
            actionObservable
                    .compose(RxUtils.applyApiRequestSchedulers())
                    .subscribe(
                        it -> Timber.d("success"),
                        e -> showSnackbar(newSnackbar("Failed to update mode")));
        }
    }

    @OnTextChanged(R.id.ssid)
    public void onSsidTextChanged(Editable text) {
        configStorage.saveWifiSsid(text != null ? text.toString() : null);
    }

    @OnTextChanged(R.id.password)
    public void onPasswordTextChanged(Editable text) {
        configStorage.saveWifiPassword(text != null ? text.toString() : null);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.fab)
    public void onFabClicked(View view) {
        // TODO: rollback mode based on callback failure
        switch (mode) {
            case PHOTO:
                goProApi.takePhoto()
                        .compose(RxUtils.applyApiRequestSchedulers())
                        .subscribe(
                                it -> Timber.d("success"),
                                e -> showSnackbar(newSnackbar("Failed to take photo")));
                showSnackbar(newSnackbar("Took picture"));
                break;

            case BURST:
                goProApi.startRecording()
                        .compose(RxUtils.applyApiRequestSchedulers())
                        .subscribe(
                                it -> Timber.d("success"),
                                e -> showSnackbar(newSnackbar("Failed to take burst photos")));
                showSnackbar(newSnackbar("Triggered photo burst"));
                break;

            case VIDEO:     // fall through
            case TIMELAPSE:
                isRecording = !isRecording;
                if (isRecording) {
                    goProApi.startRecording()
                            .compose(RxUtils.applyApiRequestSchedulers())
                            .subscribe(
                                    it -> Timber.d("success"),
                                    e -> showSnackbar(newSnackbar("Failed to stop video")));
                    showSnackbar(newSnackbar("Started recording"));
                } else {
                    goProApi.stopRecording()
                            .compose(RxUtils.applyApiRequestSchedulers())
                            .subscribe(
                                    it -> Timber.d("success"),
                                    e -> showSnackbar(newSnackbar("Failed to start video")));
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
        snackbar.show();
    }

    private Snackbar newSnackbar(@NonNull CharSequence text) {
        return Snackbar.make(coordinator, text, Snackbar.LENGTH_LONG);
    }
}
