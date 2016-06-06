package com.github.jbarr21.goproremote.common.utils;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.widget.ImageView;

public class ViewUtils {

    public static void setTintedImage(ImageView view, @DrawableRes int drawableRes, @ColorRes int colorRes) {
        view.setImageDrawable(tintedImage(view.getResources(), drawableRes, colorRes));
    }

    public static Drawable tintedImage(Resources res, @DrawableRes int drawableRes, @ColorRes int colorRes) {
        Drawable drawable = res.getDrawable(drawableRes).mutate();
        Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(wrappedDrawable, res.getColor(colorRes));
        return wrappedDrawable;
    }
}
