package lk.dexter.double_progress_tracker.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "exercises",
        foreignKeys = @ForeignKey(entity = Schedule.class,
                parentColumns = "id",
                childColumns = "scheduleId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("scheduleId")})
public class Exercise {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int scheduleId;
    private String name;
    private String targetRepsList;          // comma-separated, e.g. "25,20,15,15,12,10,10"
    private double startingWeight;
    private int orderIndex;
    private double suggestedNextWeight;

    // Constructor
    public Exercise(int scheduleId, String name, String targetRepsList,
                    double startingWeight, int orderIndex) {
        this.scheduleId = scheduleId;
        this.name = name;
        this.targetRepsList = targetRepsList;
        this.startingWeight = startingWeight;
        this.orderIndex = orderIndex;
        this.suggestedNextWeight = 0;
    }

    // Helper to get target reps as integer array
    public int[] getTargetRepsArray() {
        if (targetRepsList == null || targetRepsList.isEmpty()) return new int[0];
        String[] parts = targetRepsList.split(",");
        int[] reps = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                reps[i] = Integer.parseInt(parts[i].trim());
            } catch (NumberFormatException e) {
                reps[i] = 0;
            }
        }
        return reps;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getScheduleId() { return scheduleId; }
    public void setScheduleId(int scheduleId) { this.scheduleId = scheduleId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTargetRepsList() { return targetRepsList; }
    public void setTargetRepsList(String targetRepsList) { this.targetRepsList = targetRepsList; }
    public double getStartingWeight() { return startingWeight; }
    public void setStartingWeight(double startingWeight) { this.startingWeight = startingWeight; }
    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
    public double getSuggestedNextWeight() { return suggestedNextWeight; }
    public void setSuggestedNextWeight(double suggestedNextWeight) { this.suggestedNextWeight = suggestedNextWeight; }
}