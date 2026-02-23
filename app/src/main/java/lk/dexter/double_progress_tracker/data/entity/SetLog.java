package lk.dexter.double_progress_tracker.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "set_logs",
        foreignKeys = @ForeignKey(entity = WorkoutLog.class,
                parentColumns = "id",
                childColumns = "workoutLogId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("workoutLogId")})
public class SetLog {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int workoutLogId;
    private int exerciseId;
    private int setNumber;
    private double weight;
    private int reps;

    public SetLog(int workoutLogId, int exerciseId, int setNumber, double weight, int reps) {
        this.workoutLogId = workoutLogId;
        this.exerciseId = exerciseId;
        this.setNumber = setNumber;
        this.weight = weight;
        this.reps = reps;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getWorkoutLogId() { return workoutLogId; }
    public void setWorkoutLogId(int workoutLogId) { this.workoutLogId = workoutLogId; }
    public int getExerciseId() { return exerciseId; }
    public void setExerciseId(int exerciseId) { this.exerciseId = exerciseId; }
    public int getSetNumber() { return setNumber; }
    public void setSetNumber(int setNumber) { this.setNumber = setNumber; }
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
    public int getReps() { return reps; }
    public void setReps(int reps) { this.reps = reps; }
}