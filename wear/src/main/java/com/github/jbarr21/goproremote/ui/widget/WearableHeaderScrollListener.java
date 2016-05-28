package com.github.jbarr21.goproremote.ui.widget;

import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.wearable.view.WearableListView;
import android.view.View;

public class WearableHeaderScrollListener extends OnScrollListener implements WearableListView.OnScrollListener {

    private View headerView;

    public WearableHeaderScrollListener(View headerView) {
        this.headerView = headerView;
    }

    @Override
    public void onScroll(int dy) {
        headerView.setTranslationY(headerView.getTranslationY() - dy);
    }

    @Override
    public void onAbsoluteScrollChange(int i) {
        // no op
    }

    @Override
    public void onScrollStateChanged(int i) {
        // no op
    }

    @Override
    public void onCentralPositionChanged(int i) {
        // no op
    }
}