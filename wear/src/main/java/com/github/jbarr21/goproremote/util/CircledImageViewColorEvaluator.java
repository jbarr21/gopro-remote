package com.github.jbarr21.goproremote.util;

import android.animation.ArgbEvaluator;
import android.support.annotation.NonNull;
import android.support.wearable.view.CircledImageView;

/**
 * Allows you to easily create an object animator that will change the background color of a view.
 *
 * Example: ValueAnimator.ofObject(new CircledImageViewColorEvaluator(circledImageView), currentColor, newColor);
 */
public class CircledImageViewColorEvaluator extends ArgbEvaluator {

    private CircledImageView view;

    public CircledImageViewColorEvaluator(@NonNull CircledImageView view) {
        this.view = view;
    }

    @NonNull
    @Override
    public Object evaluate(float fraction, Object startValue, Object endValue) {
        Integer color = (Integer) super.evaluate(fraction, startValue, endValue);
        view.setCircleColor(color);
        return color;
    }
}