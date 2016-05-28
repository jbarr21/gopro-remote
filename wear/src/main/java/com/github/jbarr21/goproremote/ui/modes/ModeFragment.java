package com.github.jbarr21.goproremote.ui.modes;

import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.wearable.view.WearableListView;
import android.support.wearable.view.WearableListView.OnScrollListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.jbarr21.goproremote.R;
import com.github.jbarr21.goproremote.ui.ProgressActivity;
import com.github.jbarr21.goproremote.ui.modes.ModeAdapter.ModeViewHolder;
import com.github.jbarr21.goproremote.common.data.GoProCommand;
import com.github.jbarr21.goproremote.common.data.GoProCommandRequest;
import com.github.jbarr21.goproremote.common.data.GoProMode;
import com.github.jbarr21.goproremote.common.utils.MessageUtils;
import com.github.jbarr21.goproremote.ui.BaseFragment;
import com.github.jbarr21.goproremote.ui.widget.WearableHeaderScrollListener;

import butterknife.ButterKnife;
import butterknife.Bind;
import rx.functions.Action1;
import timber.log.Timber;

public class ModeFragment extends BaseFragment implements WearableListView.ClickListener {

    @Bind(R.id.list) WearableListView listView;

    private static final GoProMode[] MODES = GoProMode.values();

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
        listView.addOnScrollListener((OnScrollListener) new WearableHeaderScrollListener(ButterKnife.findById(rootView, R.id.headerText)));
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        if (viewHolder instanceof ModeViewHolder) {
            ModeViewHolder modeHolder = (ModeViewHolder) viewHolder;
            Timber.d("selected mode %s", modeHolder.mode.name());
            final GoProCommand command = modeHolder.mode.getCommand();
            final GoProCommandRequest commandRequest = GoProCommandRequest.create(command, System.currentTimeMillis());
            MessageUtils.sendGoProCommandMessage(googleApiClient, commandRequest)
                    .subscribe(new Action1<Integer>() {
                        @Override
                        public void call(Integer requestId) {
                            Timber.d("sent message onNext - isOnMainThread? %b", Looper.myLooper() == Looper.getMainLooper());
                            Timber.d("Sent GoPro command (%s) successfully", command.name());
                            MessageUtils.disconnectGoogleApiClient(googleApiClient);
                            if (getActivity() != null) {
                                ProgressActivity.showProgressOrFailure(getActivity(), MessageUtils.SUCCESS, commandRequest.id());
                            }
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Timber.d("sent message onError - isOnMainThread? %b", Looper.myLooper() == Looper.getMainLooper());
                            Timber.e(throwable, "Failed to send GoPro command: %s", command.name());
                            MessageUtils.disconnectGoogleApiClient(googleApiClient);
                            if (getActivity() != null) {
                                ProgressActivity.showProgressOrFailure(getActivity(), MessageUtils.FAILURE, commandRequest.id());
                            }
                        }
                    });
        }
    }

    @Override
    public void onTopEmptyRegionClick() {
        // no op
    }
}
