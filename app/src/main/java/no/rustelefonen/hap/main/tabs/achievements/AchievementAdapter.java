package no.rustelefonen.hap.main.tabs.achievements;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import no.rustelefonen.hap.R;
import no.rustelefonen.hap.entities.Achievement;

/**
 * Created by martinnikolaisorlie on 28/01/16.
 */
public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.ViewHolder> {
    private List<Achievement> items;
    private ProgressValueCallback progressValueCallback;

    public AchievementAdapter(List<Achievement> items) {
        this(items, null);
    }

    public AchievementAdapter(List<Achievement> items, ProgressValueCallback progressValueCallback) {
        this.items = items;
        this.progressValueCallback = progressValueCallback;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.achievement_list_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Achievement achievement = items.get(position);
        holder.title.setText(achievement.getTitle());
        holder.description.setText(achievement.getDescription());

        int progress = holder.progressBar.getMax();
        if(progressValueCallback != null) progress *= progressValueCallback.getProgressFor(achievement);

        holder.progressBar.setProgress(progress);
        holder.icon.setImageResource(achievement.getImageId(progress >= holder.progressBar.getMax()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public List<Achievement> getItems() {
        return items;
    }

    public void setItems(List<Achievement> items) {
        this.items = items;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView description;
        ImageView icon;
        ProgressBar progressBar;

        public ViewHolder(View itemView) {
            super(itemView);
            this.title = (TextView) itemView.findViewById(R.id.achievement_title);
            this.description = (TextView) itemView.findViewById(R.id.achievement_description);
            this.icon = (ImageView) itemView.findViewById(R.id.achievement_list_badge);
            this.progressBar = (ProgressBar) itemView.findViewById(R.id.achievement_progress_bar);
        }
    }

    public interface ProgressValueCallback{
        double getProgressFor(Achievement achievement);
    }
}
