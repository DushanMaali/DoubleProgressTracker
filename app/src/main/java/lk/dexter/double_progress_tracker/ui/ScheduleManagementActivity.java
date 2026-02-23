package lk.dexter.double_progress_tracker.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import lk.dexter.double_progress_tracker.R;
import lk.dexter.double_progress_tracker.data.entity.Schedule;
import lk.dexter.double_progress_tracker.data.repository.WorkoutRepository;
import lk.dexter.double_progress_tracker.ui.adapters.ScheduleAdapter;
import java.util.List;

public class ScheduleManagementActivity extends AppCompatActivity {
    private WorkoutRepository repository;
    private List<Schedule> schedules;
    private RecyclerView recyclerView;
    private ScheduleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_management);

        repository = new WorkoutRepository(this);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Button btnAddSchedule = findViewById(R.id.btnAddSchedule);
        btnAddSchedule.setOnClickListener(v -> showAddScheduleDialog());

        loadSchedules();
    }

    private void loadSchedules() {
        new Thread(() -> {
            schedules = repository.getAllSchedulesSync();
            runOnUiThread(() -> {
                adapter = new ScheduleAdapter(schedules, schedule -> {
                    Intent intent = new Intent(ScheduleManagementActivity.this, ScheduleDetailActivity.class);
                    intent.putExtra("schedule_id", schedule.getId());
                    intent.putExtra("schedule_name", schedule.getName());
                    startActivity(intent);
                });
                recyclerView.setAdapter(adapter);
            });
        }).start();
    }

    private void showAddScheduleDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_schedule, null);
        TextInputEditText etName = view.findViewById(R.id.etName);
        TextInputEditText etDesc = view.findViewById(R.id.etDesc);

        builder.setView(view)
                .setTitle("Add Schedule")
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etName.getText() != null ? etName.getText().toString() : "";
                    String desc = etDesc.getText() != null ? etDesc.getText().toString() : "";
                    if (!name.isEmpty()) {
                        Schedule schedule = new Schedule(name, desc);
                        repository.insertSchedule(schedule);
                        loadSchedules();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}