package lk.dexter.double_progress_tracker.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import lk.dexter.double_progress_tracker.data.entity.Schedule;
import lk.dexter.double_progress_tracker.data.repository.WorkoutRepository;
import java.util.List;

public class ScheduleViewModel extends AndroidViewModel {
    private WorkoutRepository repository;
    private MutableLiveData<List<Schedule>> schedules = new MutableLiveData<>();

    public ScheduleViewModel(Application application) {
        super(application);
        repository = new WorkoutRepository(application);
        loadSchedules();
    }

    private void loadSchedules() {
        new Thread(() -> {
            List<Schedule> list = repository.getAllSchedulesSync();
            schedules.postValue(list);
        }).start();
    }

    public LiveData<List<Schedule>> getSchedules() {
        return schedules;
    }

    public void addSchedule(Schedule schedule) {
        repository.insertSchedule(schedule);
        loadSchedules();
    }
}