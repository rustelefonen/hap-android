package no.rustelefonen.hap.intro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import no.rustelefonen.hap.R;
import no.rustelefonen.hap.tabs.misc.TabPage;
import no.rustelefonen.hap.util.AnimationListenerAdapter;

/**
 * Created by martinnikolaisorlie on 18/02/16.
 */
public class WelcomeTab extends TabPage {
    @BindView(R.id.swipe_hand_image) ImageView image;
    private Unbinder unbinder;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.intro_welcome_tab, container, false);
        unbinder = ButterKnife.bind(this, v);

        final Animation handSlideAnimation = AnimationUtils.loadAnimation(v.getContext(), R.anim.slide_hand_left);
        final AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(300);
        fadeOut.setStartOffset(100);

        handSlideAnimation.setAnimationListener(new AnimationListenerAdapter(){
            public void onAnimationEnd(Animation animation) { image.startAnimation(fadeOut); }
        });

        fadeOut.setAnimationListener(new AnimationListenerAdapter(){
            public void onAnimationEnd(Animation animation) { image.startAnimation(handSlideAnimation); }
        });

        image.startAnimation(handSlideAnimation);

        return v;
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        unbinder.unbind();
    }
}
