package lk.dexter.double_progress_tracker.data.repository;

import android.content.Context;
import lk.dexter.double_progress_tracker.data.database.AppDatabase;
import lk.dexter.double_progress_tracker.data.entity.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkoutRepository {
    private AppDatabase db;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public WorkoutRepository(Context context) {
        db = AppDatabase.getInstance(context);
    }

    // ========== Schedules ==========
    public void insertSchedule(Schedule schedule) {
        executor.execute(() -> db.scheduleDao().insert(schedule));
    }

    public List<Schedule> getAllSchedulesSync() {
        return db.scheduleDao().getAllSchedules();
    }

    // ========== Exercises ==========
    public void insertExercise(Exercise exercise) {
        executor.execute(() -> db.exerciseDao().insert(exercise));
    }

    public List<Exercise> getExercisesForScheduleSync(int scheduleId) {
        return db.exerciseDao().getExercisesForSchedule(scheduleId);
    }

    public void updateExercise(Exercise exercise) {
        executor.execute(() -> db.exerciseDao().update(exercise));
    }

    // ========== Workout Logs ==========
    public long insertWorkoutLog(WorkoutLog log) {
        return db.workoutLogDao().insert(log);
    }

    public void insertSetLogs(List<SetLog> setLogs) {
        executor.execute(() -> db.setLogDao().insertAll(setLogs));
    }

    // Previous record for an exercise
    public List<SetLog> getLatestSetLogsForExercise(int exerciseId) {
        return db.workoutLogDao().getLatestSetLogsForExercise(exerciseId);
    }
}