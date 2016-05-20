package no.rustelefonen.hap.customviews;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ProgressBar;

public class AnimatingProgressBar extends ProgressBar {
    private ValueAnimator animator;
    private boolean animateEnabled = true;

    public AnimatingProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public AnimatingProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnimatingProgressBar(Context context) {
        super(context);
        init();
    }

    private void init(){
        animator = ValueAnimator.ofInt(0);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(200);
        animator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                AnimatingProgressBar.super.setProgress((Integer) animation.getAnimatedValue());
            }
        });
    }

    public boolean isAnimateEnabled() {
        return animateEnabled;
    }

    public void setAnimateEnabled(boolean animateEnabled) {
        this.animateEnabled = animateEnabled;
    }

    public void setAnimatorInterpolator(Interpolator interpolator){
        animator.setInterpolator(interpolator);
    }

    @Override
    public synchronized void setProgress(int progress) {
        if (!animateEnabled) {
            super.setProgress(progress);
            return;
        }

        animator.cancel();
        animator.setIntValues(getProgress(), progress);
        animator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animator != null) animator.cancel();
    }
}