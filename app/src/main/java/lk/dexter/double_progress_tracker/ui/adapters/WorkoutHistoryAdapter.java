package lk.dexter.double_progress_tracker.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import lk.dexter.double_progress_tracker.R;
import lk.dexter.double_progress_tracker.ui.WorkoutSummaryActivity;
import java.util.List;
import java.util.Locale;

public class WorkoutHistoryAdapter extends RecyclerView.Adapter<WorkoutHistoryAdapter.ViewHolder> {
    private List<WorkoutSummaryActivity.WorkoutDay> workoutDays;

    public WorkoutHistoryAdapter(List<WorkoutSummaryActivity.WorkoutDay> workoutDays) {
        this.workoutDays = workoutDays;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_history_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkoutSummaryActivity.WorkoutDay day = workoutDays.get(position);
        holder.tvDate.setText(day.date);

        holder.exercisesContainer.removeAllViews();
        for (WorkoutSummaryActivity.ExerciseEntry exercise : day.exercises) {
            View exerciseView = LayoutInflater.from(holder.itemView.getContext())
                    .inflate(R.layout.item_exercise_row, holder.exercisesContainer, false);

            TextView tvExerciseName = exerciseView.findViewById(R.id.tvExerciseName);
            LinearLayout setsContainer = exerciseView.findViewById(R.id.setsContainer);

            tvExerciseName.setText(exercise.exerciseName);
            setsContainer.removeAllViews();

            for (WorkoutSummaryActivity.SetData set : exercise.sets) {
                TextView setView = new TextView(holder.itemView.getContext());
                setView.setText(String.format(Locale.getDefault(), "%.1f×%d", set.weight, set.reps));
                setView.setTextSize(14);
                setView.setTextColor(holder.itemView.getContext().getColor(R.color.text_primary));
                setView.setPadding(12, 4, 12, 4);
                setView.setBackgroundResource(R.drawable.bg_set_chip);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMarginEnd(8);
                setsContainer.addView(setView, params);
            }

            holder.exercisesContainer.addView(exerciseView);
        }
    }

    @Override
    public int getItemCount() { return workoutDays.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        LinearLayout exercisesContainer;

        ViewHolder(View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            exercisesContainer = itemView.findViewById(R.id.exercisesContainer);
        }
    }
}