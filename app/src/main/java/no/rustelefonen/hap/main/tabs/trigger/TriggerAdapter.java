package no.rustelefonen.hap.main.tabs.trigger;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import no.rustelefonen.hap.R;

/**
 * Created by martinnikolaisorlie on 23/02/16.
 */
public class TriggerAdapter extends RecyclerView.Adapter<TriggerAdapter.ViewHolder> {

    private List<TriggerActivity.CheckableTrigger> triggers;
    private View.OnClickListener onClickListener;

    public TriggerAdapter(List<TriggerActivity.CheckableTrigger> triggers, View.OnClickListener onClickListener){
        this.triggers = triggers;
        this.onClickListener = onClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.trigger_list_row, parent, false);
        v.setOnClickListener(onClickListener);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.icon.setImageResource(triggers.get(position).trigger.getImageId());
        holder.label.setText(triggers.get(position).trigger.getTitle());
        boolean isChecked = triggers.get(position).isChecked;
        holder.checkmark.setAlpha(isChecked ? 1f : 0f);
    }

    @Override
    public int getItemCount() {
        return triggers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView icon;
        ImageView checkmark;
        TextView label;

        public ViewHolder (View itemView){
            super(itemView);
            this.icon = (ImageView) itemView.findViewById(R.id.situation_icon);
            this.checkmark = (ImageView) itemView.findViewById(R.id.passed_situation_checkmark);
            this.label = (TextView) itemView.findViewById(R.id.situation_label);
        }
    }
}
