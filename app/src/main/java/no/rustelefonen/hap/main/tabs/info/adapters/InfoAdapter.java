package no.rustelefonen.hap.main.tabs.info.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import no.rustelefonen.hap.R;
import no.rustelefonen.hap.entities.Info;

/**
 * Created by lon on 21/04/16.
 */
public class InfoAdapter extends RecyclerView.Adapter<InfoAdapter.ViewHolder> {
    private List<Info> items;
    private View.OnClickListener onClickListener;

    public InfoAdapter(List<Info> items, View.OnClickListener onClickListener) {
        this.items = items;
        this.onClickListener = onClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.info_list_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.title.setText(items.get(position).getTitle());
        holder.itemView.setOnClickListener(onClickListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        public ViewHolder(View itemView) {
            super(itemView);
            this.title = (TextView) itemView.findViewById(R.id.row_title);
            itemView.findViewById(R.id.row_icon).setVisibility(View.GONE);
        }
    }
}
