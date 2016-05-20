package no.rustelefonen.hap.util;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by Fredrik on 11.03.2016.
 */
public class SizeAnimator {

    public static void width(final View v, final int newWidth){
        size(v, newWidth, v.getHeight());
    }

    public static void height(final View v, final int newHeight){
        size(v, v.getWidth(), newHeight);
    }

    public static void size(final View v, final int newWidth, final int newHeight){
        int widthDistance = Math.abs(v.getWidth() - newWidth);
        int heightDistance = Math.abs(v.getHeight() - newHeight);
        int defaultDuration = (int) (Math.max(widthDistance, heightDistance) / v.getContext().getResources().getDisplayMetrics().density);

        size(v, newWidth, newHeight, defaultDuration);
    }

    public static void size(final View v, final int newWidth, final int newHeight, int duration){
        final boolean widthIsExpanding = newWidth > v.getMeasuredWidth();
        final boolean heightIsExpanding = newHeight > v.getMeasuredHeight();
        final int initialWidth = v.getMeasuredWidth();
        final int initialHeight = v.getMeasuredHeight();
        final int widthDistance = Math.abs(initialWidth - newWidth);
        final int heightDistance = Math.abs(initialHeight - newHeight);

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                int widthStep = (int) (widthDistance * interpolatedTime);
                int heightStep = (int) (heightDistance * interpolatedTime);
                v.getLayoutParams().width = widthIsExpanding ? initialWidth + widthStep : initialWidth - widthStep;
                v.getLayoutParams().height = heightIsExpanding ? initialHeight + heightStep : initialHeight - heightStep;
                v.requestLayout();
            }
            @Override
            public boolean willChangeBounds() { return true; }
        };

        a.setDuration(duration);
        v.startAnimation(a);
    }
}
