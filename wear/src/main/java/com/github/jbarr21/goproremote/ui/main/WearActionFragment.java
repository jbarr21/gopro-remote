package com.github.jbarr21.goproremote.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.wearable.view.CircledImageView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.jbarr21.goproremote.R;
import com.github.jbarr21.goproremote.ui.BaseFragment;

import java.io.Serializable;

import butterknife.Bind;
import butterknife.OnClick;

public class WearActionFragment extends BaseFragment {

    private static final String ARG_ICON_RES_ID = "icon_res_id";
    private static final String ARG_LABEL = "label";
    private static final String ARG_CLICK_LISTENER = "click_listener";

    @Bind(R.id.circle) CircledImageView circle;
    @Bind(R.id.label) TextView label;

    private OnActionClickedListener listener;

    public static WearActionFragment newInstance(Context context, @DrawableRes int icon, @StringRes int label, OnActionClickedListener listener) {
        return newInstance(icon, context.getString(label), listener);
    }

    public static WearActionFragment newInstance(@DrawableRes int iconResId, CharSequence label, OnActionClickedListener listener) {
        WearActionFragment fragment = new WearActionFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ICON_RES_ID, iconResId);
        args.putCharSequence(ARG_LABEL, label);
        args.putSerializable(ARG_CLICK_LISTENER, listener);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wear_action, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            int iconResId = args.getInt(ARG_ICON_RES_ID, 0);
            if (iconResId != 0) {
                circle.setImageResource(iconResId);
            }

            CharSequence label = args.getCharSequence(ARG_LABEL);
            if (!TextUtils.isEmpty(label)) {
                this.label.setText(label);
                this.label.setVisibility(View.VISIBLE);
            }

            listener = (OnActionClickedListener) args.getSerializable(ARG_CLICK_LISTENER);
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.circle)
    public void onActionClicked(View view) {
        if (listener != null && getActivity() != null) {
            listener.onActionClicked(getActivity().getApplicationContext());
        }
    }

    public interface OnActionClickedListener extends Serializable {
        void onActionClicked(Context appContext);
    }
}
