package com.test.synchronoss.weatherdiary;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WeatherDataJobScheduler extends JobService {

    private String url = "https://api.openweathermap.org/data/2.5/weather?";
    private String appId = "5ad7218f2e11df834b0eaf3a33a39d2a";
    StringBuffer buffer = new StringBuffer(url);
    private final String CACHE_DIR_KEY = "WEATHERDIARY_KEY";
    WeatherEntity weatherInfo;
    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d("Jyo","on job start");
        double lat = params.getExtras().getDouble("LATITUDE");
        double lng = params.getExtras().getDouble("LONGITUDE");

        //prepare URL
        buffer.append("lat="+lat+"&lon="+lng+"&appid="+appId);


        OkHttpClient httpClient = new OkHttpClient();

        Request request = new Request.Builder()
                .url(buffer.toString())
                .build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("Jyo","Request failed");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()){
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("weather");
                        JSONObject itemObject = jsonArray.getJSONObject(0);
                        JSONObject object;
                        object = jsonObject.getJSONObject("main");
                        weatherInfo = new WeatherEntity();
                        weatherInfo.setCurrent_temperature(String.valueOf(Math.round(((object.getDouble("temp")-273.15)*10)/10.0))+"°");
                        weatherInfo.setMaxMin_temperature("Max:"+String.valueOf(Math.round(((object.getDouble("temp_max")-273.15)*10)/10.0))+"°"+" / "+" Min:"+String.valueOf(Math.round(((object.getDouble("temp_min")-273.15)*10)/10.0))+"°");
                        weatherInfo.setCurrent_location(jsonObject.getString("name"));
                        long dateTime;
                        dateTime = jsonObject.getLong("dt");
                        SimpleDateFormat formatter = new SimpleDateFormat("dd/mm/yyyy:hh:ss");

                        // Create a calendar object that will convert the date and time value in milliseconds to date.
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(dateTime);

                        weatherInfo.setCurrent_date(String.valueOf(formatter.format(calendar.getTime())));
                        weatherInfo.setWeather_description(itemObject.getString("description"));
                        weatherInfo.setCurrent_humidity("Humidity: "+String.valueOf(object.getInt("humidity")));
                        weatherInfo.setCurrent_pressure("Pressure: "+String.valueOf(object.getInt("pressure")));
                        FileOutputStream fos = null;
                        //Object object = new Object();//temp
                        try {
                            fos = getApplicationContext().openFileOutput(CACHE_DIR_KEY, Context.MODE_PRIVATE);
                            ObjectOutputStream oos = new ObjectOutputStream(fos);
                            oos.writeObject(weatherInfo);
                            oos.close();
                            fos.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
