package lk.dexter.double_progress_tracker.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import lk.dexter.double_progress_tracker.R;
import lk.dexter.double_progress_tracker.data.entity.Exercise;
import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ViewHolder> {
    private List<Exercise> exercises;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Exercise exercise);
    }

    public ExerciseAdapter(List<Exercise> exercises, OnItemClickListener listener) {
        this.exercises = exercises;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Exercise e = exercises.get(position);
        holder.tvName.setText(e.getName());
        int[] targets = e.getTargetRepsArray();
        StringBuilder targetStr = new StringBuilder();
        for (int t : targets) {
            targetStr.append(t).append(" ");
        }
        holder.tvTarget.setText("Targets: " + targetStr.toString().trim());
        if (e.getSuggestedNextWeight() > 0) {
            holder.tvSuggestion.setText("Increase to " + e.getSuggestedNextWeight() + " kg");
            holder.tvSuggestion.setVisibility(View.VISIBLE);
        } else {
            holder.tvSuggestion.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(v -> listener.onItemClick(e));
    }

    @Override
    public int getItemCount() { return exercises.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTarget, tvSuggestion;
        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvTarget = itemView.findViewById(R.id.tvTarget);
            tvSuggestion = itemView.findViewById(R.id.tvSuggestion);
        }
    }
}