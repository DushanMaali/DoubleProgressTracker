package lk.dexter.double_progress_tracker.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import lk.dexter.double_progress_tracker.R;
import lk.dexter.double_progress_tracker.data.entity.WorkoutLog;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class WorkoutLogAdapter extends RecyclerView.Adapter<WorkoutLogAdapter.ViewHolder> {
    private List<WorkoutLog> logs;
    private OnWorkoutLogActionListener listener;

    public interface OnWorkoutLogActionListener {
        void onView(WorkoutLog log);
        void onDelete(WorkoutLog log);
    }

    public WorkoutLogAdapter(List<WorkoutLog> logs, OnWorkoutLogActionListener listener) {
        this.logs = logs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkoutLog log = logs.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        holder.tvDate.setText(sdf.format(log.getDate()));
        holder.tvNotes.setText(log.getNotes());

        holder.ivMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.inflate(R.menu.workout_log_menu);
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_view) {
                    listener.onView(log);
                    return true;
                } else if (item.getItemId() == R.id.action_delete) {
                    listener.onDelete(log);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() { return logs.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvNotes;
        ImageView ivMenu;
        ViewHolder(View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvNotes = itemView.findViewById(R.id.tvNotes);
            ivMenu = itemView.findViewById(R.id.ivMenu);
        }
    }
}