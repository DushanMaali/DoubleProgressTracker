package lk.dexter.double_progress_tracker.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import lk.dexter.double_progress_tracker.data.entity.SetLog;
import lk.dexter.double_progress_tracker.data.entity.WorkoutLog;
import java.util.List;

@Dao
public interface WorkoutLogDao {
    @Insert
    long insert(WorkoutLog workoutLog);

    @Query("SELECT * FROM workout_logs WHERE scheduleId = :scheduleId ORDER BY date DESC")
    List<WorkoutLog> getWorkoutLogsForSchedule(int scheduleId);

    // Get the latest set logs for an exercise (previous record)
    @Query("SELECT sl.* FROM set_logs sl " +
            "INNER JOIN workout_logs wl ON sl.workoutLogId = wl.id " +
            "WHERE sl.exerciseId = :exerciseId " +
            "ORDER BY wl.date DESC LIMIT 3")
    List<SetLog> getLatestSetLogsForExercise(int exerciseId);
}