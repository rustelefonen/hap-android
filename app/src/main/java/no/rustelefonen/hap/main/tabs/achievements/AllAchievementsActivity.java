package no.rustelefonen.hap.main.tabs.achievements;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import no.rustelefonen.hap.R;
import no.rustelefonen.hap.entities.Achievement;
import no.rustelefonen.hap.entities.User;
import no.rustelefonen.hap.lists.misc.DividerItemDecoration;
import no.rustelefonen.hap.persistence.OrmLiteActivity;
import no.rustelefonen.hap.persistence.dao.AchievementDao;

/**
 * Created by Fredrik on 02.05.2016.
 */
public class AllAchievementsActivity extends OrmLiteActivity{
    public static final String USER_INFO_EXTRA = "user_info_extra";

    @BindView(R.id.nested_scroll_view) NestedScrollView nestedScrollView;
    @BindView(R.id.upcoming_recyclerview) RecyclerView upcomingRecyclerView;
    @BindView(R.id.completed_recyclerview) RecyclerView completedRecyclerView;
    private AchievementDao achievementDao;
    private User user;
    private Handler updateHandler;
    private Runnable updateRunnable;
    private Runnable reloadRunnable;
    private Unbinder unbinder;
    private long lastScrollTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.all_achievements_activity);
        super.onCreate(savedInstanceState);
        unbinder = ButterKnife.bind(this);
        initRecyclerView(upcomingRecyclerView);
        initRecyclerView(completedRecyclerView);
        user = getIntent().getParcelableExtra(USER_INFO_EXTRA);
        updateHandler = new Handler();
        initScrollChangeListener();
        achievementDao = new AchievementDao(this);
        populateUpcoming();
        populateCompleted();
        startProgressUpdater();
    }

    private void initRecyclerView(RecyclerView recyclerView){
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setFocusable(false);
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false); //disable animation
    }

    private void populateUpcoming() {
        final List<Achievement> items = achievementDao.fetchAllUpcomingSorted(user);
        AchievementAdapter achievementAdapter = new AchievementAdapter(items, new AchievementAdapter.ProgressValueCallback() {
            @Override
            public double getProgressFor(Achievement achievement) {
                if(achievement.isComplete(user)) reloadRecyclerViews();
                return achievement.getProgress(user);
            }
        });
        upcomingRecyclerView.setAdapter(achievementAdapter);
    }

    private void populateCompleted() {
        final List<Achievement> items = achievementDao.fetchCompletedSorted(user);
        AchievementAdapter achievementAdapter = new AchievementAdapter(items);
        completedRecyclerView.setAdapter(achievementAdapter);
    }

    private void startProgressUpdater(){
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateHandler.postDelayed(this, 1000);
                if(System.currentTimeMillis() - lastScrollTime < 100) return;

                LinearLayoutManager layoutManager = (LinearLayoutManager) upcomingRecyclerView.getLayoutManager();
                int firstVisible = layoutManager.findFirstVisibleItemPosition();
                int lastVisible = layoutManager.findLastVisibleItemPosition();
                upcomingRecyclerView.getAdapter().notifyItemRangeChanged(firstVisible, 1);//lastVisible+1);
            }
        };
        updateHandler.postDelayed(updateRunnable, 1000);
    }

    private void reloadRecyclerViews() {
        reloadRunnable = new Runnable() {
            @Override
            public void run() {
                populateUpcoming();
                populateCompleted();
            }
        };
        updateHandler.post(reloadRunnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        updateHandler.removeCallbacks(updateRunnable);
        updateHandler.removeCallbacks(reloadRunnable);
    }

    private void initScrollChangeListener(){
        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                lastScrollTime = System.currentTimeMillis();
            }
        });
    }
}
