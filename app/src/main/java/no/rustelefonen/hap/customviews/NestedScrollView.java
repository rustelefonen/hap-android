package no.rustelefonen.hap.customviews;

/**
 * Created by lon on 19/04/16.
 */

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class NestedScrollView extends android.support.v4.widget.NestedScrollView {

    private boolean enableScrolling = true;

    public NestedScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public NestedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NestedScrollView(Context context) {
        super(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return isEnableScrolling() && super.onInterceptTouchEvent(ev);
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return isEnableScrolling() && super.onTouchEvent(ev);
    }

    public boolean isEnableScrolling() {
        return enableScrolling;
    }

    public void setEnableScrolling(boolean enableScrolling) {
        this.enableScrolling = enableScrolling;
    }
}