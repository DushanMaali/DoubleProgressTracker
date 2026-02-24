package lk.dexter.double_progress_tracker.ui.model;

import lk.dexter.double_progress_tracker.data.entity.Exercise;
import java.util.List;

public class SupersetGroup {
    private int supersetId;
    private List<Exercise> exercises;

    public SupersetGroup(int supersetId, List<Exercise> exercises) {
        this.supersetId = supersetId;
        this.exercises = exercises;
    }

    public int getSupersetId() { return supersetId; }
    public List<Exercise> getExercises() { return exercises; }
}