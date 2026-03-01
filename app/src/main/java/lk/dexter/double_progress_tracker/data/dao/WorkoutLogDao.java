package lk.dexter.double_progress_tracker.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
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

    // Get all sets from the most recent workout for a given exercise
    @Query("SELECT sl.* FROM set_logs sl " +
            "WHERE sl.workoutLogId = (SELECT wl.id FROM workout_logs wl " +
            "                        INNER JOIN set_logs sl2 ON wl.id = sl2.workoutLogId " +
            "                        WHERE sl2.exerciseId = :exerciseId " +
            "                        ORDER BY wl.date DESC LIMIT 1)")
    List<SetLog> getLatestSetsForExercise(int exerciseId);

    @Delete
    void delete(WorkoutLog workoutLog);
}