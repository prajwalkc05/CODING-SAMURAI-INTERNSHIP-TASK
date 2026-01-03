package com.example.weatherapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DailyAdapter extends RecyclerView.Adapter<DailyAdapter.ViewHolder> {

    private List<ForecastResponse.ForecastItem> dailyList;

    public DailyAdapter(List<ForecastResponse.ForecastItem> dailyList) {
        this.dailyList = dailyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_daily_forecast, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ForecastResponse.ForecastItem item = dailyList.get(position);
        
        // Use "EEE" for short day names (Mon, Tue, Wed)
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        holder.tvDay.setText(dayFormat.format(new Date(item.dt * 1000)));

        holder.tvMinTemp.setText(String.format(Locale.getDefault(), "%.0f°", item.main.tempMin));
        holder.tvMaxTemp.setText(String.format(Locale.getDefault(), "%.0f°", item.main.tempMax));
        
        holder.pbTempRange.setMax(40);
        holder.pbTempRange.setProgress((int) item.main.temp); 

        // Icon Mapping Logic
        int conditionId = item.weather.get(0).id;
        
        // Thunderstorm (2xx)
        if (conditionId >= 200 && conditionId < 300) {
            holder.ivIcon.setImageResource(R.drawable.ic_storm_icon);
        } 
        // Rain / Drizzle (3xx, 5xx)
        else if (conditionId >= 300 && conditionId < 600) {
            holder.ivIcon.setImageResource(R.drawable.ic_rain_icon);
        } 
        // Snow (6xx)
        else if (conditionId >= 600 && conditionId < 700) {
            holder.ivIcon.setImageResource(R.drawable.ic_snow);
        } 
        // Mist / Fog / Haze (7xx) -> Treated as NORMAL CLOUDY
        else if (conditionId >= 700 && conditionId < 800) {
            holder.ivIcon.setImageResource(R.drawable.ic_cloud_icon);
        } 
        // Clear / Sunny (800)
        else if (conditionId == 800) {
            holder.ivIcon.setImageResource(R.drawable.ic_clear_icon);
        } 
        // Clouds (80x) -> Treated as NORMAL CLOUDY
        else if (conditionId > 800) {
             holder.ivIcon.setImageResource(R.drawable.ic_cloud_icon);
        }
        // Default
        else {
             holder.ivIcon.setImageResource(R.drawable.ic_cloud_icon);
        }
    }

    @Override
    public int getItemCount() {
        return dailyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay, tvMinTemp, tvMaxTemp;
        ImageView ivIcon;
        ProgressBar pbTempRange;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            tvMinTemp = itemView.findViewById(R.id.tvMinTemp);
            tvMaxTemp = itemView.findViewById(R.id.tvMaxTemp);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            pbTempRange = itemView.findViewById(R.id.pbTempRange);
        }
    }
}
