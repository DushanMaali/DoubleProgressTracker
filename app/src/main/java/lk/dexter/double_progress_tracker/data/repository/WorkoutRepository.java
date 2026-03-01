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

    // Previous record for an exercise (all sets from last workout)
    public List<SetLog> getLatestSetsForExercise(int exerciseId) {
        return db.workoutLogDao().getLatestSetsForExercise(exerciseId);
    }

    // ========== Draft Sets ==========
    public void insertDraftSet(DraftSetLog draftSet) {
        executor.execute(() -> db.draftSetLogDao().insert(draftSet));
    }

    public void insertDraftSets(List<DraftSetLog> draftSets) {
        executor.execute(() -> db.draftSetLogDao().insertAll(draftSets));
    }

    public List<DraftSetLog> getDraftSetsForExerciseSync(int exerciseId) {
        return db.draftSetLogDao().getDraftSetsForExercise(exerciseId);
    }

    public void clearDraftSetsForExercise(int exerciseId) {
        executor.execute(() -> db.draftSetLogDao().deleteDraftSetsForExercise(exerciseId));
    }

    public void clearAllDrafts() {
        executor.execute(() -> db.draftSetLogDao().clearAllDrafts());
    }

    // Get drafts for a specific schedule
    public List<DraftSetLog> getDraftSetsForScheduleSync(int scheduleId) {
        return db.draftSetLogDao().getDraftSetsForSchedule(scheduleId);
    }

    // Delete drafts for a schedule
    public void deleteDraftSetsForSchedule(int scheduleId) {
        executor.execute(() -> db.draftSetLogDao().deleteDraftSetsForSchedule(scheduleId));
    }

    // Get workout logs for a schedule
    public List<WorkoutLog> getWorkoutLogsForScheduleSync(int scheduleId) {
        return db.workoutLogDao().getWorkoutLogsForSchedule(scheduleId);
    }

    // Get set logs for a specific workout (optional, for view details)
    public List<SetLog> getSetLogsForWorkoutSync(int workoutLogId) {
        return db.setLogDao().getSetLogsForWorkout(workoutLogId);
    }

    // Delete an exercise
    public void deleteExercise(Exercise exercise) {
        executor.execute(() -> db.exerciseDao().delete(exercise));
    }

    // Delete a workout log
    public void deleteWorkoutLog(WorkoutLog log) {
        executor.execute(() -> db.workoutLogDao().delete(log));
    }

    public Exercise getExerciseByIdSync(int exerciseId) {
        return db.exerciseDao().getExerciseById(exerciseId);
    }




}