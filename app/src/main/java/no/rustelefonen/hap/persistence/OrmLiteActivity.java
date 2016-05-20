package no.rustelefonen.hap.persistence;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import no.rustelefonen.hap.R;

/**
 * Created by lon on 25/02/16.
 */
public class OrmLiteActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener{
    private DatabaseHelper databaseHelper;
    protected Toolbar toolbar;
    protected AppBarLayout appBarLayout;
    private int appBarScrolledOffset;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        initToolbar();
        setDatabaseHelper(OpenHelperManager.getHelper(this, DatabaseHelper.class));
    }

    public void initToolbar() {
        appBarLayout = (AppBarLayout) findViewById(R.id.toolbar_top_layout);
        if(appBarLayout != null) appBarLayout.addOnOffsetChangedListener(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar_top);
        if(toolbar == null) return;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { onBackPressed(); }
        });
    }

    public void setActionBarTitle(String title){
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(title);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        OpenHelperManager.releaseHelper();
    }

    public DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }

    private void setDatabaseHelper(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        appBarScrolledOffset = verticalOffset;
    }

    public int getAppBarScrolledOffset() {
        return appBarScrolledOffset;
    }

    public int getAppBarHeight(){
        return appBarLayout.getHeight();
    }

    public void onNestedFling(int xVel, int yVel){
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();

        if (behavior != null) {
            behavior.onNestedFling((CoordinatorLayout)appBarLayout.getParent(), appBarLayout, null, xVel, yVel, true);
        }
    }
}
