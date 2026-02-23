package lk.dexter.double_progress_tracker.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import lk.dexter.double_progress_tracker.R;
import lk.dexter.double_progress_tracker.data.entity.Exercise;
import lk.dexter.double_progress_tracker.data.entity.SetLog;
import lk.dexter.double_progress_tracker.data.entity.WorkoutLog;
import lk.dexter.double_progress_tracker.data.repository.WorkoutRepository;
import lk.dexter.double_progress_tracker.ui.adapters.LogWorkoutAdapter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class LogWorkoutActivity extends AppCompatActivity {
    private int scheduleId;
    private WorkoutRepository repository;
    private List<Exercise> exercises;
    private LogWorkoutAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_workout);

        scheduleId = getIntent().getIntExtra("schedule_id", -1);
        repository = new WorkoutRepository(this);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadExercises();

        Button btnSave = findViewById(R.id.btnSaveLog);
        btnSave.setOnClickListener(v -> saveWorkout());
    }

    private void loadExercises() {
        new Thread(() -> {
            exercises = repository.getExercisesForScheduleSync(scheduleId);
            runOnUiThread(() -> {
                adapter = new LogWorkoutAdapter(exercises, repository);
                recyclerView.setAdapter(adapter);
            });
        }).start();
    }

    private void saveWorkout() {
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

            // Double progression check with per-set targets
            checkDoubleProgression(exercises, weights, repsMap, adapter.getTargetRepsMap());

            runOnUiThread(() -> {
                Toast.makeText(this, "Workout saved!", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    private void checkDoubleProgression(List<Exercise> exercises,
                                        Map<Integer, Double> weights,
                                        Map<Integer, List<Integer>> repsMap,
                                        Map<Integer, int[]> targetRepsMap) {
        for (Exercise ex : exercises) {
            List<Integer> reps = repsMap.get(ex.getId());
            int[] targets = targetRepsMap.get(ex.getId());
            if (reps == null || targets == null || reps.size() < targets.length) {
                // Not all sets completed? We require at least the target number of sets
                // If fewer sets logged, don't suggest increase.
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
}