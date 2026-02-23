package lk.dexter.double_progress_tracker.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import lk.dexter.double_progress_tracker.R;
import lk.dexter.double_progress_tracker.data.entity.Exercise;
import lk.dexter.double_progress_tracker.data.entity.SetLog;
import lk.dexter.double_progress_tracker.data.repository.WorkoutRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogWorkoutAdapter extends RecyclerView.Adapter<LogWorkoutAdapter.ViewHolder> {
    private List<Exercise> exercises;
    private WorkoutRepository repository;
    private Map<Integer, List<SetLog>> previousRecords = new HashMap<>();
    private Map<Integer, Double> enteredWeights = new HashMap<>();
    private Map<Integer, List<EditText>> setRepsEditTexts = new HashMap<>();

    public LogWorkoutAdapter(List<Exercise> exercises, WorkoutRepository repository) {
        this.exercises = exercises;
        this.repository = repository;
        loadPreviousRecords();
    }

    private void loadPreviousRecords() {
        new Thread(() -> {
            for (Exercise e : exercises) {
                List<SetLog> last = repository.getLatestSetLogsForExercise(e.getId());
                previousRecords.put(e.getId(), last);
            }
        }).start();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_log_exercise, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Exercise exercise = exercises.get(position);
        holder.bind(exercise);
    }

    @Override
    public int getItemCount() { return exercises.size(); }

    public Map<Integer, Double> getWeights() { return enteredWeights; }

    public Map<Integer, List<Integer>> getRepsLists() {
        Map<Integer, List<Integer>> result = new HashMap<>();
        for (Map.Entry<Integer, List<EditText>> entry : setRepsEditTexts.entrySet()) {
            List<Integer> reps = new ArrayList<>();
            for (EditText et : entry.getValue()) {
                String text = et.getText().toString().trim();
                if (!text.isEmpty()) {
                    try {
                        reps.add(Integer.parseInt(text));
                    } catch (NumberFormatException e) {
                        // ignore invalid entries
                    }
                }
            }
            if (!reps.isEmpty()) {
                result.put(entry.getKey(), reps);
            }
        }
        return result;
    }

    // Helper to get target reps for an exercise (for double progression check)
    public Map<Integer, int[]> getTargetRepsMap() {
        Map<Integer, int[]> map = new HashMap<>();
        for (Exercise e : exercises) {
            map.put(e.getId(), e.getTargetRepsArray());
        }
        return map;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvExerciseName, tvTarget, tvPrevious;
        EditText etWeight;
        Button btnAddSet;
        LinearLayout setsContainer;
        Exercise currentExercise;
        List<EditText> setEditTexts;

        ViewHolder(View itemView) {
            super(itemView);
            tvExerciseName = itemView.findViewById(R.id.tvExerciseName);
            tvTarget = itemView.findViewById(R.id.tvTarget);
            tvPrevious = itemView.findViewById(R.id.tvPrevious);
            etWeight = itemView.findViewById(R.id.etWeight);
            btnAddSet = itemView.findViewById(R.id.btnAddSet);
            setsContainer = itemView.findViewById(R.id.setsContainer);
        }

        void bind(Exercise exercise) {
            currentExercise = exercise;
            setEditTexts = new ArrayList<>();
            setRepsEditTexts.put(exercise.getId(), setEditTexts);

            tvExerciseName.setText(exercise.getName());

            // Show target reps per set
            int[] targetReps = exercise.getTargetRepsArray();
            StringBuilder targetStr = new StringBuilder("Targets: ");
            for (int t : targetReps) {
                targetStr.append(t).append(" ");
            }
            tvTarget.setText(targetStr.toString().trim());

            // Show previous record
            List<SetLog> prev = previousRecords.get(exercise.getId());
            if (prev != null && !prev.isEmpty()) {
                StringBuilder sb = new StringBuilder("Previous: ");
                for (SetLog s : prev) {
                    sb.append(s.getWeight()).append("kg × ").append(s.getReps()).append(" ");
                }
                tvPrevious.setText(sb.toString());
            } else {
                tvPrevious.setText("No previous log");
            }

            // Pre-fill weight with suggestion or starting weight
            double weight = exercise.getSuggestedNextWeight() > 0 ?
                    exercise.getSuggestedNextWeight() : exercise.getStartingWeight();
            etWeight.setText(String.valueOf(weight));
            enteredWeights.put(exercise.getId(), weight);

            // Clear container and add set rows based on targetReps length
            setsContainer.removeAllViews();
            for (int i = 0; i < targetReps.length; i++) {
                addSetRow(i + 1, targetReps[i], null);
            }

            // Weight change listener
            etWeight.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    try {
                        double w = Double.parseDouble(etWeight.getText().toString());
                        enteredWeights.put(exercise.getId(), w);
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }
            });

            // Add set button (allows extra sets beyond target)
            btnAddSet.setOnClickListener(v -> {
                int nextSetNumber = setEditTexts.size() + 1;
                addSetRow(nextSetNumber, 0, null); // no target for extra sets
            });
        }

        private void addSetRow(int setNumber, int targetRep, Integer initialReps) {
            LayoutInflater inflater = LayoutInflater.from(itemView.getContext());
            View row = inflater.inflate(R.layout.item_set_row, setsContainer, false);
            TextView tvSetNumber = row.findViewById(R.id.tvSetNumber);
            EditText etSetReps = row.findViewById(R.id.etSetReps);

            tvSetNumber.setText("Set " + setNumber);
            if (targetRep > 0) {
                etSetReps.setHint("Target: " + targetRep);
            } else {
                etSetReps.setHint("Reps");
            }
            if (initialReps != null) {
                etSetReps.setText(String.valueOf(initialReps));
            }

            setsContainer.addView(row);
            setEditTexts.add(etSetReps);
        }
    }
}