package lk.dexter.double_progress_tracker.ui;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import lk.dexter.double_progress_tracker.R;
import lk.dexter.double_progress_tracker.data.entity.Exercise;
import lk.dexter.double_progress_tracker.data.entity.SetLog;
import lk.dexter.double_progress_tracker.data.entity.WorkoutLog;
import lk.dexter.double_progress_tracker.data.repository.WorkoutRepository;
import lk.dexter.double_progress_tracker.ui.adapters.WorkoutHistoryAdapter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WorkoutSummaryActivity extends AppCompatActivity {
    private int scheduleId;
    private String scheduleName;
    private WorkoutRepository repository;
    private RecyclerView recyclerView;
    private WorkoutHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_summary);

        scheduleId = getIntent().getIntExtra("schedule_id", -1);
        scheduleName = getIntent().getStringExtra("schedule_name");
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(scheduleName + " - Workout Summary");

        repository = new WorkoutRepository(this);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadSummary();
    }

    private void loadSummary() {
        new Thread(() -> {
            List<WorkoutLog> logs = repository.getWorkoutLogsForScheduleSync(scheduleId);
            List<WorkoutDay> workoutDays = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            for (WorkoutLog log : logs) {
                String dateStr = sdf.format(log.getDate());
                List<SetLog> setLogs = repository.getSetLogsForWorkoutSync(log.getId());

                // Group sets by exercise
                Map<Integer, List<SetLog>> setsByExercise = new HashMap<>();
                for (SetLog set : setLogs) {
                    setsByExercise.computeIfAbsent(set.getExerciseId(), k -> new ArrayList<>()).add(set);
                }

                List<ExerciseEntry> exerciseEntries = new ArrayList<>();
                for (Map.Entry<Integer, List<SetLog>> entry : setsByExercise.entrySet()) {
                    int exerciseId = entry.getKey();
                    List<SetLog> sets = entry.getValue();
                    Exercise exercise = repository.getExerciseByIdSync(exerciseId);
                    if (exercise != null) {
                        List<SetData> setDataList = new ArrayList<>();
                        for (SetLog set : sets) {
                            setDataList.add(new SetData(set.getWeight(), set.getReps()));
                        }
                        exerciseEntries.add(new ExerciseEntry(exercise.getName(), setDataList));
                    }
                }

                workoutDays.add(new WorkoutDay(dateStr, exerciseEntries));
            }

            // Sort by date descending (newest first)
            workoutDays.sort((d1, d2) -> d2.date.compareTo(d1.date));

            runOnUiThread(() -> {
                adapter = new WorkoutHistoryAdapter(workoutDays);
                recyclerView.setAdapter(adapter);
            });
        }).start();
    }

    // Data classes
    public static class SetData {
        public double weight;
        public int reps;
        public SetData(double weight, int reps) {
            this.weight = weight;
            this.reps = reps;
        }
    }

    public static class ExerciseEntry {
        public String exerciseName;
        public List<SetData> sets;
        public ExerciseEntry(String exerciseName, List<SetData> sets) {
            this.exerciseName = exerciseName;
            this.sets = sets;
        }
    }

    public static class WorkoutDay {
        public String date;
        public List<ExerciseEntry> exercises;
        public WorkoutDay(String date, List<ExerciseEntry> exercises) {
            this.date = date;
            this.exercises = exercises;
        }
    }
}