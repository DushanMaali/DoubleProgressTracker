package lk.dexter.double_progress_tracker.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import lk.dexter.double_progress_tracker.R;
import lk.dexter.double_progress_tracker.data.entity.Exercise;
import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ViewHolder> {
    private List<Exercise> exercises;
    private OnExerciseActionListener listener;

    public interface OnExerciseActionListener {
        void onEdit(Exercise exercise);
        void onDelete(Exercise exercise);
    }

    public ExerciseAdapter(List<Exercise> exercises, OnExerciseActionListener listener) {
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

        holder.ivMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.inflate(R.menu.exercise_item_menu);
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_edit) {
                    listener.onEdit(e);
                    return true;
                } else if (item.getItemId() == R.id.action_delete) {
                    listener.onDelete(e);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() { return exercises.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTarget, tvSuggestion;
        ImageView ivMenu;
        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvTarget = itemView.findViewById(R.id.tvTarget);
            tvSuggestion = itemView.findViewById(R.id.tvSuggestion);
            ivMenu = itemView.findViewById(R.id.ivMenu);
        }
    }
}