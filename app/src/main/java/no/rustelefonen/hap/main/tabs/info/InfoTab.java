package no.rustelefonen.hap.main.tabs.info;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import no.rustelefonen.hap.R;
import no.rustelefonen.hap.entities.Category;
import no.rustelefonen.hap.entities.Info;
import no.rustelefonen.hap.lists.misc.DividerItemDecoration;
import no.rustelefonen.hap.main.tabs.info.adapters.InfoCategoryAdapter;
import no.rustelefonen.hap.main.tabs.info.brain.BrainActivity;
import no.rustelefonen.hap.persistence.OrmLiteActivity;
import no.rustelefonen.hap.persistence.dao.InfoDao;
import no.rustelefonen.hap.tabs.misc.TabPage;

/**
 * Created by Fredrik on 19.01.2016.
 */
public class InfoTab extends TabPage{
    @BindView(R.id.info_list_recyclerview) RecyclerView recyclerView;
    private Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.info_tab, container, false);
        unbinder = ButterKnife.bind(this, v);
        EventBus.getDefault().register(this);
        initRecyclerView();
        populateList();
        return v;
    }

    private void initRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setFocusable(false);
    }

    private void populateList() {
        InfoDao infoDao = new InfoDao((OrmLiteActivity) getActivity());
        final List<Category> items = infoDao.getAllInfoCategories();
        InfoCategoryAdapter infoCategoryAdapter = new InfoCategoryAdapter(items, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = recyclerView.getChildAdapterPosition(v);
                Intent intent;
                List<Info> infoList = items.get(position).getInfoList();
                if (infoList.size() <= 1) {
                    Info selectedInfo = infoList.get(0);
                    if(selectedInfo.getTitle().equalsIgnoreCase("3d-hjernen")){
                        intent = new Intent(getContext(), BrainActivity.class);
                    } else {
                        intent = new Intent(getContext(), InfoDetailActivity.class);
                        intent.putExtra(InfoDetailActivity.HELP_INFO_ID_EXTRA, selectedInfo.getId());
                    }
                } else {
                    intent = new Intent(getContext(), InfoCategoryActivity.class);
                    intent.putExtra(InfoCategoryActivity.CATEGORY_ID_EXTRA, items.get(position).getId());
                }
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(infoCategoryAdapter);
    }

    @OnClick(R.id.call_row)
    public void callRusTlf() {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + "08588")));
    }

    @OnClick(R.id.chat_row)
    public void startChatActivity() {
        startActivity(new Intent(getContext(), ContactRustlfActivity.class));
    }

    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        unbinder.unbind();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void reloadHelpInfoList(UpdateHelpInfoListEvent event) {
        populateList();
    }

    public static class UpdateHelpInfoListEvent {}
}
