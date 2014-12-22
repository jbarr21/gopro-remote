package com.github.jbarr21.goproremote.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.github.jbarr21.goproremote.R;
import com.melnykov.fab.FloatingActionButton;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.listeners.EventListener;

import java.lang.ref.WeakReference;

public class SnackbarEventListener implements EventListener {

    private Context appContext;
    private WeakReference<FloatingActionButton> floatingActionButtonRef;

    public SnackbarEventListener(@NonNull Context context, @NonNull FloatingActionButton fab) {
        appContext = context.getApplicationContext();
        floatingActionButtonRef = new WeakReference<>(fab);
    }

    @Override
    public void onShow(Snackbar snackbar) {
        if (hasFloatingActionButton()) {
            Animation sbAnimation = loadAnimation(R.anim.sb__in);
            floatingActionButtonRef.get().animate().translationY(-snackbar.getHeight())
                    .setInterpolator(sbAnimation.getInterpolator())
                    .setDuration(sbAnimation.getDuration());
        }
    }

    @Override
    public void onShown(Snackbar snackbar) {
        // no op
    }

    @Override
    public void onDismiss(Snackbar snackbar) {
        if (hasFloatingActionButton()) {
            Animation sbAnimation = loadAnimation(R.anim.sb__out);
            floatingActionButtonRef.get().animate().translationY(0)
                    .setInterpolator(sbAnimation.getInterpolator())
                    .setDuration(sbAnimation.getDuration());
        }
    }

    @Override
    public void onDismissed(Snackbar snackbar) {
        // no op
    }

    private Animation loadAnimation(int animationId) {
        return AnimationUtils.loadAnimation(appContext, animationId);
    }

    private boolean hasFloatingActionButton() {
        return floatingActionButtonRef.get() != null;
    }
}