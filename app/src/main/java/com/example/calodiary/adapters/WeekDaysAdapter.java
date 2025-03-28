package com.example.calodiary.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.calodiary.R;
import com.example.calodiary.models.DayData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WeekDaysAdapter extends RecyclerView.Adapter<WeekDaysAdapter.DayViewHolder> {
    private List<DayData> days;
    private final OnDayClickListener listener;
    private int selectedPosition = 0;

    public interface OnDayClickListener {
        void onDayClick(DayData day);
    }

    public WeekDaysAdapter(List<DayData> days, OnDayClickListener listener) {
        this.days = days != null ? days : new ArrayList<>();
        this.listener = listener;
    }

    public void updateDays(List<DayData> newDays) {
        this.days = newDays;
        selectedPosition = 0;
        if (!days.isEmpty()) {
            days.get(0).setSelected(true);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        DayData day = days.get(position);
        holder.bind(day, position);
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    class DayViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDayName;
        private final TextView tvDayNumber;
        private final TextView tvCalories;
        private final CardView cardView;

        DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayName = itemView.findViewById(R.id.tvDayName);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
            tvCalories = itemView.findViewById(R.id.tvCalories);
            cardView = itemView.findViewById(R.id.cardView);
        }

        void bind(final DayData day, final int position) {
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd", Locale.getDefault());

            tvDayName.setText(dayFormat.format(day.getDateTime()));
            tvDayNumber.setText(dateFormat.format(day.getDateTime()));
            tvCalories.setText(String.format(Locale.getDefault(), "%d/%d cal", day.getCalories(), (int)day.getCalorieGoal()));

            // Set color based on calorie goal
            int bgColor;
            int textColor;
            if (day.getCalories() == 0) {
                bgColor = itemView.getContext().getColor(android.R.color.white);
                textColor = itemView.getContext().getColor(android.R.color.black);
            } else if (day.isCalorieGoalMet()) {
                bgColor = itemView.getContext().getColor(R.color.goal_met_bg);
                textColor = itemView.getContext().getColor(R.color.goal_met_text);
            } else {
                bgColor = itemView.getContext().getColor(R.color.goal_not_met_bg);
                textColor = itemView.getContext().getColor(R.color.goal_not_met_text);
            }

            if (day.isSelected()) {
                bgColor = itemView.getContext().getColor(R.color.selected_day_bg);
            }

            cardView.setCardBackgroundColor(bgColor);
            tvDayName.setTextColor(textColor);
            tvDayNumber.setTextColor(textColor);
            tvCalories.setTextColor(textColor);

            itemView.setOnClickListener(v -> {
                if (selectedPosition != position) {
                    int oldPosition = selectedPosition;
                    selectedPosition = position;
                    days.get(oldPosition).setSelected(false);
                    day.setSelected(true);
                    notifyItemChanged(oldPosition);
                    notifyItemChanged(position);
                    listener.onDayClick(day);
                }
            });
        }
    }
} 