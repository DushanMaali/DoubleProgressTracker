package lk.dexter.double_progress_tracker.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import lk.dexter.double_progress_tracker.R;
import lk.dexter.double_progress_tracker.data.entity.Exercise;
import lk.dexter.double_progress_tracker.data.repository.WorkoutRepository;
import lk.dexter.double_progress_tracker.ui.adapters.ExerciseAdapter;
import java.util.List;

public class ScheduleDetailActivity extends AppCompatActivity {
    private int scheduleId;
    private String scheduleName;
    private WorkoutRepository repository;
    private List<Exercise> exercises;
    private RecyclerView recyclerView;
    private ExerciseAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_detail);

        scheduleId = getIntent().getIntExtra("schedule_id", -1);
        scheduleName = getIntent().getStringExtra("schedule_name");
        TextView tvName = findViewById(R.id.tvScheduleName);
        tvName.setText(scheduleName + " - Exercises");

        repository = new WorkoutRepository(this);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadExercises();

        findViewById(R.id.btnAddExercise).setOnClickListener(v -> showAddExerciseDialog());
    }

    private void loadExercises() {
        new Thread(() -> {
            exercises = repository.getExercisesForScheduleSync(scheduleId);
            runOnUiThread(() -> {
                adapter = new ExerciseAdapter(exercises, exercise -> {
                    // Optional: edit exercise
                });
                recyclerView.setAdapter(adapter);
            });
        }).start();
    }

    private void showAddExerciseDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_exercise, null);
        TextInputEditText etName = view.findViewById(R.id.etName);
        TextInputEditText etTargetReps = view.findViewById(R.id.etTargetReps);
        TextInputEditText etWeight = view.findViewById(R.id.etWeight);
        CheckBox cbIsSuperset = view.findViewById(R.id.cbIsSuperset);
        TextInputLayout tilSupersetId = view.findViewById(R.id.tilSupersetId);
        TextInputLayout tilSupersetOrder = view.findViewById(R.id.tilSupersetOrder);
        TextInputEditText etSupersetId = view.findViewById(R.id.etSupersetId);
        TextInputEditText etSupersetOrder = view.findViewById(R.id.etSupersetOrder);

        cbIsSuperset.setOnCheckedChangeListener((buttonView, isChecked) -> {
            tilSupersetId.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            tilSupersetOrder.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        builder.setView(view)
                .setTitle("Add Exercise")
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etName.getText() != null ? etName.getText().toString() : "";
                    String targetReps = etTargetReps.getText() != null ? etTargetReps.getText().toString() : "";
                    double weight = 0;
                    if (etWeight.getText() != null) {
                        String wStr = etWeight.getText().toString();
                        if (!wStr.isEmpty()) {
                            try {
                                weight = Double.parseDouble(wStr);
                            } catch (NumberFormatException e) {
                                weight = 0;
                            }
                        }
                    }
                    Integer supersetId = null;
                    int supersetOrder = 0;
                    if (cbIsSuperset.isChecked()) {
                        if (etSupersetId.getText() != null) {
                            try {
                                supersetId = Integer.parseInt(etSupersetId.getText().toString());
                            } catch (NumberFormatException e) {
                                supersetId = null;
                            }
                        }
                        if (etSupersetOrder.getText() != null) {
                            try {
                                supersetOrder = Integer.parseInt(etSupersetOrder.getText().toString());
                            } catch (NumberFormatException e) {
                                supersetOrder = 1;
                            }
                        }
                    }
                    int order = exercises.size();
                    Exercise exercise = new Exercise(scheduleId, name, targetReps, weight, order,
                            supersetId, supersetOrder);
                    repository.insertExercise(exercise);
                    loadExercises();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}