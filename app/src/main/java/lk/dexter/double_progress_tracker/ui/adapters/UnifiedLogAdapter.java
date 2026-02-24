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
import lk.dexter.double_progress_tracker.ui.model.SupersetGroup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnifiedLogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_SINGLE_EXERCISE = 0;
    private static final int TYPE_SUPERSET = 1;

    private List<Object> items; // Exercise or SupersetGroup
    private WorkoutRepository repository;
    private Map<Integer, List<SetLog>> previousRecords = new HashMap<>();
    private Map<Integer, Double> enteredWeights = new HashMap<>();
    private Map<Integer, List<EditText>> setRepsEditTexts = new HashMap<>();

    // Helper class for sorting
    private static class ItemWrapper {
        int order;
        Object item; // Exercise or SupersetGroup
        ItemWrapper(int order, Object item) {
            this.order = order;
            this.item = item;
        }
    }

    public UnifiedLogAdapter(List<Exercise> exercises, WorkoutRepository repository) {
        this.repository = repository;
        this.items = groupExercises(exercises);
        loadPreviousRecords();
    }

    private List<Object> groupExercises(List<Exercise> exercises) {
        Map<Integer, List<Exercise>> supersetMap = new HashMap<>();
        List<Exercise> singles = new ArrayList<>();
        List<ItemWrapper> wrappers = new ArrayList<>();

        // Separate singles and superset exercises
        for (Exercise e : exercises) {
            if (e.getSupersetId() != null) {
                supersetMap.computeIfAbsent(e.getSupersetId(), k -> new ArrayList<>()).add(e);
            } else {
                singles.add(e);
            }
        }

        // Add singles as wrappers
        for (Exercise s : singles) {
            wrappers.add(new ItemWrapper(s.getOrderIndex(), s));
        }

        // For each superset, compute the minimum orderIndex and create a group
        for (Map.Entry<Integer, List<Exercise>> entry : supersetMap.entrySet()) {
            int minOrder = entry.getValue().stream()
                    .mapToInt(Exercise::getOrderIndex)
                    .min()
                    .orElse(Integer.MAX_VALUE);

            // Sort exercises within superset by supersetOrder
            List<Exercise> sortedExercises = new ArrayList<>(entry.getValue());
            sortedExercises.sort(Comparator.comparingInt(Exercise::getSupersetOrder));

            SupersetGroup group = new SupersetGroup(entry.getKey(), sortedExercises);
            wrappers.add(new ItemWrapper(minOrder, group));
        }

        // Sort all wrappers by order (oldest first = lowest orderIndex)
        wrappers.sort(Comparator.comparingInt(w -> w.order));

        // Extract items in sorted order
        List<Object> grouped = new ArrayList<>();
        for (ItemWrapper w : wrappers) {
            grouped.add(w.item);
        }
        return grouped;
    }

    private void loadPreviousRecords() {
        new Thread(() -> {
            for (Object obj : items) {
                if (obj instanceof Exercise) {
                    Exercise e = (Exercise) obj;
                    List<SetLog> last = repository.getLatestSetsForExercise(e.getId());
                    previousRecords.put(e.getId(), last);
                } else if (obj instanceof SupersetGroup) {
                    for (Exercise e : ((SupersetGroup) obj).getExercises()) {
                        List<SetLog> last = repository.getLatestSetsForExercise(e.getId());
                        previousRecords.put(e.getId(), last);
                    }
                }
            }
        }).start();
    }

    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        if (item instanceof Exercise) return TYPE_SINGLE_EXERCISE;
        else return TYPE_SUPERSET;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SINGLE_EXERCISE) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_log_exercise, parent, false);
            return new SingleExerciseViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_superset_header, parent, false);
            return new SupersetViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SingleExerciseViewHolder) {
            Exercise exercise = (Exercise) items.get(position);
            ((SingleExerciseViewHolder) holder).bind(exercise);
        } else if (holder instanceof SupersetViewHolder) {
            SupersetGroup group = (SupersetGroup) items.get(position);
            ((SupersetViewHolder) holder).bind(group);
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

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
        for (Object obj : items) {
            if (obj instanceof Exercise) {
                Exercise e = (Exercise) obj;
                map.put(e.getId(), e.getTargetRepsArray());
            } else if (obj instanceof SupersetGroup) {
                for (Exercise e : ((SupersetGroup) obj).getExercises()) {
                    map.put(e.getId(), e.getTargetRepsArray());
                }
            }
        }
        return map;
    }

    // ---------- ViewHolder for single exercise ----------
    class SingleExerciseViewHolder extends RecyclerView.ViewHolder {
        TextView tvExerciseName, tvPreviousDateWeight;
        EditText etCurrentWeight;
        Button btnAddSet;
        LinearLayout previousSetsContainer, ongoingSetsContainer;
        Exercise currentExercise;
        List<EditText> setEditTexts;

        SingleExerciseViewHolder(View itemView) {
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
            setEditTexts = new ArrayList<>();
            setRepsEditTexts.put(exercise.getId(), setEditTexts);

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
                            .inflate(R.layout.item_set_row_previous, previousSetsContainer, false);
                    TextView tvSetNumber = row.findViewById(R.id.tvSetNumber);
                    TextView tvTarget = row.findViewById(R.id.tvTargetReps);
                    TextView tvPrevReps = row.findViewById(R.id.tvPreviousReps);

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

            // Current weight
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
            setEditTexts.clear();
            ongoingSetsContainer.removeAllViews();
            for (int i = 0; i < targetReps.length; i++) {
                addCurrentSetRow(i + 1, targetReps[i], null);
            }

            // Add set button
            btnAddSet.setOnClickListener(v -> {
                int nextSetNumber = setEditTexts.size() + 1;
                addCurrentSetRow(nextSetNumber, 0, null);
            });
        }

        private void addCurrentSetRow(int setNumber, int targetRep, Integer initialReps) {
            View row = LayoutInflater.from(itemView.getContext())
                    .inflate(R.layout.item_set_row_ongoing, ongoingSetsContainer, false);
            TextView tvSetNumber = row.findViewById(R.id.tvSetNumber);
            TextView tvTargetHint = row.findViewById(R.id.tvTargetHint);
            EditText etCurrent = row.findViewById(R.id.etCurrentReps);

            tvSetNumber.setText("Set " + setNumber + ":");
            if (targetRep > 0) {
                tvTargetHint.setText("(" + targetRep + ")");
            } else {
                tvTargetHint.setText("");
            }
            if (initialReps != null) {
                etCurrent.setText(String.valueOf(initialReps));
            }
            ongoingSetsContainer.addView(row);
            setEditTexts.add(etCurrent);
        }
    }

    // ---------- ViewHolder for superset group ----------
    class SupersetViewHolder extends RecyclerView.ViewHolder {
        LinearLayout container;

        SupersetViewHolder(View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.supersetExercisesContainer);
        }

        void bind(SupersetGroup group) {
            container.removeAllViews();
            for (Exercise exercise : group.getExercises()) {
                // Inflate item_log_exercise for each exercise in the superset
                View exerciseView = LayoutInflater.from(itemView.getContext())
                        .inflate(R.layout.item_log_exercise, container, false);
                bindExerciseView(exerciseView, exercise);
                container.addView(exerciseView);
            }
        }

        private void bindExerciseView(View view, Exercise exercise) {
            TextView tvName = view.findViewById(R.id.tvExerciseName);
            TextView tvPrev = view.findViewById(R.id.tvPreviousDateWeight);
            EditText etWeight = view.findViewById(R.id.etCurrentWeight);
            Button btnAdd = view.findViewById(R.id.btnAddSet);
            LinearLayout prevContainer = view.findViewById(R.id.previousSetsContainer);
            LinearLayout ongoingContainer = view.findViewById(R.id.ongoingSetsContainer);

            tvName.setText(exercise.getName());

            int[] targetReps = exercise.getTargetRepsArray();

            // Previous sets
            List<SetLog> prevSets = previousRecords.get(exercise.getId());
            if (prevSets != null && !prevSets.isEmpty()) {
                double prevWeight = prevSets.get(0).getWeight();
                tvPrev.setText("Last: " + prevWeight + " kg");
                prevContainer.removeAllViews();
                for (int i = 0; i < prevSets.size(); i++) {
                    SetLog set = prevSets.get(i);
                    View row = LayoutInflater.from(itemView.getContext())
                            .inflate(R.layout.item_set_row_previous, prevContainer, false);
                    TextView tvSetNumber = row.findViewById(R.id.tvSetNumber);
                    TextView tvTarget = row.findViewById(R.id.tvTargetReps);
                    TextView tvPrevReps = row.findViewById(R.id.tvPreviousReps);

                    tvSetNumber.setText("Set " + (i + 1));
                    if (i < targetReps.length) {
                        tvTarget.setText(String.valueOf(targetReps[i]));
                    } else {
                        tvTarget.setText("-");
                    }
                    tvPrevReps.setText(String.valueOf(set.getReps()));
                    prevContainer.addView(row);
                }
            } else {
                tvPrev.setText("No previous log");
                prevContainer.removeAllViews();
            }

            // Current weight
            double suggestedWeight = exercise.getSuggestedNextWeight() > 0 ?
                    exercise.getSuggestedNextWeight() : exercise.getStartingWeight();
            etWeight.setText(String.valueOf(suggestedWeight));
            enteredWeights.put(exercise.getId(), suggestedWeight);
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

            // Current sets
            List<EditText> setEditTextsForExercise = new ArrayList<>();
            setRepsEditTexts.put(exercise.getId(), setEditTextsForExercise);
            ongoingContainer.removeAllViews();
            for (int i = 0; i < targetReps.length; i++) {
                addCurrentSetRowToContainer(ongoingContainer, setEditTextsForExercise, i + 1, targetReps[i], null);
            }

            // Add set button
            btnAdd.setOnClickListener(v -> {
                int nextSetNumber = setEditTextsForExercise.size() + 1;
                addCurrentSetRowToContainer(ongoingContainer, setEditTextsForExercise, nextSetNumber, 0, null);
            });
        }

        private void addCurrentSetRowToContainer(LinearLayout container, List<EditText> setEditTexts,
                                                 int setNumber, int targetRep, Integer initialReps) {
            View row = LayoutInflater.from(container.getContext())
                    .inflate(R.layout.item_set_row_ongoing, container, false);
            TextView tvSetNumber = row.findViewById(R.id.tvSetNumber);
            TextView tvTargetHint = row.findViewById(R.id.tvTargetHint);
            EditText etCurrent = row.findViewById(R.id.etCurrentReps);

            tvSetNumber.setText("Set " + setNumber + ":");
            if (targetRep > 0) {
                tvTargetHint.setText("(" + targetRep + ")");
            } else {
                tvTargetHint.setText("");
            }
            if (initialReps != null) {
                etCurrent.setText(String.valueOf(initialReps));
            }
            container.addView(row);
            setEditTexts.add(etCurrent);
        }
    }
}