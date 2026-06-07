package com.example.timska.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timska.R;
import com.example.timska.data.local.AttendanceEntity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** RecyclerView adapter for the teacher's live list of registered taps. */
public class AttendanceAdapter extends ListAdapter<AttendanceEntity, AttendanceAdapter.VH> {

    private static final SimpleDateFormat TIME_FMT =
            new SimpleDateFormat("HH:mm:ss", Locale.US);

    public AttendanceAdapter() {
        super(DIFF);
    }

    private static final DiffUtil.ItemCallback<AttendanceEntity> DIFF =
            new DiffUtil.ItemCallback<AttendanceEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull AttendanceEntity a, @NonNull AttendanceEntity b) {
                    return a.id == b.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull AttendanceEntity a, @NonNull AttendanceEntity b) {
                    return a.synced == b.synced
                            && a.studentName.equals(b.studentName)
                            && a.timestamp == b.timestamp;
                }
            };

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendance, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        AttendanceEntity item = getItem(position);
        holder.name.setText(item.studentName);
        String meta = item.studentId;
        if (item.course != null && !item.course.isEmpty()) {
            meta += " · " + item.course;
        }
        holder.meta.setText(meta);
        holder.time.setText(TIME_FMT.format(new Date(item.timestamp)));

        int dotColor = item.synced ? R.color.success : R.color.warning;
        holder.dot.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), dotColor));
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView meta;
        final TextView time;
        final View dot;

        VH(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvName);
            meta = itemView.findViewById(R.id.tvMeta);
            time = itemView.findViewById(R.id.tvTime);
            dot = itemView.findViewById(R.id.syncDot);
        }
    }
}
