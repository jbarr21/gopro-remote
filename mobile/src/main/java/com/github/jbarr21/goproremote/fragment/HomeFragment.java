package com.github.jbarr21.goproremote.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.jbarr21.goproremote.R;
import com.github.jbarr21.goproremote.api.Apis;
import com.github.jbarr21.goproremote.api.GoProApi;
import com.github.jbarr21.goproremote.data.ConfigStorage;
import com.github.jbarr21.goproremote.util.GoProNotificaionManager;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static butterknife.OnTextChanged.Callback.AFTER_TEXT_CHANGED;

public class HomeFragment extends Fragment {

    @InjectView(R.id.password) TextView mPasswordInput;

    private GoProNotificaionManager noticiationManager;
    private ConfigStorage configStorage;

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

        ButterKnife.inject(this, getView());
        mPasswordInput.setText(configStorage.getWifiPassword());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }

    @SuppressWarnings("unused")
    @OnTextChanged(value = R.id.ssid, callback = AFTER_TEXT_CHANGED)
    void onSsidTextChanged(final Editable s) {
        configStorage.saveWifiSsid(s.toString());
    }

    @SuppressWarnings("unused")
    @OnTextChanged(value = R.id.password, callback = AFTER_TEXT_CHANGED)
    void onPasswordTextChanged(final Editable s) {
        configStorage.saveWifiPassword(s.toString());
    }

    @SuppressWarnings("unused")
    @OnClick({ R.id.show_notificaion_btn, R.id.take_photo_btn, R.id.stop_video_btn, R.id.mode_photo, R.id.mode_video })
    public void onGoProCommandClicked(View view) {
        GoProApi goProApi = Apis.getGoProApi();
        switch (view.getId()) {
            case R.id.show_notificaion_btn:
                noticiationManager.showStartNotification();
                break;
            case R.id.take_photo_btn:
                goProApi.takePhoto(goProApiCallback);
                break;
            case R.id.stop_video_btn:
                goProApi.stopVideo(goProApiCallback);
                break;
            case R.id.mode_photo:
                goProApi.setPhotoMode(goProApiCallback);
                break;
            case R.id.mode_video:
                goProApi.setVideoMode(goProApiCallback);
                break;
            default:
                Toast.makeText(getActivity(), "not implemented", Toast.LENGTH_SHORT).show();
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
}
