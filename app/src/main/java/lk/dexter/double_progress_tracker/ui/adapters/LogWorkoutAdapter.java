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
import com.google.android.material.textfield.TextInputLayout;
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
                        // ignore
                    }
                }
            }
            if (!reps.isEmpty()) {
                result.put(entry.getKey(), reps);
            }
        }
        return result;
    }

    public Map<Integer, int[]> getTargetRepsMap() {
        Map<Integer, int[]> map = new HashMap<>();
        for (Exercise e : exercises) {
            map.put(e.getId(), e.getTargetRepsArray());
        }
        return map;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvExerciseName, tvPreviousDateWeight;
        EditText etCurrentWeight;
        Button btnAddSet;
        LinearLayout previousSetsContainer, ongoingSetsContainer;
        Exercise currentExercise;
        List<EditText> setEditTexts;

        ViewHolder(View itemView) {
            super(itemView);
            tvExerciseName = itemView.findViewById(R.id.tvExerciseName);
            tvPreviousDateWeight = itemView.findViewById(R.id.tvPreviousDateWeight);
            etCurrentWeight = itemView.findViewById(R.id.etCurrentWeight);
            btnAddSet = itemView.findViewById(R.id.btnAddSet);
            previousSetsContainer = itemView.findViewById(R.id.previousSetsContainer);
            ongoingSetsContainer = itemView.findViewById(R.id.ongoingSetsContainer);
        }

        void bind(Exercise exercise) {
            currentExercise = exercise;
            tvExerciseName.setText(exercise.getName());

            int[] targetReps = exercise.getTargetRepsArray();

            // Previous section
            List<SetLog> prevSets = previousRecords.get(exercise.getId());
            if (prevSets != null && !prevSets.isEmpty()) {
                double prevWeight = prevSets.get(0).getWeight();
                tvPreviousDateWeight.setText("Last: " + prevWeight + " kg");
                previousSetsContainer.removeAllViews();
                for (int i = 0; i < prevSets.size(); i++) {
                    SetLog set = prevSets.get(i);
                    View row = LayoutInflater.from(itemView.getContext())
                            .inflate(R.layout.item_set_row_three_columns, previousSetsContainer, false);
                    TextView tvSetNumber = row.findViewById(R.id.tvSetNumber);
                    TextView tvTarget = row.findViewById(R.id.tvTargetReps);
                    TextView tvPrevReps = row.findViewById(R.id.tvPreviousReps);
                    // Hide current input
                    row.findViewById(R.id.inputLayoutCurrentReps).setVisibility(View.GONE);
                    tvPrevReps.setVisibility(View.VISIBLE);

                    tvSetNumber.setText("Set " + (i + 1));
                    if (i < targetReps.length) {
                        tvTarget.setText(String.valueOf(targetReps[i]));
                    } else {
                        tvTarget.setText("-");
                    }
                    tvPrevReps.setText(String.valueOf(set.getReps()));
                    previousSetsContainer.addView(row);
                }
            } else {
                tvPreviousDateWeight.setText("No previous log");
                previousSetsContainer.removeAllViews();
            }

            // Current section
            double suggestedWeight = exercise.getSuggestedNextWeight() > 0 ?
                    exercise.getSuggestedNextWeight() : exercise.getStartingWeight();
            etCurrentWeight.setText(String.valueOf(suggestedWeight));
            enteredWeights.put(exercise.getId(), suggestedWeight);
            etCurrentWeight.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    try {
                        double w = Double.parseDouble(etCurrentWeight.getText().toString());
                        enteredWeights.put(exercise.getId(), w);
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }
            });

            // Current sets
            setEditTexts = new ArrayList<>();
            setRepsEditTexts.put(exercise.getId(), setEditTexts);
            ongoingSetsContainer.removeAllViews();
            for (int i = 0; i < targetReps.length; i++) {
                addCurrentSetRow(i + 1, targetReps[i], null);
            }

            // Add set button
            btnAddSet.setOnClickListener(v -> {
                int nextSetNumber = setEditTexts.size() + 1;
                addCurrentSetRow(nextSetNumber, 0, null); // no target for extra sets
            });
        }

        private void addCurrentSetRow(int setNumber, int targetRep, Integer initialReps) {
            View row = LayoutInflater.from(itemView.getContext())
                    .inflate(R.layout.item_set_row_three_columns, ongoingSetsContainer, false);
            TextView tvSetNumber = row.findViewById(R.id.tvSetNumber);
            TextView tvTarget = row.findViewById(R.id.tvTargetReps);
            // Hide previous TextView, show current input
            row.findViewById(R.id.tvPreviousReps).setVisibility(View.GONE);
            TextInputLayout inputLayout = row.findViewById(R.id.inputLayoutCurrentReps);
            inputLayout.setVisibility(View.VISIBLE);
            EditText etCurrent = row.findViewById(R.id.etCurrentReps);

            tvSetNumber.setText("Set " + setNumber);
            if (targetRep > 0) {
                tvTarget.setText(String.valueOf(targetRep));
            } else {
                tvTarget.setText("-");
            }
            if (initialReps != null) {
                etCurrent.setText(String.valueOf(initialReps));
            }
            ongoingSetsContainer.addView(row);
            setEditTexts.add(etCurrent);
        }
    }
}