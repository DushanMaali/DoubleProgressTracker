package lk.dexter.double_progress_tracker.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import lk.dexter.double_progress_tracker.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnManage = findViewById(R.id.btnManageSchedules);
        Button btnNormal = findViewById(R.id.btnLogWorkoutNormal);
        Button btnAdvanced = findViewById(R.id.btnLogWorkoutAdvanced);

        btnManage.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ScheduleManagementActivity.class);
            startActivity(intent);
        });

        btnNormal.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LogWorkoutSelectionActivity.class);
            intent.putExtra("mode", "normal");
            startActivity(intent);
        });

        btnAdvanced.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LogWorkoutSelectionActivity.class);
            intent.putExtra("mode", "advanced");
            startActivity(intent);
        });
    }
}