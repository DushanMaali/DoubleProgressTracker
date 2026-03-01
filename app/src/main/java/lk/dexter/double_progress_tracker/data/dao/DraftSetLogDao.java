package lk.dexter.double_progress_tracker.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import lk.dexter.double_progress_tracker.data.entity.DraftSetLog;
import java.util.List;

@Dao
public interface DraftSetLogDao {
    @Insert
    void insert(DraftSetLog draftSetLog);

    @Insert
    void insertAll(List<DraftSetLog> draftSetLogs);

    @Query("SELECT * FROM draft_set_logs WHERE exerciseId = :exerciseId")
    List<DraftSetLog> getDraftSetsForExercise(int exerciseId);

    @Query("SELECT * FROM draft_set_logs WHERE scheduleId = :scheduleId")
    List<DraftSetLog> getDraftSetsForSchedule(int scheduleId);

    @Query("DELETE FROM draft_set_logs WHERE exerciseId = :exerciseId")
    void deleteDraftSetsForExercise(int exerciseId);

    @Query("DELETE FROM draft_set_logs WHERE scheduleId = :scheduleId")
    void deleteDraftSetsForSchedule(int scheduleId);

    @Query("DELETE FROM draft_set_logs")
    void clearAllDrafts();
}