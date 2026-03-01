package lk.dexter.double_progress_tracker.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import lk.dexter.double_progress_tracker.R;
import lk.dexter.double_progress_tracker.data.entity.WorkoutLog;
import lk.dexter.double_progress_tracker.data.repository.WorkoutRepository;
import lk.dexter.double_progress_tracker.ui.adapters.WorkoutLogAdapter;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class WorkoutHistoryActivity extends AppCompatActivity {
    private int scheduleId;
    private WorkoutRepository repository;
    private List<WorkoutLog> workoutLogs;
    private RecyclerView recyclerView;
    private WorkoutLogAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_history);

        scheduleId = getIntent().getIntExtra("schedule_id", -1);
        repository = new WorkoutRepository(this);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadWorkoutLogs();
    }

    private void loadWorkoutLogs() {
        new Thread(() -> {
            workoutLogs = repository.getWorkoutLogsForScheduleSync(scheduleId);
            runOnUiThread(() -> {
                adapter = new WorkoutLogAdapter(workoutLogs, new WorkoutLogAdapter.OnWorkoutLogActionListener() {
                    @Override
                    public void onView(WorkoutLog log) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                        new MaterialAlertDialogBuilder(WorkoutHistoryActivity.this)
                                .setTitle("Workout Details")
                                .setMessage("Date: " + sdf.format(log.getDate()) +
                                        "\nNotes: " + log.getNotes())
                                .setPositiveButton("OK", null)
                                .show();
                    }

                    @Override
                    public void onDelete(WorkoutLog log) {
                        confirmDelete(log);
                    }
                });
                recyclerView.setAdapter(adapter);
            });
        }).start();
    }

    private void confirmDelete(WorkoutLog log) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String dateStr = sdf.format(log.getDate());
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Workout")
                .setMessage("Delete workout from " + dateStr + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    repository.deleteWorkoutLog(log);
                    loadWorkoutLogs(); // refresh
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}