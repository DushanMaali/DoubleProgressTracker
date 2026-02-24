package lk.dexter.double_progress_tracker.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import lk.dexter.double_progress_tracker.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.activity_main);

        Button btnManageSchedules = findViewById(R.id.btnManageSchedules);
        Button btnLogWorkout = findViewById(R.id.btnLogWorkout);

        btnManageSchedules.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ScheduleManagementActivity.class);
            startActivity(intent);
        });

        btnLogWorkout.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LogWorkoutSelectionActivity.class);
            startActivity(intent);
        });
    }
}