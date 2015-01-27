package com.github.jbarr21.goproremote.widget;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView.OnCenterProximityListener;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.jbarr21.goproremote.R;
import com.github.jbarr21.goproremote.util.CircledImageViewColorEvaluator;
import com.twotoasters.servos.util.anim.TextColorEvaluator;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class WearableListItemLayout extends LinearLayout implements OnCenterProximityListener {

    @InjectView(R.id.circle) CircledImageView circle;
    @InjectView(R.id.label) TextView label;

    // item's circle starts at non-center size and grows while item's label starts at half alpha and becomes opaque
    private static final float CIRCLE_SCALE_PCT = 0.40f;

    private long animationDuration;
    private int circleColorCenter, circleColorNonCenter;
    private int textColorCenter, textColorNonCenter;

    public WearableListItemLayout(Context context) {
        this(context, null);
    }

    public WearableListItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WearableListItemLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        inflate(getContext(), R.layout.widget_wearable_list_item, this);
        animationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        circleColorCenter = getResources().getColor(R.color.wl_circle_centered);
        circleColorNonCenter = colorWithAlpha(circleColorCenter, 0.5f);
        textColorCenter = getResources().getColor(R.color.wl_text_centered);
        textColorNonCenter = colorWithAlpha(textColorCenter, 0.5f);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this);
    }

    // OnCenterProximityListener

    @Override
    public void onCenterPosition(boolean animate) {
        float circleScale = 1f + CIRCLE_SCALE_PCT;
        updateWidgets(circleScale, circleColorCenter, textColorCenter, animate);
    }

    @Override
    public void onNonCenterPosition(boolean animate) {
        float circleScale = 1f;
        updateWidgets(circleScale, circleColorNonCenter, textColorNonCenter, animate);
    }

    private void updateWidgets(float circleScale, int circleColor, int textColor, boolean animate) {
        if (animate) {
            AnimatorSet set = new AnimatorSet();
            set.setDuration(animationDuration);
            set.playTogether(
                    ObjectAnimator.ofPropertyValuesHolder(circle,
                            PropertyValuesHolder.ofFloat("scaleX", circle.getScaleX(), circleScale),
                            PropertyValuesHolder.ofFloat("scaleY", circle.getScaleY(), circleScale)),
                    ValueAnimator.ofObject(new CircledImageViewColorEvaluator(circle), getCircleColor(false), circleColor),
                    ValueAnimator.ofObject(new TextColorEvaluator(label), label.getCurrentTextColor(), textColor));
            set.start();
        } else {
            circle.setScaleX(circleScale);
            circle.setScaleY(circleScale);
            circle.setCircleColor(circleColor);
            label.setTextColor(textColor);
        }
    }

    // TODO: track this better
    private int getCircleColor(boolean isCenter) {
        return isCenter ? circleColorCenter : circleColorNonCenter;
    }

    private int colorWithAlpha(int color, float alpha) {
        return Color.argb(Math.round(255 * alpha), Color.red(color), Color.green(color), Color.blue(color));
    }
}