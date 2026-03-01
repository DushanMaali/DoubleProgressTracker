package lk.dexter.double_progress_tracker.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import lk.dexter.double_progress_tracker.R;
import lk.dexter.double_progress_tracker.data.entity.Schedule;
import lk.dexter.double_progress_tracker.data.repository.WorkoutRepository;
import lk.dexter.double_progress_tracker.ui.adapters.ScheduleAdapter;
import java.util.List;

public class LogWorkoutSelectionActivity extends AppCompatActivity {
    private WorkoutRepository repository;
    private List<Schedule> schedules;
    private RecyclerView recyclerView;
    private String mode; // "normal" or "advanced"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_workout_selection);

        mode = getIntent().getStringExtra("mode");
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText("Select Schedule to Log (" + mode + ")");

        repository = new WorkoutRepository(this);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadSchedules();
    }

    private void loadSchedules() {
        new Thread(() -> {
            schedules = repository.getAllSchedulesSync();
            runOnUiThread(() -> {
                ScheduleAdapter adapter = new ScheduleAdapter(schedules, schedule -> {
                    Intent intent = new Intent(LogWorkoutSelectionActivity.this, LogWorkoutActivity.class);
                    intent.putExtra("schedule_id", schedule.getId());
                    intent.putExtra("schedule_name", schedule.getName());
                    intent.putExtra("mode", mode);
                    startActivity(intent);
                });
                recyclerView.setAdapter(adapter);
            });
        }).start();
    }
}