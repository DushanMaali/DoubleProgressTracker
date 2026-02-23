package lk.dexter.double_progress_tracker.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import lk.dexter.double_progress_tracker.data.entity.SetLog;
import java.util.List;

@Dao
public interface SetLogDao {
    @Insert
    void insert(SetLog setLog);

    @Insert
    void insertAll(List<SetLog> setLogs);

    @Query("SELECT * FROM set_logs WHERE workoutLogId = :workoutLogId ORDER BY exerciseId, setNumber")
    List<SetLog> getSetLogsForWorkout(int workoutLogId);
}