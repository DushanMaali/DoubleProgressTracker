package lk.dexter.double_progress_tracker.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import lk.dexter.double_progress_tracker.R;
import lk.dexter.double_progress_tracker.data.entity.DraftSetLog;
import lk.dexter.double_progress_tracker.data.entity.Exercise;
import lk.dexter.double_progress_tracker.data.entity.SetLog;
import lk.dexter.double_progress_tracker.data.entity.WorkoutLog;
import lk.dexter.double_progress_tracker.data.repository.WorkoutRepository;
import lk.dexter.double_progress_tracker.ui.adapters.UnifiedLogAdapter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class LogWorkoutActivity extends AppCompatActivity {
    private int scheduleId;
    private WorkoutRepository repository;
    private List<Exercise> exercises;
    private UnifiedLogAdapter adapter;
    private RecyclerView recyclerView;
    private String mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_workout);

        scheduleId = getIntent().getIntExtra("schedule_id", -1);
        mode = getIntent().getStringExtra("mode");
        repository = new WorkoutRepository(this);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadExercises();

        Button btnSave = findViewById(R.id.btnSaveLog);
        btnSave.setOnClickListener(v -> saveWorkout());

        Button btnFinishWorkout = findViewById(R.id.btnFinishWorkout);
        btnFinishWorkout.setOnClickListener(v -> finishWorkout());
    }

    private void loadExercises() {
        new Thread(() -> {
            exercises = repository.getExercisesForScheduleSync(scheduleId);
            runOnUiThread(() -> {
                adapter = new UnifiedLogAdapter(exercises, repository, mode,
                        (exerciseId, setData) -> {
                            new Thread(() -> {
                                repository.clearDraftSetsForExercise(exerciseId);
                                List<DraftSetLog> drafts = new ArrayList<>();
                                for (int i = 0; i < setData.size(); i++) {
                                    UnifiedLogAdapter.SetInput input = setData.get(i);
                                    drafts.add(new DraftSetLog(scheduleId, exerciseId, i + 1,
                                            input.weight, input.reps, true));
                                }
                                if (!drafts.isEmpty()) {
                                    repository.insertDraftSets(drafts);
                                }
                                runOnUiThread(() ->
                                        Toast.makeText(LogWorkoutActivity.this,
                                                "Exercise saved to draft", Toast.LENGTH_SHORT).show());
                            }).start();
                        });
                recyclerView.setAdapter(adapter);
            });
        }).start();
    }

    private void saveWorkout() {
        if ("normal".equals(mode)) {
            saveNormalWorkout();
        } else {
            saveAdvancedWorkout();
        }
    }

    private void saveNormalWorkout() {
        Map<Integer, Double> weights = adapter.getWeights();
        Map<Integer, List<Integer>> repsMap = adapter.getRepsLists();

        if (weights == null || repsMap == null || weights.isEmpty()) {
            Toast.makeText(this, "Please enter at least one set", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            WorkoutLog log = new WorkoutLog(scheduleId, new Date(), "");
            long workoutLogId = repository.insertWorkoutLog(log);

            List<SetLog> allSets = new ArrayList<>();
            for (Exercise ex : exercises) {
                Double w = weights.get(ex.getId());
                List<Integer> repsList = repsMap.get(ex.getId());
                if (w != null && repsList != null && !repsList.isEmpty()) {
                    for (int i = 0; i < repsList.size(); i++) {
                        SetLog set = new SetLog((int) workoutLogId, ex.getId(), i + 1, w, repsList.get(i));
                        allSets.add(set);
                    }
                }
            }
            repository.insertSetLogs(allSets);

            checkDoubleProgression(weights, repsMap, adapter.getTargetRepsMap());

            repository.deleteDraftSetsForSchedule(scheduleId);

            runOnUiThread(() -> {
                Toast.makeText(this, "Workout saved!", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    private void saveAdvancedWorkout() {
        Map<Integer, List<UnifiedLogAdapter.SetInput>> setDataMap = adapter.getAdvancedSetData();
        if (setDataMap == null || setDataMap.isEmpty()) {
            Toast.makeText(this, "Please enter at least one set", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            WorkoutLog log = new WorkoutLog(scheduleId, new Date(), "");
            long workoutLogId = repository.insertWorkoutLog(log);

            List<SetLog> allSets = new ArrayList<>();
            for (Exercise ex : exercises) {
                List<UnifiedLogAdapter.SetInput> sets = setDataMap.get(ex.getId());
                if (sets != null && !sets.isEmpty()) {
                    for (int i = 0; i < sets.size(); i++) {
                        UnifiedLogAdapter.SetInput input = sets.get(i);
                        SetLog set = new SetLog((int) workoutLogId, ex.getId(), i + 1, input.weight, input.reps);
                        allSets.add(set);
                    }
                }
            }
            repository.insertSetLogs(allSets);

            repository.deleteDraftSetsForSchedule(scheduleId);

            runOnUiThread(() -> {
                Toast.makeText(this, "Workout saved!", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    private void finishWorkout() {
        new Thread(() -> {
            List<DraftSetLog> allDrafts = new ArrayList<>();
            for (Exercise ex : exercises) {
                allDrafts.addAll(repository.getDraftSetsForExerciseSync(ex.getId()));
            }
            if (allDrafts.isEmpty()) {
                runOnUiThread(() ->
                        Toast.makeText(this, "No finished exercises to save", Toast.LENGTH_SHORT).show());
                return;
            }
            WorkoutLog log = new WorkoutLog(scheduleId, new Date(), "");
            long workoutLogId = repository.insertWorkoutLog(log);
            List<SetLog> setLogs = new ArrayList<>();
            for (DraftSetLog draft : allDrafts) {
                SetLog set = new SetLog((int) workoutLogId, draft.getExerciseId(),
                        draft.getSetNumber(), draft.getWeight(), draft.getReps());
                setLogs.add(set);
            }
            repository.insertSetLogs(setLogs);
            repository.deleteDraftSetsForSchedule(scheduleId);
            runOnUiThread(() -> {
                Toast.makeText(this, "Workout finished and saved!", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    private void checkDoubleProgression(Map<Integer, Double> weights,
                                        Map<Integer, List<Integer>> repsMap,
                                        Map<Integer, int[]> targetRepsMap) {
        for (Exercise ex : exercises) {
            List<Integer> reps = repsMap.get(ex.getId());
            int[] targets = targetRepsMap.get(ex.getId());
            if (reps == null || targets == null || reps.size() < targets.length) {
                continue;
            }
            boolean allReachedTarget = true;
            for (int i = 0; i < targets.length; i++) {
                if (i >= reps.size() || reps.get(i) < targets[i]) {
                    allReachedTarget = false;
                    break;
                }
            }
            if (allReachedTarget) {
                double newWeight = weights.get(ex.getId()) + 2.5;
                ex.setSuggestedNextWeight(newWeight);
                repository.updateExercise(ex);
            } else {
                if (ex.getSuggestedNextWeight() != 0) {
                    ex.setSuggestedNextWeight(0);
                    repository.updateExercise(ex);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Thread(() -> {
            List<DraftSetLog> drafts = repository.getDraftSetsForScheduleSync(scheduleId);
            runOnUiThread(() -> {
                if (adapter != null) {
                    adapter.restoreDrafts(drafts);
                }
            });
        }).start();
    }
}