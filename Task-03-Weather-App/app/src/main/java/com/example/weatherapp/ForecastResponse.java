package com.example.weatherapp;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ForecastResponse {
    @SerializedName("list")
    public List<ForecastItem> list;

    @SerializedName("city")
    public City city;

    public class ForecastItem {
        @SerializedName("dt")
        public long dt;

        @SerializedName("main")
        public WeatherResponse.Main main;

        @SerializedName("weather")
        public List<WeatherResponse.Weather> weather;

        @SerializedName("dt_txt")
        public String dtTxt;
    }

    public class City {
        @SerializedName("name")
        public String name;
        
        @SerializedName("sunrise")
        public long sunrise;
        
        @SerializedName("sunset")
        public long sunset;
    }
}
