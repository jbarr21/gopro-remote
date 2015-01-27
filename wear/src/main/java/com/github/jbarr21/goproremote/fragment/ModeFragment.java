package com.github.jbarr21.goproremote.fragment;

import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.jbarr21.goproremote.R;
import com.github.jbarr21.goproremote.adapter.ModeAdapter;
import com.github.jbarr21.goproremote.adapter.ModeAdapter.ModeViewHolder;
import com.github.jbarr21.goproremote.common.GoProCommand;
import com.github.jbarr21.goproremote.common.GoProCommandRequest;
import com.github.jbarr21.goproremote.common.GoProMode;
import com.github.jbarr21.goproremote.common.MessageUtils;
import com.github.jbarr21.goproremote.util.NavUtils;
import com.github.jbarr21.goproremote.util.WearableHeaderScrollListener;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;

public class ModeFragment extends BaseFragment implements WearableListView.ClickListener {

    @InjectView(R.id.list) WearableListView listView;

    private static GoProMode[] MODES = { GoProMode.VIDEO, GoProMode.PHOTO, GoProMode.BURST, GoProMode.TIMELAPSE };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mode, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupListView(getView());
    }

    private void setupListView(View rootView) {
        listView.setHasFixedSize(true);
        listView.setClickListener(this);
        listView.setAdapter(new ModeAdapter(MODES));
        listView.setGreedyTouchMode(true);
        listView.addOnScrollListener(new WearableHeaderScrollListener(ButterKnife.findById(rootView, R.id.headerText)));
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        if (viewHolder instanceof ModeViewHolder) {
            ModeViewHolder modeHolder = (ModeViewHolder) viewHolder;
            Timber.d("selected mode %s", modeHolder.mode.name());
            GoProCommand command = modeHolder.mode.getCommand();
            GoProCommandRequest commandRequest = new GoProCommandRequest(command);
            MessageUtils.sendGoProCommandMessage(googleApiClient, commandRequest)
                    .subscribe(goProCommand -> {
                        Timber.d("sent message onNext - isOnMainThread? %b", Looper.myLooper() == Looper.getMainLooper());
                        Timber.d("Sent GoPro command (%s) successfully", command.name());
                        MessageUtils.disconnectGoogleApiClient(googleApiClient);
                        if (getActivity() != null) {
                            NavUtils.showProgressOrFailure(getActivity(), MessageUtils.SUCCESS, commandRequest.getId());
                        }
                    }, throwable -> {
                        Timber.d("sent message onError - isOnMainThread? %b", Looper.myLooper() == Looper.getMainLooper());
                        Timber.e(throwable, "Failed to send GoPro command: %s", command.name());
                        MessageUtils.disconnectGoogleApiClient(googleApiClient);
                        if (getActivity() != null) {
                            NavUtils.showProgressOrFailure(getActivity(), MessageUtils.FAILURE, commandRequest.getId());
                        }
                    });
        }
    }

    @Override
    public void onTopEmptyRegionClick() {
        // no op
    }
}
