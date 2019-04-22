package com.augment.cordovaplugin;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.Button;

import java.util.HashMap;

public class AugmentCordovaButton extends Button {

    final static int UNDEFINED_COLOR = -1;

    public final static String KEY_CODE             = "code";
    public final static String KEY_COLOR            = "color";
    public final static String KEY_TITLE            = "title";
    public final static String KEY_BORDER_SIZE      = "borderSize";
    public final static String KEY_BORDER_RADIUS    = "borderRadius";
    public final static String KEY_BORDER_COLOR     = "borderColor";
    public final static String KEY_FONT_SIZE        = "fontSize";
    public final static String KEY_BACKGROUND_COLOR = "backgroundColor";

    String code = null;
    int mBorderSize = 0;
    int mBorderRadius = 0;
    int mBorderColor = UNDEFINED_COLOR;
    int mBackgroundColor = UNDEFINED_COLOR;

    public AugmentCordovaButton(Context context) {
        super(context);
    }

    public AugmentCordovaButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AugmentCordovaButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void applyData(HashMap<String, String> data) {
        if (data.containsKey(KEY_CODE)) {
            this.code = data.get(KEY_CODE);
        }

        if (data.containsKey(KEY_TITLE)) {
            setText(data.get(KEY_TITLE));
        }

        if (data.containsKey(KEY_BORDER_SIZE)) {
            this.mBorderSize = Integer.parseInt(data.get(KEY_BORDER_SIZE));
        }

        if (data.containsKey(KEY_BORDER_RADIUS)) {
            this.mBorderRadius = Integer.parseInt(data.get(KEY_BORDER_RADIUS));
        }

        if (data.containsKey(KEY_BORDER_COLOR)) {
            this.mBorderColor = Color.parseColor(data.get(KEY_BORDER_COLOR));
        }

        if (data.containsKey(KEY_FONT_SIZE)) {
            setTextSize(Integer.parseInt(data.get(KEY_FONT_SIZE)));
        }

        if (data.containsKey(KEY_COLOR)) {
            setTextColor(Color.parseColor(data.get(KEY_COLOR)));
        }

        if (data.containsKey(KEY_BACKGROUND_COLOR)) {
            this.mBackgroundColor = Color.parseColor(data.get(KEY_BACKGROUND_COLOR));
        }

        setBackground(getCalculatedBackground());
    }

    // Helpers

    Drawable getCalculatedBackground() {
        GradientDrawable gradientDrawable = new GradientDrawable();
        if (mBackgroundColor != UNDEFINED_COLOR) {
            gradientDrawable.setColor(mBackgroundColor);
        }
        else {
            gradientDrawable.setColor(Color.TRANSPARENT);
        }

        if (mBorderRadius > 0) {
            gradientDrawable.setCornerRadius(mBorderRadius);
        }

        if (mBorderSize > 0) {
            int borderColor = mBorderColor;
            if (borderColor == UNDEFINED_COLOR) {
                borderColor = Color.TRANSPARENT;
            }
            gradientDrawable.setStroke(mBorderSize, borderColor);
        }

        return gradientDrawable;
    }

    // Getters / setters

    public String getCode() {
        return code;
    }
}
