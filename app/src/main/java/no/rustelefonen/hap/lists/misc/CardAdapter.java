package no.rustelefonen.hap.lists.misc;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import no.rustelefonen.hap.R;
import no.rustelefonen.hap.util.Expandable;

/**
 * Created by Fredrik on 17/02/16.
 */
public abstract class CardAdapter<C, T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {
    private List<SubAdapter> subAdapters;
    private boolean expandableRows;

    private Set<Integer> expandedRows;

    public CardAdapter(List<C> cards) {
        subAdapters = new ArrayList<>();
        expandedRows = new HashSet<>();

        for(C card : cards){
            subAdapters.add(new SubAdapter(card, getCountForCard(card)));
        }
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_row, parent, false);
        CardViewHolder cardViewHolder = new CardViewHolder(v);
        cardViewHolder.arrow.setVisibility(expandableRows ? View.VISIBLE : View.GONE);
        return new CardViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final CardViewHolder holder, final int position) {
        holder.title.setText(getCardTitle(position));
        holder.recyclerView.setAdapter(subAdapters.get(position));
        if(!expandableRows) return;

        initRowExpandState(holder, position);
        holder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (expandedRows.contains(position)) {
                    holder.arrow.animate().rotation(0).setDuration(200).start();
                    Expandable.collapse(holder.recyclerView);
                    expandedRows.remove(position);

                } else {
                    holder.arrow.animate().rotation(90).setDuration(200).start();
                    Expandable.expand(holder.recyclerView);
                    expandedRows.add(position);
                }
            }
        });
    }

    private void initRowExpandState(CardViewHolder holder, int position){
        if(expandedRows.contains(position)){
            holder.arrow.setRotation(90);
            holder.recyclerView.setVisibility(View.VISIBLE);
            holder.recyclerView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        else{
            holder.arrow.setRotation(0);
            holder.recyclerView.setVisibility(View.GONE);
            holder.recyclerView.getLayoutParams().height = 0;
        }
    }

    @Override
    public int getItemCount() {
        return subAdapters.size();
    }

    public boolean isExpandableRows() {
        return expandableRows;
    }

    public void setExpandableRows(boolean expandableRows) {
        this.expandableRows = expandableRows;
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView arrow;
        RecyclerView recyclerView;

        public CardViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.achievement_card_header);
            arrow = (ImageView) itemView.findViewById(R.id.list_arrow);
            recyclerView = (RecyclerView) itemView.findViewById(R.id.card_recyclerview);

            recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
            recyclerView.setNestedScrollingEnabled(false);
            recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext()));
        }
    }

    //Abstract and Overrideable methods
    public abstract String getCardTitle(int position);

    public abstract int getCountForCard(C card);

    public abstract T onCreateSubViewHolder(ViewGroup parent, int viewType);

    public abstract void onBindSubViewHolder(T holder, C card, int position);

    public void onSubViewRecycled(T holder){}

    public SubAdapter getSubAdatperForSection(int section){
        return subAdapters.get(section);
    }

    public class SubAdapter extends RecyclerView.Adapter<T> {
        private int itemCount;
        private C card;

        public SubAdapter(C card, int itemCount) {
            this.itemCount = itemCount;
            this.card = card;
        }

        @Override
        public T onCreateViewHolder(ViewGroup parent, int viewType) {
            return onCreateSubViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(T holder, int position) {
            CardAdapter.this.onBindSubViewHolder(holder, card, position);
        }

        @Override
        public int getItemCount() {
            return itemCount;
        }

        @Override
        public void onViewRecycled(T holder){
            super.onViewRecycled(holder);
            onSubViewRecycled(holder);
        }
    }

    public interface SubItemOnClickListener<C> {
        void onClick(View v, C card, int position);
    }
}