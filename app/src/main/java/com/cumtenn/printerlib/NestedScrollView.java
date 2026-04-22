package com.cumtenn.printerlib;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class NestedScrollView extends ScrollView {

    private int maxHeight = 300 * 3;

    public NestedScrollView(Context context) {
        super(context);
    }

    public NestedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NestedScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.EXACTLY) {
            heightSize = Math.min(heightSize, maxHeight);
        }

        int newHeightSpec = MeasureSpec.makeMeasureSpec(heightSize, heightMode);
        super.onMeasure(widthMeasureSpec, newHeightSpec);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(ev);
    }
}
