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
import lk.dexter.double_progress_tracker.data.entity.DraftSetLog;
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

    private List<Object> items;
    private WorkoutRepository repository;
    private boolean isAdvanced;

    // For normal mode
    private Map<Integer, Double> enteredWeights = new HashMap<>();
    private Map<Integer, List<EditText>> setRepsEditTexts = new HashMap<>();

    // For advanced mode
    private Map<Integer, List<View>> advancedSetViews = new HashMap<>();

    private Map<Integer, List<SetLog>> previousRecords = new HashMap<>();
    private Map<Integer, List<DraftSetLog>> draftSetsByExercise = new HashMap<>();

    public static class SetInput {
        public double weight;
        public int reps;
        public SetInput(double weight, int reps) {
            this.weight = weight;
            this.reps = reps;
        }
    }

    public interface OnExerciseFinishListener {
        void onExerciseFinished(int exerciseId, List<SetInput> setData);
    }
    private OnExerciseFinishListener finishListener;

    private static class ItemWrapper {
        int order;
        Object item;
        ItemWrapper(int order, Object item) {
            this.order = order;
            this.item = item;
        }
    }

    public UnifiedLogAdapter(List<Exercise> exercises, WorkoutRepository repository, String mode,
                             OnExerciseFinishListener listener) {
        this.repository = repository;
        this.isAdvanced = "advanced".equals(mode);
        this.finishListener = listener;
        this.items = groupExercises(exercises);
        loadPreviousRecords();
    }

    public void restoreDrafts(List<DraftSetLog> drafts) {
        draftSetsByExercise.clear();
        for (DraftSetLog draft : drafts) {
            draftSetsByExercise.computeIfAbsent(draft.getExerciseId(), k -> new ArrayList<>()).add(draft);
        }
        notifyDataSetChanged();
    }

    private List<Object> groupExercises(List<Exercise> exercises) {
        Map<Integer, List<Exercise>> supersetMap = new HashMap<>();
        List<Exercise> singles = new ArrayList<>();
        List<ItemWrapper> wrappers = new ArrayList<>();

        for (Exercise e : exercises) {
            if (e.getSupersetId() != null) {
                supersetMap.computeIfAbsent(e.getSupersetId(), k -> new ArrayList<>()).add(e);
            } else {
                singles.add(e);
            }
        }

        for (Exercise s : singles) {
            wrappers.add(new ItemWrapper(s.getOrderIndex(), s));
        }

        for (Map.Entry<Integer, List<Exercise>> entry : supersetMap.entrySet()) {
            int minOrder = entry.getValue().stream()
                    .mapToInt(Exercise::getOrderIndex)
                    .min()
                    .orElse(Integer.MAX_VALUE);

            List<Exercise> sortedExercises = new ArrayList<>(entry.getValue());
            sortedExercises.sort(Comparator.comparingInt(Exercise::getSupersetOrder));

            SupersetGroup group = new SupersetGroup(entry.getKey(), sortedExercises);
            wrappers.add(new ItemWrapper(minOrder, group));
        }

        wrappers.sort(Comparator.comparingInt(w -> w.order));

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

    // ---------- Getters for data ----------
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

    public Map<Integer, List<SetInput>> getAdvancedSetData() {
        Map<Integer, List<SetInput>> result = new HashMap<>();
        for (Map.Entry<Integer, List<View>> entry : advancedSetViews.entrySet()) {
            List<SetInput> inputs = new ArrayList<>();
            for (View row : entry.getValue()) {
                EditText etWeight = row.findViewById(R.id.etWeight);
                EditText etReps = row.findViewById(R.id.etReps);
                String wStr = etWeight.getText().toString().trim();
                String rStr = etReps.getText().toString().trim();
                if (!wStr.isEmpty() && !rStr.isEmpty()) {
                    try {
                        double w = Double.parseDouble(wStr);
                        int r = Integer.parseInt(rStr);
                        inputs.add(new SetInput(w, r));
                    } catch (NumberFormatException e) {
                        // skip
                    }
                }
            }
            if (!inputs.isEmpty()) {
                result.put(entry.getKey(), inputs);
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
        Button btnAddSet, btnFinishExercise;
        LinearLayout previousSetsContainer, ongoingSetsContainer;
        Exercise currentExercise;

        // Normal mode
        List<EditText> setEditTexts;

        // Advanced mode
        List<View> advancedRows;

        SingleExerciseViewHolder(View itemView) {
            super(itemView);
            tvExerciseName = itemView.findViewById(R.id.tvExerciseName);
            tvPreviousDateWeight = itemView.findViewById(R.id.tvPreviousDateWeight);
            etCurrentWeight = itemView.findViewById(R.id.etCurrentWeight);
            btnAddSet = itemView.findViewById(R.id.btnAddSet);
            btnFinishExercise = itemView.findViewById(R.id.btnFinishExercise);
            previousSetsContainer = itemView.findViewById(R.id.previousSetsContainer);
            ongoingSetsContainer = itemView.findViewById(R.id.ongoingSetsContainer);
        }

        void bind(Exercise exercise) {
            currentExercise = exercise;
            int[] targetReps = exercise.getTargetRepsArray();

            tvExerciseName.setText(exercise.getName());

            // Previous section
            List<SetLog> prevSets = previousRecords.get(exercise.getId());
            if (prevSets != null && !prevSets.isEmpty()) {
                double prevWeight = prevSets.get(0).getWeight();
                tvPreviousDateWeight.setText("Last: " + prevWeight + " kg");
                previousSetsContainer.removeAllViews();
                int maxPrev = Math.min(prevSets.size(), targetReps.length);
                for (int i = 0; i < maxPrev; i++) {
                    SetLog set = prevSets.get(i);
                    View row = LayoutInflater.from(itemView.getContext())
                            .inflate(R.layout.item_set_row_previous, previousSetsContainer, false);
                    TextView tvSetNumber = row.findViewById(R.id.tvSetNumber);
                    TextView tvTarget = row.findViewById(R.id.tvTargetReps);
                    TextView tvPrevReps = row.findViewById(R.id.tvPreviousReps);

                    tvSetNumber.setText("Set " + (i + 1));
                    tvTarget.setText(String.valueOf(targetReps[i]));
                    tvPrevReps.setText(String.valueOf(set.getReps()));
                    previousSetsContainer.addView(row);
                }
            } else {
                tvPreviousDateWeight.setText("No previous log");
                previousSetsContainer.removeAllViews();
            }

            // Current section: depends on mode
            if (isAdvanced) {
                setupAdvancedMode(exercise, targetReps);
            } else {
                setupNormalMode(exercise, targetReps);
            }

            btnFinishExercise.setOnClickListener(v -> {
                List<SetInput> setData = new ArrayList<>();
                if (isAdvanced) {
                    for (View row : advancedRows) {
                        EditText etW = row.findViewById(R.id.etWeight);
                        EditText etR = row.findViewById(R.id.etReps);
                        String wStr = etW.getText().toString().trim();
                        String rStr = etR.getText().toString().trim();
                        if (!wStr.isEmpty() && !rStr.isEmpty()) {
                            try {
                                double w = Double.parseDouble(wStr);
                                int r = Integer.parseInt(rStr);
                                setData.add(new SetInput(w, r));
                            } catch (NumberFormatException e) {
                                // skip
                            }
                        }
                    }
                } else {
                    double weight;
                    try {
                        weight = Double.parseDouble(etCurrentWeight.getText().toString());
                    } catch (NumberFormatException e) {
                        weight = 0;
                    }
                    for (EditText et : setEditTexts) {
                        String repText = et.getText().toString().trim();
                        if (!repText.isEmpty()) {
                            try {
                                int r = Integer.parseInt(repText);
                                setData.add(new SetInput(weight, r));
                            } catch (NumberFormatException ex) {
                                // ignore
                            }
                        }
                    }
                }
                if (finishListener != null && !setData.isEmpty()) {
                    finishListener.onExerciseFinished(currentExercise.getId(), setData);
                }
            });
        }

        private void setupNormalMode(Exercise exercise, int[] targetReps) {
            etCurrentWeight.setVisibility(View.VISIBLE);
            ((View) etCurrentWeight.getParent().getParent()).setVisibility(View.VISIBLE);

            setEditTexts = new ArrayList<>();
            setRepsEditTexts.put(exercise.getId(), setEditTexts);

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

            ongoingSetsContainer.removeAllViews();
            setEditTexts.clear();
            for (int i = 0; i < targetReps.length; i++) {
                addNormalSetRow(i + 1, targetReps[i], null);
            }

            btnAddSet.setOnClickListener(v -> {
                int nextSetNumber = setEditTexts.size() + 1;
                addNormalSetRow(nextSetNumber, 0, null);
            });
        }

        private void addNormalSetRow(int setNumber, int targetRep, Integer initialReps) {
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

        private void setupAdvancedMode(Exercise exercise, int[] targetReps) {
            etCurrentWeight.setVisibility(View.GONE);
            ((View) etCurrentWeight.getParent().getParent()).setVisibility(View.GONE);

            advancedRows = new ArrayList<>();
            advancedSetViews.put(exercise.getId(), advancedRows);

            ongoingSetsContainer.removeAllViews();
            for (int i = 0; i < targetReps.length; i++) {
                addAdvancedSetRow(i + 1, targetReps[i], null, null);
            }

            btnAddSet.setOnClickListener(v -> {
                int nextSetNumber = advancedRows.size() + 1;
                addAdvancedSetRow(nextSetNumber, 0, null, null);
            });
        }

        private void addAdvancedSetRow(int setNumber, int targetRep, Double initialWeight, Integer initialReps) {
            View row = LayoutInflater.from(itemView.getContext())
                    .inflate(R.layout.item_set_row_advanced, ongoingSetsContainer, false);
            TextView tvSetNumber = row.findViewById(R.id.tvSetNumber);
            EditText etWeight = row.findViewById(R.id.etWeight);
            TextView tvTargetHint = row.findViewById(R.id.tvTargetHint);
            EditText etReps = row.findViewById(R.id.etReps);

            tvSetNumber.setText("Set " + setNumber + ":");
            if (targetRep > 0) {
                tvTargetHint.setText("(" + targetRep + ")");
            } else {
                tvTargetHint.setText("");
            }
            if (initialWeight != null) {
                etWeight.setText(String.valueOf(initialWeight));
            }
            if (initialReps != null) {
                etReps.setText(String.valueOf(initialReps));
            }
            ongoingSetsContainer.addView(row);
            advancedRows.add(row);
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
            Button btnFinish = view.findViewById(R.id.btnFinishExercise);
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
                int maxPrev = Math.min(prevSets.size(), targetReps.length);
                for (int i = 0; i < maxPrev; i++) {
                    SetLog set = prevSets.get(i);
                    View row = LayoutInflater.from(itemView.getContext())
                            .inflate(R.layout.item_set_row_previous, prevContainer, false);
                    TextView tvSetNumber = row.findViewById(R.id.tvSetNumber);
                    TextView tvTarget = row.findViewById(R.id.tvTargetReps);
                    TextView tvPrevReps = row.findViewById(R.id.tvPreviousReps);

                    tvSetNumber.setText("Set " + (i + 1));
                    tvTarget.setText(String.valueOf(targetReps[i]));
                    tvPrevReps.setText(String.valueOf(set.getReps()));
                    prevContainer.addView(row);
                }
            } else {
                tvPrev.setText("No previous log");
                prevContainer.removeAllViews();
            }

            // Current section
            if (isAdvanced) {
                setupAdvancedMode(view, exercise, targetReps, ongoingContainer, btnAdd, btnFinish, etWeight);
            } else {
                setupNormalMode(view, exercise, targetReps, ongoingContainer, btnAdd, btnFinish, etWeight);
            }
        }

        private void setupNormalMode(View view, Exercise exercise, int[] targetReps,
                                     LinearLayout ongoingContainer, Button btnAdd, Button btnFinish,
                                     EditText etWeight) {
            etWeight.setVisibility(View.VISIBLE);
            ((View) etWeight.getParent().getParent()).setVisibility(View.VISIBLE);

            List<EditText> setEditTexts = new ArrayList<>();
            setRepsEditTexts.put(exercise.getId(), setEditTexts);

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

            ongoingContainer.removeAllViews();
            setEditTexts.clear();
            for (int i = 0; i < targetReps.length; i++) {
                addNormalSetRowToContainer(ongoingContainer, setEditTexts, i + 1, targetReps[i], null);
            }

            btnAdd.setOnClickListener(v -> {
                int nextSetNumber = setEditTexts.size() + 1;
                addNormalSetRowToContainer(ongoingContainer, setEditTexts, nextSetNumber, 0, null);
            });

            btnFinish.setOnClickListener(v -> {
                double weight;
                try {
                    weight = Double.parseDouble(etWeight.getText().toString());
                } catch (NumberFormatException e) {
                    weight = 0;
                }
                List<SetInput> setData = new ArrayList<>();
                for (EditText et : setEditTexts) {
                    String repText = et.getText().toString().trim();
                    if (!repText.isEmpty()) {
                        try {
                            int r = Integer.parseInt(repText);
                            setData.add(new SetInput(weight, r));
                        } catch (NumberFormatException ex) {
                            // ignore
                        }
                    }
                }
                if (finishListener != null && !setData.isEmpty()) {
                    finishListener.onExerciseFinished(exercise.getId(), setData);
                }
            });
        }

        private void addNormalSetRowToContainer(LinearLayout container, List<EditText> setEditTexts,
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

        private void setupAdvancedMode(View view, Exercise exercise, int[] targetReps,
                                       LinearLayout ongoingContainer, Button btnAdd, Button btnFinish,
                                       EditText etWeight) {
            etWeight.setVisibility(View.GONE);
            ((View) etWeight.getParent().getParent()).setVisibility(View.GONE);

            List<View> advancedRows = new ArrayList<>();
            advancedSetViews.put(exercise.getId(), advancedRows);

            ongoingContainer.removeAllViews();
            for (int i = 0; i < targetReps.length; i++) {
                addAdvancedSetRowToContainer(ongoingContainer, advancedRows, i + 1, targetReps[i], null, null);
            }

            btnAdd.setOnClickListener(v -> {
                int nextSetNumber = advancedRows.size() + 1;
                addAdvancedSetRowToContainer(ongoingContainer, advancedRows, nextSetNumber, 0, null, null);
            });

            btnFinish.setOnClickListener(v -> {
                List<SetInput> setData = new ArrayList<>();
                for (View row : advancedRows) {
                    EditText etW = row.findViewById(R.id.etWeight);
                    EditText etR = row.findViewById(R.id.etReps);
                    String wStr = etW.getText().toString().trim();
                    String rStr = etR.getText().toString().trim();
                    if (!wStr.isEmpty() && !rStr.isEmpty()) {
                        try {
                            double w = Double.parseDouble(wStr);
                            int r = Integer.parseInt(rStr);
                            setData.add(new SetInput(w, r));
                        } catch (NumberFormatException e) {
                            // ignore
                        }
                    }
                }
                if (finishListener != null && !setData.isEmpty()) {
                    finishListener.onExerciseFinished(exercise.getId(), setData);
                }
            });
        }

        private void addAdvancedSetRowToContainer(LinearLayout container, List<View> advancedRows,
                                                  int setNumber, int targetRep, Double initialWeight, Integer initialReps) {
            View row = LayoutInflater.from(container.getContext())
                    .inflate(R.layout.item_set_row_advanced, container, false);
            TextView tvSetNumber = row.findViewById(R.id.tvSetNumber);
            EditText etWeight = row.findViewById(R.id.etWeight);
            TextView tvTargetHint = row.findViewById(R.id.tvTargetHint);
            EditText etReps = row.findViewById(R.id.etReps);

            tvSetNumber.setText("Set " + setNumber + ":");
            if (targetRep > 0) {
                tvTargetHint.setText("(" + targetRep + ")");
            } else {
                tvTargetHint.setText("");
            }
            if (initialWeight != null) {
                etWeight.setText(String.valueOf(initialWeight));
            }
            if (initialReps != null) {
                etReps.setText(String.valueOf(initialReps));
            }
            container.addView(row);
            advancedRows.add(row);
        }
    }
}