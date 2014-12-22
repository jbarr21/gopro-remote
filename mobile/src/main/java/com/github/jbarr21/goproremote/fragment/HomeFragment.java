package com.github.jbarr21.goproremote.fragment;

import android.app.Activity;
import android.content.Context;
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
import com.github.jbarr21.goproremote.data.ConfigStorage;
import com.github.jbarr21.goproremote.data.GoProMode;
import com.github.jbarr21.goproremote.data.GoProState;
import com.github.jbarr21.goproremote.util.GoProNotificationManager;
import com.github.jbarr21.goproremote.util.SnackbarEventListener;
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

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.InjectViews;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import retrofit.client.Response;
import rx.android.observables.AndroidObservable;
import rx.functions.Action1;
import timber.log.Timber;

public class HomeFragment extends Fragment implements FloatingLabelEditText.EditTextListener {

    @InjectView(R.id.toolbar) Toolbar toolbar;
    @InjectView(R.id.notificationSwitch) SwitchCompat notificationSwitch;
    @InjectView(R.id.ssid) FloatingLabelEditText ssidInput;
    @InjectView(R.id.password) FloatingLabelEditText passwordInput;
    @InjectView(R.id.mode) RadioGroup modeGroup;
    @InjectView(R.id.fab) FloatingActionButton fab;
    @InjectViews({ R.id.mode_video, R.id.mode_photo, R.id.mode_burst, R.id.mode_timelapse }) List<RadioButton> modeButtons;

    private HashMap<Integer, GoProMode> radioButtonToModeMap;

    private GoProNotificationManager notificationManager;
    private ConfigStorage configStorage;
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
        Context appContext = getActivity().getApplicationContext();

        notificationManager = GoProNotificationManager.from(appContext);
        configStorage = new ConfigStorage(appContext);
        goProApi = Apis.getGoProApi();
        mode = GoProMode.VIDEO;
        isRecording = false;

        setupViews((ActionBarActivity) getActivity());
        updateCameraCurrentState();
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
        updateNotificationUiNoAnim(false);
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
        AndroidObservable.bindFragment(this, goProApi.fetchCameraState())
                .subscribe(new Action1<Response>() {
                    @Override
                    public void call(Response response) {
                        try {
                            byte[] stateBytes = StreamUtils.streamToBytes(response.getBody().in());
                            GoProState state = GoProState.from(stateBytes);
                            updateNotificationUiNoAnim(true);
                            selectMode(state.getCurrentMode());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        updateNotificationUiNoAnim(false);
                        Timber.e(throwable, "Error updating current camera state");
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }

    @SuppressWarnings("unused")
    @OnCheckedChanged(R.id.notificationSwitch)
    public void onNotificationToggled(boolean isChecked) {
        updateNotificationUiWithAnim(isChecked);
        if (isChecked) {
            goProApi.powerOn().subscribe(); // TODO: init default mode
        } else {
            goProApi.powerOff().subscribe();
        }
    }

    private void updateNotificationUiNoAnim(boolean enabled) {
        updateNotificationUi(enabled, false);
    }

    private void updateNotificationUiWithAnim(boolean enabled) {
        updateNotificationUi(enabled, true);
    }

    private void updateNotificationUi(boolean enabled, boolean animate) {
        ButterKnife.apply(modeButtons, new EnabledSetter(), enabled);
        if (notificationSwitch.isChecked() != enabled) {
            notificationSwitch.setChecked(enabled);
        }

        if (enabled) {
            notificationManager.showStartNotification();
            fab.show(animate);
        } else {
            notificationManager.hideStartNotification();
            fab.hide(animate);
        }
    }

    public void selectMode(GoProMode mode) {
        ((RadioButton) modeGroup.getChildAt(mode.ordinal())).setChecked(true);
    }

    @SuppressWarnings("unused")
    @OnClick({ R.id.mode_video, R.id.mode_photo, R.id.mode_burst, R.id.mode_timelapse })
    public void onModeSelected() {
        mode = radioButtonToModeMap.get(modeGroup.getCheckedRadioButtonId());
        updateFabIcon();
        switch (mode) {
            case VIDEO: goProApi.setVideoMode().subscribe(); break;
            case PHOTO: goProApi.setPhotoMode().subscribe(); break;
            case BURST: goProApi.setBurstMode().subscribe(); break;
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
        switch (mode) {
            case VIDEO:
                isRecording = !isRecording;
                if (isRecording) {
                    goProApi.startRecording().subscribe(); // TODO: rollback mode based on callback failure
                    showSnackbar("Started recording");
                } else {
                    goProApi.stopRecording().subscribe();
                    showSnackbar("Stopped recording");
                }
                break;
            case PHOTO:
                goProApi.takePhoto().subscribe();
                showSnackbar("Took picture");
                break;
            // TODO: impl others
        }
        updateFabIcon();
    }

    private void updateFabIcon() {
        int resId;
        switch (mode) {
            case VIDEO:     resId = isRecording ? R.drawable.ic_stop_white_24dp : R.drawable.ic_videocam_white_24dp; break;
            case PHOTO:     // fall through
            case BURST:     // fall through
            case TIMELAPSE: resId = R.drawable.ic_photo_camera_white_24dp; break;
            default: return;
        }
        fab.setImageResource(resId);
    }

    private void showSnackbar(CharSequence text) {
        if (getActivity() != null) {
            SnackbarManager.show(newSnackbar(getActivity(), text));
        }
    }

    private Snackbar newSnackbar(@NonNull Activity activity, @NonNull CharSequence text) {
        return Snackbar.with(activity)
                .duration(SnackbarDuration.LENGTH_SHORT)
                .eventListener(new SnackbarEventListener(activity, fab))
                .text(text);
    }
}
