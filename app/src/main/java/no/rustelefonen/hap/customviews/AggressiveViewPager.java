package no.rustelefonen.hap.customviews;

import android.content.Context;
import android.graphics.PointF;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Fredrik on 16.04.2016.
 */
public class AggressiveViewPager extends ViewPager {

    private PointF initialTouch;

    public AggressiveViewPager(Context context) {
        super(context);
    }

    public AggressiveViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                initialTouch = new PointF(ev.getRawX(), ev.getRawY());
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaX = Math.abs(initialTouch.x - ev.getRawX());
                float deltaY = Math.abs(initialTouch.y - ev.getRawY());

                if (10 < deltaX && deltaX > deltaY){
                    return true;
                }
                break;
        }

        return super.onInterceptTouchEvent(ev);
    }
}
