package com.github.jbarr21.goproremote.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;

import com.github.jbarr21.goproremote.GoProRemoteApp;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import butterknife.ButterKnife;

public abstract class BaseFragment extends Fragment {

    protected Context appContext;
    protected GoogleApiClient googleApiClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        appContext = GoProRemoteApp.getInstance();
        googleApiClient = new GoogleApiClient.Builder(appContext)
                .addApi(Wearable.API)
                .build();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ButterKnife.bind(this, getView());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
