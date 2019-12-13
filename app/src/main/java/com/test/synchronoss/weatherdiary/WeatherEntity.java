package com.test.synchronoss.weatherdiary;


import java.io.Serializable;

public class WeatherEntity implements Serializable {

    private String current_location,current_date,current_temperature,maxMin_temperature,weather_description,current_humidity,current_pressure;

    public String getCurrent_location() {
        return current_location;
    }

    public void setCurrent_location(String current_location) {
        this.current_location = current_location;
    }

    public String getCurrent_date() {
        return current_date;
    }

    public void setCurrent_date(String current_date) {
        this.current_date = current_date;
    }

    public String getCurrent_temperature() {
        return current_temperature;
    }

    public void setCurrent_temperature(String current_temperature) {
        this.current_temperature = current_temperature;
    }

    public String getMaxMin_temperature() {
        return maxMin_temperature;
    }

    public void setMaxMin_temperature(String maxMin_temperature) {
        this.maxMin_temperature = maxMin_temperature;
    }

    public String getWeather_description() {
        return weather_description;
    }

    public void setWeather_description(String weather_description) {
        this.weather_description = weather_description;
    }

    public String getCurrent_humidity() {
        return current_humidity;
    }

    public void setCurrent_humidity(String current_humidity) {
        this.current_humidity = current_humidity;
    }

    public String getCurrent_pressure() {
        return current_pressure;
    }

    public void setCurrent_pressure(String current_pressure) {
        this.current_pressure = current_pressure;
    }
}
