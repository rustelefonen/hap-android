package no.rustelefonen.hap.tabs.misc;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by martinnikolaisorlie on 11/02/16.
 */
public class SwipePageAdapter extends FragmentPagerAdapter {
    private class Tab {
        Class clazz;
        String title;
        Integer iconRes;
    }

    private List<Tab> tabs;
    private Map<Class, Integer> tabIndexes;

    public SwipePageAdapter(FragmentManager fm) {
        super(fm);
        tabs = new ArrayList<>();
        tabIndexes = new HashMap<>();
    }

    public void addItem(Class page){
        addItem(page, null, 0);
    }

    public void addItem(Class page, String title, int iconRes){
        Tab tab = new Tab();
        tab.clazz = page;
        tab.title = title;
        tab.iconRes = iconRes;
        tabs.add(tab);
        tabIndexes.put(page, tabs.size() - 1);
    }

    @Override
    public Fragment getItem(int position) {
        return TabPage.newInstance(tabs.get(position).clazz);
    }

    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }

    @Override
    public int getCount() {
        return tabs.size();
    }

    @Override
    public String getPageTitle(int position) {
        return tabs.get(position).title;
    }

    public String getPageTitle(Class clazz){
        return getPageTitle(tabIndexes.get(clazz));
    }

    public int getPageIconResId(int position){
        return tabs.get(position).iconRes;
    }

    public int getPageIconResId(Class clazz){
        return getPageIconResId(tabIndexes.get(clazz));
    }

    public int getPositionFor(Class clazz){
        return tabIndexes.get(clazz);
    }

    public Class getClassFor(int position){
        for (Class aClass : tabIndexes.keySet()) {
            if(tabIndexes.get(aClass) == position) return aClass;
        }
        return null;
    }
}
