package no.rustelefonen.hap.main.tabs.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import lombok.AllArgsConstructor;
import no.rustelefonen.hap.R;
import no.rustelefonen.hap.main.tabs.achievements.AchievementTab;
import no.rustelefonen.hap.main.tabs.info.InfoTab;
import no.rustelefonen.hap.tabs.misc.SwipePageAdapter;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.primitives.Ints.tryParse;

/**
 * Created by Fredrik on 04.05.2016.
 */
public class MainTabPageHandler implements ViewPager.OnPageChangeListener  {
    public static final String TAB_BADGE_VALUES = "tab_badge_values";

    @AllArgsConstructor
    public static class MenuItemTabConstraint {
        MenuItem menuItem;
        List<Integer> validAtTabIndexes;
    }

    private MainActivity activity;
    private SwipePageAdapter swipePageAdapter;
    private FloatingActionButton fab;
    private TabLayout tabLayout;
    private List<MenuItemTabConstraint> menuItemConstrains;

    public MainTabPageHandler(MainActivity activity, SwipePageAdapter swipePageAdapter, FloatingActionButton fab, TabLayout tabLayout) {
        this.activity = activity;
        this.swipePageAdapter = swipePageAdapter;
        this.fab = fab;
        this.tabLayout = tabLayout;
        setupTabs();
    }

    private void setupTabs() {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab == null) continue;
            tab.setIcon(swipePageAdapter.getPageIconResId(i));

            tab.setCustomView(R.layout.tab_icon_layout);
            View v = tab.getCustomView();
            if (v == null) continue;

            //setting saved tab badge value
            SharedPreferences sharedPreferences = activity.getSharedPreferences(TAB_BADGE_VALUES, Activity.MODE_PRIVATE);
            String badgeValue = ""+sharedPreferences.getInt(swipePageAdapter.getPageTitle(i), 0);
            TextView textView = (TextView) v.findViewById(R.id.badge_text);
            textView.setText(badgeValue);
            if (textView.getText().equals("0")) textView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

    @Override
    public void onPageSelected(int position) {
        EventBus.getDefault().post(new TabSelectedEvent(swipePageAdapter.getClassFor(position)));
        activity.setActionBarTitle(swipePageAdapter.getPageTitle(position));
        syncMenuItemsStateFor(position);
        syncFabState(position);

        //AchievementTab Badge clearing
        if (position == swipePageAdapter.getPositionFor(AchievementTab.class)){
            boolean didClear = clearTabBadgeAtIndex(position);
            if(didClear) EventBus.getDefault().postSticky(new AchievementTab.PresentUnlockedAchievementEvent());
        }
    }

    public void syncMenuItemsStateFor(int position){
        if(menuItemConstrains == null) return;
        for (MenuItemTabConstraint menuItemConstrain : menuItemConstrains) {
            if(menuItemConstrain.validAtTabIndexes.contains(position)){
                menuItemConstrain.menuItem.setVisible(true);
            }
            else menuItemConstrain.menuItem.setVisible(false);
        }
    }

    private void syncFabState(int position){
        if (position != swipePageAdapter.getPositionFor(InfoTab.class)) fab.show();
        else fab.hide();
    }

    private boolean clearTabBadgeAtIndex(int i){
        View v = getViewForTabAt(i);
        if (v == null) return false;

        TextView textView = (TextView) v.findViewById(R.id.badge_text);
        if (textView.getText().length() <= 0 || textView.getText().equals("0")) return false;

        textView.setText(null);
        textView.setVisibility(View.INVISIBLE);
        persistTabBadgeNumber(i, 0);
        return true;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    public void incrementTabBadgeAtIndex(int i){
        View v = getViewForTabAt(i);
        if (v == null) return;

        TextView textView = (TextView) v.findViewById(R.id.badge_text);
        Integer badgeValue = firstNonNull(tryParse(textView.getText().toString()), 0);

        badgeValue++;
        textView.setText(String.valueOf(badgeValue));
        textView.setVisibility(View.VISIBLE);

        persistTabBadgeNumber(i, badgeValue);
    }

    private View getViewForTabAt(int position) {
        TabLayout.Tab tab = tabLayout.getTabAt(position);
        return tab != null ? tab.getCustomView() : null;
    }

    private void persistTabBadgeNumber(int tabIndex, int value) {
        SharedPreferences.Editor editor = activity.getSharedPreferences(TAB_BADGE_VALUES, Activity.MODE_PRIVATE).edit();
        editor.putInt(swipePageAdapter.getPageTitle(tabIndex), value);
        editor.commit(); //commit needed, apply does not seem to work in this case
    }

    public void setMenuItemConstrains(List<MenuItemTabConstraint> menuItemConstrains) {
        this.menuItemConstrains = menuItemConstrains;
    }

    @AllArgsConstructor
    public static class TabSelectedEvent {
        public Class tabClass;
    }
}
