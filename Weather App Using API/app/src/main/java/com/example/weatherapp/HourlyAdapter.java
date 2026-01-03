package com.example.weatherapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HourlyAdapter extends RecyclerView.Adapter<HourlyAdapter.ViewHolder> {

    private List<ForecastResponse.ForecastItem> hourlyList;

    public HourlyAdapter(List<ForecastResponse.ForecastItem> hourlyList) {
        this.hourlyList = hourlyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hourly_forecast, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ForecastResponse.ForecastItem item = hourlyList.get(position);
        
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        holder.tvTime.setText(timeFormat.format(new Date(item.dt * 1000)));
        holder.tvTemp.setText(String.format(Locale.getDefault(), "%.0f°", item.main.temp));
        
        // Icon Mapping Logic
        int conditionId = item.weather.get(0).id;
        if (conditionId >= 200 && conditionId < 300) {
            holder.ivIcon.setImageResource(R.drawable.ic_storm);
        } else if (conditionId >= 300 && conditionId < 600) {
            holder.ivIcon.setImageResource(R.drawable.ic_rain);
        } else if (conditionId >= 600 && conditionId < 700) {
            holder.ivIcon.setImageResource(R.drawable.ic_snow);
        } else if (conditionId >= 700 && conditionId < 800) {
            holder.ivIcon.setImageResource(R.drawable.ic_mist);
        } else if (conditionId == 800) {
            holder.ivIcon.setImageResource(R.drawable.ic_sun);
        } else if (conditionId > 800) {
            holder.ivIcon.setImageResource(R.drawable.ic_cloud);
        }
    }

    @Override
    public int getItemCount() {
        return hourlyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvTemp;
        ImageView ivIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvTemp = itemView.findViewById(R.id.tvTemp);
            ivIcon = itemView.findViewById(R.id.ivIcon);
        }
    }
}
