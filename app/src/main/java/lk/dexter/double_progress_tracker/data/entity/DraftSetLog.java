package lk.dexter.double_progress_tracker.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "draft_set_logs")
public class DraftSetLog {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int scheduleId;          // New field
    private int exerciseId;
    private int setNumber;
    private double weight;
    private int reps;
    private boolean isCompleted;     // true if exercise finished

    public DraftSetLog(int scheduleId, int exerciseId, int setNumber, double weight, int reps, boolean isCompleted) {
        this.scheduleId = scheduleId;
        this.exerciseId = exerciseId;
        this.setNumber = setNumber;
        this.weight = weight;
        this.reps = reps;
        this.isCompleted = isCompleted;
    }

    // Getters and setters (include scheduleId)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getScheduleId() { return scheduleId; }
    public void setScheduleId(int scheduleId) { this.scheduleId = scheduleId; }
    public int getExerciseId() { return exerciseId; }
    public void setExerciseId(int exerciseId) { this.exerciseId = exerciseId; }
    public int getSetNumber() { return setNumber; }
    public void setSetNumber(int setNumber) { this.setNumber = setNumber; }
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
    public int getReps() { return reps; }
    public void setReps(int reps) { this.reps = reps; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}