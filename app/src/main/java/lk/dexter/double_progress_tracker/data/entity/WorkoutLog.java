package lk.dexter.double_progress_tracker.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "workout_logs")
public class WorkoutLog {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int scheduleId;
    private Date date;
    private String notes;

    public WorkoutLog(int scheduleId, Date date, String notes) {
        this.scheduleId = scheduleId;
        this.date = date;
        this.notes = notes;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getScheduleId() { return scheduleId; }
    public void setScheduleId(int scheduleId) { this.scheduleId = scheduleId; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}