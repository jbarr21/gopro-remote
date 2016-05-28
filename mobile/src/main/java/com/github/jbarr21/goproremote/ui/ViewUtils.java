package com.github.jbarr21.goproremote.ui;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.widget.ImageView;

import com.github.jbarr21.goproremote.GoProRemoteApp;

public class ViewUtils {

    public static void setTintedImage(ImageView view, @DrawableRes int drawableRes, @ColorRes int colorRes) {
        view.setImageDrawable(tintedImage(drawableRes, colorRes));
    }

    public static Drawable tintedImage(@DrawableRes int drawableRes, @ColorRes int colorRes) {
        Resources res = GoProRemoteApp.getInstance().getResources();
        Drawable drawable = res.getDrawable(drawableRes).mutate();
        Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(wrappedDrawable, res.getColor(colorRes));
        return wrappedDrawable;
    }
}
