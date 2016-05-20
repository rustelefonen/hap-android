package no.rustelefonen.hap.main.tabs.activity;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.MenuItem;
import android.view.View;

import java.util.List;

import no.rustelefonen.hap.R;
import no.rustelefonen.hap.entities.Info;
import no.rustelefonen.hap.lists.misc.DividerItemDecoration;
import no.rustelefonen.hap.main.tabs.info.InfoDetailActivity;
import no.rustelefonen.hap.main.tabs.info.adapters.InfoAdapter;
import no.rustelefonen.hap.main.tabs.info.brain.BrainActivity;
import no.rustelefonen.hap.persistence.dao.InfoDao;

/**
 * Created by Fredrik on 05.05.2016.
 */
public class InfoSearchHandler implements SearchView.OnQueryTextListener{
    private MainActivity activity;
    private MenuItem menuSearch;
    private TabLayout tabLayout;
    private RecyclerView recyclerView;

    public InfoSearchHandler(MainActivity activity, RecyclerView recyclerView) {
        this(activity, recyclerView, null);
    }

    public InfoSearchHandler(MainActivity activity,RecyclerView recyclerView, TabLayout tabLayout) {
        this.activity = activity;
        this.recyclerView = recyclerView;
        this.tabLayout = tabLayout;
        initRecyclerView();
    }

    private void initRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.addItemDecoration(new DividerItemDecoration(activity));
        recyclerView.setVisibility(View.GONE);
    }

    public void setupSearchMenu(MenuItem menuSearch){
        this.menuSearch = menuSearch;
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuSearch);
        searchView.setQueryHint(activity.getString(R.string.search_hint));
        searchView.setOnQueryTextListener(this);
        searchView.setIconified(true);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if(newText.isEmpty()) {
            hideResultView();
            return true;
        }

        final List<Info> searchResult = new InfoDao(activity).searchInfoTitles(newText);
        if(searchResult.isEmpty()){
            hideResultView();
            return true;
        }

        InfoAdapter infoAdapter = new InfoAdapter(searchResult, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = recyclerView.getChildAdapterPosition(v);
                Info info = searchResult.get(position);
                if (info.getTitle().equalsIgnoreCase("3d-hjernen")) {
                    activity.startActivity(new Intent(activity, BrainActivity.class));
                }
                else {
                    Intent intent = new Intent(activity, InfoDetailActivity.class);
                    intent.putExtra(InfoDetailActivity.HELP_INFO_ID_EXTRA, info.getId());
                    activity.startActivity(intent);
                }
                menuSearch.collapseActionView(); // To collapse menu and reset the search when going back
                hideResultView();
            }
        });
        recyclerView.setAdapter(infoAdapter);
        showResultView();
        return true;
    }

    private void hideResultView(){
        tabLayout.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        recyclerView.setAdapter(null);
    }

    private void showResultView(){
        recyclerView.setVisibility(View.VISIBLE);
        tabLayout.setVisibility(View.GONE);
    }
}
