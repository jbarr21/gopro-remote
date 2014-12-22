package com.github.jbarr21.goproremote.fragment;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.InterpolatorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.github.jbarr21.goproremote.GoProRemoteApp;
import com.github.jbarr21.goproremote.R;
import com.github.jbarr21.goproremote.api.Apis;
import com.github.jbarr21.goproremote.api.GoProApi;
import com.github.jbarr21.goproremote.data.ConfigStorage;
import com.github.jbarr21.goproremote.util.GoProNotificaionManager;
import com.marvinlabs.widget.floatinglabel.edittext.FloatingLabelEditText;
import com.melnykov.fab.FloatingActionButton;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.EventListener;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class HomeFragment extends Fragment implements OnCheckedChangeListener, FloatingLabelEditText.EditTextListener {

    @InjectView(R.id.toolbar) Toolbar toolbar;
    @InjectView(R.id.notificationSwitch) SwitchCompat notificationSwitch;
    @InjectView(R.id.ssid) FloatingLabelEditText ssidInput;
    @InjectView(R.id.password) FloatingLabelEditText passwordInput;
    @InjectView(R.id.mode_photo) Button photoButton;
    @InjectView(R.id.mode_video) Button videoButton;
    @InjectView(R.id.fab) FloatingActionButton fab;

    private GoProNotificaionManager noticiationManager;
    private ConfigStorage configStorage;
    private GoProMode mode;

    public HomeFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Context appContext = getActivity().getApplicationContext();

        noticiationManager = GoProNotificaionManager.from(appContext);
        configStorage = new ConfigStorage(appContext);
        mode = GoProMode.VIDEO;
        setupViews((ActionBarActivity) getActivity());
    }

    private void setupViews(@NonNull ActionBarActivity activity) {
        ButterKnife.inject(this, getView());
        activity.setSupportActionBar(toolbar);
        notificationSwitch.setOnCheckedChangeListener(this);
        ssidInput.setInputWidgetText(configStorage.getWifiSsid());
        ssidInput.setEditTextListener(this);
        passwordInput.setInputWidgetText(configStorage.getWifiPassword());
        passwordInput.setEditTextListener(this);
        fab.hide(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton view, boolean isChecked) {
        if (view == notificationSwitch) {
            if (isChecked) {
                noticiationManager.showStartNotification();
                fab.show();
                // TODO: init default mode
            } else {
                noticiationManager.hideStartNotification();
                fab.hide();
            }
            photoButton.setEnabled(isChecked);
            videoButton.setEnabled(isChecked);
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
        GoProApi api = Apis.getGoProApi();
        switch (mode) {
            case VIDEO:
                // TODO: switch mode on callback
                api.startVideo(goProApiCallback);
                mode = GoProMode.RECORDING;
                showSnackbar("Started recording");
                break;
            case RECORDING:
                api.stopVideo(goProApiCallback);
                mode = GoProMode.VIDEO;
                showSnackbar("Stopped recording");
                break;
            case PHOTO:
                api.takePhoto(goProApiCallback);
                showSnackbar("Took picture");
                break;
        }
        updateFabIcon();
    }

    @SuppressWarnings("unused")
    @OnClick({ R.id.mode_photo, R.id.mode_video })
    public void onGoProCommandClicked(View view) {
        GoProApi api = Apis.getGoProApi();
        switch (view.getId()) {
            case R.id.mode_photo:
                mode = GoProMode.PHOTO;
                api.setPhotoMode(goProApiCallback);
                break;
            case R.id.mode_video:
                mode = GoProMode.VIDEO;
                api.setVideoMode(goProApiCallback);
                break;
            default:
                showSnackbar("not implemented");
        }
        updateFabIcon();
    }

    private void updateFabIcon() {
        int resId;
        switch (mode) {
            case VIDEO:     resId = R.drawable.ic_videocam_white_24dp; break;
            case RECORDING: resId = R.drawable.ic_stop_white_24dp; break;
            case PHOTO:     resId = R.drawable.ic_photo_camera_white_24dp; break;
            default: return;
        }
        fab.setImageResource(resId);
    }

    private void showSnackbar(CharSequence text) {
        if (getActivity() != null) {
            SnackbarManager.show(
                    Snackbar.with(getActivity())
                            .text(text)
                            .eventListener(new EventListener() {
                                @Override
                                public void onShow(Snackbar snackbar) {
                                    fab.animate().translationY(-snackbar.getHeight())
                                            .setInterpolator(getInterpolator(R.interpolator.sb__decelerate_cubic))
                                            .setDuration(getDuration(snackbar));
                                }

                                @Override
                                public void onShown(Snackbar snackbar) {
                                    // no op
                                }

                                @Override
                                public void onDismiss(Snackbar snackbar) {
                                    fab.animate().translationY(0)
                                            .setInterpolator(getInterpolator(R.interpolator.sb__accelerate_cubic))
                                            .setDuration(getDuration(snackbar));
                                }

                                @Override
                                public void onDismissed(Snackbar snackbar) {
                                    // no op
                                }

                                private long getDuration(Snackbar snackbar) {
                                    return snackbar.getDuration() / 10;
                                }

                                private TimeInterpolator getInterpolator(@InterpolatorRes int id) {
                                    return AnimationUtils.loadInterpolator(GoProRemoteApp.getInstance(), id);
                                }
                            })
            );
        }
    }

    // Used to make the calls asynchronous
    Callback<Response> goProApiCallback = new Callback<Response>() {
        @Override
        public void success(Response response, Response response2) {
            // no op
        }

        @Override
        public void failure(RetrofitError error) {
            // no op
        }
    };

    private enum GoProMode {
        PHOTO, VIDEO, RECORDING
    }
}
