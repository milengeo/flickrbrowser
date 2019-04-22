package com.example.flickr.widget;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;


/**
 * Custom layout manager with a desired aspect ratio
 */
public class AspectLayout extends LinearLayout {

    private static final double ASPECT = 0.75;
    public AspectLayout(Context context) {
        super(context);
    }

    public AspectLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AspectLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = (int) (ASPECT * widthSize);
        int newHeightSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, newHeightSpec);
    }

}