package lk.dexter.double_progress_tracker.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import lk.dexter.double_progress_tracker.data.entity.Exercise;
import java.util.List;

@Dao
public interface ExerciseDao {
    @Insert
    long insert(Exercise exercise);

    @Update
    void update(Exercise exercise);

    @Delete
    void delete(Exercise exercise);

    @Query("SELECT * FROM exercises WHERE scheduleId = :scheduleId ORDER BY orderIndex")
    List<Exercise> getExercisesForSchedule(int scheduleId);
}