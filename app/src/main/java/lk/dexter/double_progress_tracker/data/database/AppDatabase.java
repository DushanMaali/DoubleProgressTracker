package lk.dexter.double_progress_tracker.data.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import lk.dexter.double_progress_tracker.data.Converters;
import lk.dexter.double_progress_tracker.data.dao.*;
import lk.dexter.double_progress_tracker.data.entity.*;

@Database(entities = {Schedule.class, Exercise.class, WorkoutLog.class, SetLog.class},
        version = 4,
        exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract ScheduleDao scheduleDao();
    public abstract ExerciseDao exerciseDao();
    public abstract WorkoutLogDao workoutLogDao();
    public abstract SetLogDao setLogDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "workout_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}