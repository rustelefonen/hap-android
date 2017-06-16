package no.rustelefonen.hap.main.tabs.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import no.rustelefonen.hap.R;
import no.rustelefonen.hap.entities.User;
import no.rustelefonen.hap.main.tabs.achievements.AchievementTab;
import no.rustelefonen.hap.main.tabs.home.HomeTab;
import no.rustelefonen.hap.main.tabs.info.InfoTab;
import no.rustelefonen.hap.main.tabs.program.ProgramTab;
import no.rustelefonen.hap.main.tabs.trigger.TriggerActivity;
import no.rustelefonen.hap.notifications.AchievementReceiver;
import no.rustelefonen.hap.persistence.OrmLiteActivity;
import no.rustelefonen.hap.persistence.dao.UserDao;
import no.rustelefonen.hap.tabs.misc.SwipePageAdapter;
import no.rustelefonen.hap.util.DialogHelper;

public class MainActivity extends OrmLiteActivity {
    private static final int USER_TRIGGER_REQUEST = 1;
    public static final String NAV_TO_ACHIEVEMENT_EXTRA = "nav_to_achievement_extra";

    private User user;
    private SwipePageAdapter swipePageAdapter;
    private MainTabPageHandler tabPageHandler;
    private InfoSearchHandler infoSearchHandler;
    private Unbinder unbinder;

    @BindView(R.id.search) RecyclerView searchRecyclerView;
    @BindView(R.id.viewpager) ViewPager viewPager;
    @BindView(R.id.tabs) TabLayout tabLayout;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.nav_view) NavigationView navigationView;
    @BindView(R.id.add_triggers) FloatingActionButton floatingActionButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.main_activity);
        super.onCreate(savedInstanceState);
        unbinder = ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        initViewElements();
        navigateViewPagerIfSpecifiedBy(getIntent());
        user = new UserDao(this).getFirst();
        infoSearchHandler = new InfoSearchHandler(this, searchRecyclerView, tabLayout);
    }

    private void initViewElements() {
        swipePageAdapter = new SwipePageAdapter(getSupportFragmentManager());
        swipePageAdapter.addItem(HomeTab.class, getString(R.string.home_tab_title), R.drawable.home);
        swipePageAdapter.addItem(ProgramTab.class, getString(R.string.overview_tab_title), R.drawable.graph);
        swipePageAdapter.addItem(AchievementTab.class, getString(R.string.achievements_tab_title), R.drawable.trophy);
        swipePageAdapter.addItem(InfoTab.class, getString(R.string.info_tab_title), R.drawable.book);

        viewPager.setAdapter(swipePageAdapter);
        viewPager.setOffscreenPageLimit(4);
        tabLayout.setupWithViewPager(viewPager);

        tabPageHandler = new MainTabPageHandler(this, swipePageAdapter, floatingActionButton, tabLayout);
        viewPager.addOnPageChangeListener(tabPageHandler);

        //Drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(new MainDrawerItemHandler(this, drawer));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search);

        int infoTabPos = swipePageAdapter.getPositionFor(InfoTab.class);
        tabPageHandler.setMenuItemConstrains(Collections.singletonList(
                new MainTabPageHandler.MenuItemTabConstraint(menuItem, Collections.singletonList(infoTabPos))));
        tabPageHandler.syncMenuItemsStateFor(viewPager.getCurrentItem());
        infoSearchHandler.setupSearchMenu(menuItem);
        return true;
    }

    //Handle Drawer back button pressing
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) drawer.closeDrawers();
        else if (viewPager.getCurrentItem() == 0) DialogHelper.showConfirmExitDialog(this, "Avslutt", "Er du sikker på at du vil avslutte?");
        else viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @OnClick(R.id.add_triggers)
    public void addTriggers() {
        Intent intent = new Intent(this, TriggerActivity.class);
        intent.putExtra(TriggerActivity.USER_INFO_EXTRA, user);
        startActivityForResult(intent, USER_TRIGGER_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == USER_TRIGGER_REQUEST && resultCode == RESULT_OK) {
            user = data.getExtras().getParcelable(TriggerActivity.USER_INFO_EXTRA);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNotificationReceived(AchievementReceiver.NotificationReceiveListener event) {
        int achievementTabIndex = swipePageAdapter.getPositionFor(AchievementTab.class);
        if (viewPager.getCurrentItem() == achievementTabIndex) {
            EventBus.getDefault().postSticky(new AchievementTab.PresentUnlockedAchievementEvent());
            return;
        }
        tabPageHandler.incrementTabBadgeAtIndex(achievementTabIndex);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        unbinder.unbind();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        navigateViewPagerIfSpecifiedBy(intent);
    }

    private void navigateViewPagerIfSpecifiedBy(Intent intent) {
        if (intent == null) return;
        int initPage = intent.getBooleanExtra(NAV_TO_ACHIEVEMENT_EXTRA, false)
                ? swipePageAdapter.getPositionFor(AchievementTab.class)
                : swipePageAdapter.getPositionFor(HomeTab.class);
        viewPager.setCurrentItem(initPage);
        if (initPage == 0) tabPageHandler.onPageSelected(0); //bug or feature in viewpager..
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void scrollToProgramTab(ProgramTab.TriggerAddedEvent event) {
        viewPager.setCurrentItem(swipePageAdapter.getPositionFor(ProgramTab.class));
    }

    public void refreshUser() {
        user = new UserDao(this).getFirst();
    }
}