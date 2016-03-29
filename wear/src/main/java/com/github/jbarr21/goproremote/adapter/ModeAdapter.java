package com.github.jbarr21.goproremote.adapter;

import android.support.v7.widget.RecyclerView;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.jbarr21.goproremote.R;
import com.github.jbarr21.goproremote.adapter.ModeAdapter.ModeViewHolder;
import com.github.jbarr21.goproremote.common.GoProMode;

import butterknife.ButterKnife;
import butterknife.Bind;

public class ModeAdapter extends RecyclerView.Adapter<ModeViewHolder> {

    private final GoProMode[] modes;

    public ModeAdapter(GoProMode[] modes) {
        this.modes = modes;
    }

    @Override
    public ModeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mode, parent, false);
        return new ModeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ModeViewHolder holder, int position) {
        holder.mode = modes[position];
        holder.label.setText(modes[position].getLabelResId());
    }

    @Override
    public int getItemCount() {
        return modes.length;
    }

    public static class ModeViewHolder extends WearableListView.ViewHolder {
        @Bind(R.id.circle) CircledImageView circle;
        @Bind(R.id.label) TextView label;

        public GoProMode mode;

        public ModeViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
