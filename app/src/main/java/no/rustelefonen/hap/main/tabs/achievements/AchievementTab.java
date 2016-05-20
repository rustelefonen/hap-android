package no.rustelefonen.hap.main.tabs.achievements;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import no.rustelefonen.hap.R;
import no.rustelefonen.hap.entities.Achievement;
import no.rustelefonen.hap.lists.misc.DividerItemDecoration;
import no.rustelefonen.hap.main.tabs.activity.MainActivity;
import no.rustelefonen.hap.persistence.dao.AchievementDao;
import no.rustelefonen.hap.tabs.misc.TabPage;

import static no.rustelefonen.hap.util.DialogHelper.showInfoDialog;

/**
 * Created by martinnikolaisorlie on 26/01/16.
 */
public class AchievementTab extends TabPage {

    private MainActivity mainActivity;
    private AchievementDao achievementDao;
    private AchievementAdapter achievementAdapter;
    private Achievement nextUpcoming;
    private List<Achievement> completedAchievements;
    private Handler handler;
    private Runnable progressUpdater;
    private Unbinder unbinder;

    @BindView(R.id.achievement_row) RelativeLayout achievementRow;
    @BindView(R.id.achievement_title) TextView achievementTitle;
    @BindView(R.id.achievement_description) TextView achievementDescription;
    @BindView(R.id.achievement_list_badge) ImageView achievementBadge;
    @BindView(R.id.achievement_progress_bar) ProgressBar achievementProgress;
    @BindView(R.id.achievement_recyclerview) RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.achievement_tab, container, false);
        unbinder = ButterKnife.bind(this, v);
        mainActivity = (MainActivity) getActivity();
        handler = new Handler();
        initRecyclerView();
        initAchievementsList();
        EventBus.getDefault().register(this);

        //disable ripple effect
        achievementRow.setClickable(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) achievementRow.setForeground(null);
        return v;
    }

    @Override
    public void onResume(){
        super.onResume();
        handler.post(progressUpdater);
    }

    @Override
    public void onPause(){
        super.onPause();
        handler.removeCallbacks(progressUpdater);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        unbinder.unbind();
    }

    private void initRecyclerView(){
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity()));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setFocusable(false);
    }

    private void initAchievementsList() {
        achievementDao = new AchievementDao(mainActivity);
        completedAchievements = achievementDao.fetchCompletedSorted(mainActivity.getUser());
        achievementAdapter = new AchievementAdapter(completedAchievements);
        recyclerView.setAdapter(achievementAdapter);
        refetchNextUpcoming();
    }

    private void refetchNextUpcoming(){
        nextUpcoming = achievementDao.fetchNextUpcoming(mainActivity.getUser());
        if(nextUpcoming == null){
            handler.removeCallbacks(progressUpdater);
            achievementRow.setVisibility(View.GONE);
        }
        else {
            achievementRow.setVisibility(View.VISIBLE);
            initProgressUpdater();
            updateNextAchievementView();
        }
    }

    private void updateNextAchievementView(){
        if(nextUpcoming == null) return;
        achievementTitle.setText(nextUpcoming.getTitle());
        achievementDescription.setText(nextUpcoming.getDescription());
        achievementBadge.setImageResource(nextUpcoming.getImageId(false));
        achievementProgress.setProgress((int) (nextUpcoming.getProgress(mainActivity.getUser()) * achievementProgress.getMax()));
    }

    private void initProgressUpdater(){
        progressUpdater = new Runnable() {
            @Override
            public void run() {
                if (achievementProgress == null) return;

                int progress = nextUpcoming != null ? (int) (nextUpcoming.getProgress(mainActivity.getUser()) * achievementProgress.getMax()) : 0;
                achievementProgress.setProgress(progress);
                handler.postDelayed(this, 1000);
                if(progress < achievementProgress.getMax()) return;
                updateAchievementList();
            }
        };
        handler.post(progressUpdater);
    }

    private void updateAchievementList(){
        if(nextUpcoming != null){
            achievementAdapter.getItems().add(0, nextUpcoming);
            achievementAdapter.notifyItemInserted(0);
        }
        refetchNextUpcoming();
        if(nextUpcoming == null) return;

        final float initialY = achievementRow.getY();
        achievementRow.animate().y(initialY + achievementRow.getHeight()).setDuration(250).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                updateNextAchievementView();
                achievementRow.setY(initialY);
                achievementRow.setAlpha(0);
                achievementRow.animate().alpha(1).setDuration(250).setListener(null);
            }
        });
    }

    @OnClick(R.id.next_upcoming_card)
    public void showAllAchievements(){
        Intent intent = new Intent(getContext(), AllAchievementsActivity.class);
        intent.putExtra(AllAchievementsActivity.USER_INFO_EXTRA, mainActivity.getUser());
        startActivity(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onPresentAchievementEvent(PresentUnlockedAchievementEvent presentAchievementEvent){
        EventBus.getDefault().removeStickyEvent(presentAchievementEvent);
        Achievement unlockedAchievement = null;

        if (nextUpcoming != null && nextUpcoming.isComplete(mainActivity.getUser())) unlockedAchievement = nextUpcoming;
        else if (completedAchievements != null && !completedAchievements.isEmpty()) unlockedAchievement = completedAchievements.get(0);

        if(unlockedAchievement != null){
            showInfoDialog(getActivity(), unlockedAchievement.getTitle(), unlockedAchievement.getDescription());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshAchievemnts(RefreshAchievementListEvent event){
        initAchievementsList();
    }

    public static class RefreshAchievementListEvent{}
    public static class PresentUnlockedAchievementEvent {}
}
