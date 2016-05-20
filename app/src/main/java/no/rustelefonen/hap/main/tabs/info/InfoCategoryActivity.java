package no.rustelefonen.hap.main.tabs.info;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import no.rustelefonen.hap.R;
import no.rustelefonen.hap.entities.Info;
import no.rustelefonen.hap.entities.Category;
import no.rustelefonen.hap.lists.misc.DividerItemDecoration;
import no.rustelefonen.hap.main.tabs.info.adapters.InfoAdapter;
import no.rustelefonen.hap.main.tabs.info.brain.BrainActivity;
import no.rustelefonen.hap.persistence.OrmLiteActivity;
import no.rustelefonen.hap.persistence.dao.InfoDao;

/**
 * Created by lon on 21/04/16.
 */
public class InfoCategoryActivity extends OrmLiteActivity {
    public static String CATEGORY_ID_EXTRA = "category_extra";

    private Category category;
    private Unbinder unbinder;
    @BindView(R.id.info_list_recyclerview) RecyclerView recyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.info_category_activity);
        super.onCreate(savedInstanceState);
        unbinder = ButterKnife.bind(this);

        int categoryId = getIntent().getIntExtra(CATEGORY_ID_EXTRA, 0);
        category = new InfoDao(this).getCategoryById(categoryId);
        if(category == null){
            finish();
            return;
        }

        setActionBarTitle(category.getTitle());
        initRecyclerView();
        populateList();
    }

    private void initRecyclerView(){
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setFocusable(false);
    }

    private void populateList() {
        InfoAdapter infoAdapter = new InfoAdapter(category.getInfoList(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = recyclerView.getChildAdapterPosition(v);
                Info info = category.getInfoList().get(position);

                if(info.getTitle().equalsIgnoreCase("3d-hjernen")){
                    startActivity(new Intent(InfoCategoryActivity.this, BrainActivity.class));
                }
                else{
                    Intent intent = new Intent(InfoCategoryActivity.this, InfoDetailActivity.class);
                    intent.putExtra(InfoDetailActivity.HELP_INFO_ID_EXTRA, info.getId());
                    startActivity(intent);
                }
            }
        });
        recyclerView.setAdapter(infoAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
