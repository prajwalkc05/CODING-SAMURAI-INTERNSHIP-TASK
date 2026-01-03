package com.example.weatherapp;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WeatherResponse {

    @SerializedName("main")
    public Main main;

    @SerializedName("weather")
    public List<Weather> weather;

    @SerializedName("wind")
    public Wind wind;
    
    @SerializedName("sys")
    public Sys sys;

    @SerializedName("name")
    public String name;

    @SerializedName("cod")
    public int cod;

    public class Main {
        @SerializedName("temp")
        public float temp;

        @SerializedName("feels_like")
        public float feelsLike;

        @SerializedName("humidity")
        public int humidity;

        @SerializedName("temp_min")
        public float tempMin;

        @SerializedName("temp_max")
        public float tempMax;
    }

    public class Weather {
        @SerializedName("id")
        public int id;
        
        @SerializedName("main")
        public String main;
        
        @SerializedName("description")
        public String description;
        
        @SerializedName("icon")
        public String icon;
    }

    public class Wind {
        @SerializedName("speed")
        public float speed;
    }
    
    public class Sys {
        @SerializedName("sunrise")
        public long sunrise;
        
        @SerializedName("sunset")
        public long sunset;
    }
}
