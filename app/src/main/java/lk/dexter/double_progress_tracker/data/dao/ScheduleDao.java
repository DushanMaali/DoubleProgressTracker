package lk.dexter.double_progress_tracker.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import lk.dexter.double_progress_tracker.data.entity.Schedule;
import java.util.List;

@Dao
public interface ScheduleDao {
    @Insert
    long insert(Schedule schedule);

    @Update
    void update(Schedule schedule);

    @Delete
    void delete(Schedule schedule);

    @Query("SELECT * FROM schedules ORDER BY name")
    List<Schedule> getAllSchedules();
}