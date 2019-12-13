package com.test.synchronoss.weatherdiary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class MainActivity extends AppCompatActivity {

    private final int JOB_ID = 101;
    LocationManager locationManager;
    LocationListener locationListener;

    Location presentLocation;
    AlertDialog.Builder alertBuilder;
    private final int PERMISSION_REQUEST_CODE = 100;
    private final String CACHE_DIR_KEY = "WEATHERDIARY_KEY";
    private static final String TAG = "Jyo";

    TextView currentLocation, currentDate, currentTemperature, maxMinTemperature, weatherDescription, currentHumidity, currentPressure;

    Bundle savedInstanceState;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        setContentView(R.layout.activity_main);
        alertBuilder = new AlertDialog.Builder(this);
        currentLocation = (TextView) findViewById(R.id.location);
        currentDate = (TextView) findViewById(R.id.date);
        currentTemperature = (TextView) findViewById(R.id.temperature);
        maxMinTemperature = (TextView) findViewById(R.id.minMaxTemperature);
        weatherDescription = (TextView) findViewById(R.id.description);
        currentHumidity = (TextView) findViewById(R.id.humidity);
        currentPressure = (TextView) findViewById(R.id.pressure);
        getCurrentLocation();
        getWeatherInfo(savedInstanceState);

    }

    @Override
    protected void onStart() {
        super.onStart();
        getCurrentLocation();
        getWeatherInfo(savedInstanceState);
    }

    private void getCurrentLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                setCurrentLocation(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                alertBuilder.setMessage("Location must be turned on to recieve latest weather info. Continue?")
                        .setTitle("Turn on location")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);

                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();

                            }
                        });
                AlertDialog alertDialog = alertBuilder.create();
                alertDialog.show();

            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}, PERMISSION_REQUEST_CODE);
                return;
            }
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,locationListener);
        presentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    Activity#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for Activity#requestPermissions for more details.
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}, PERMISSION_REQUEST_CODE);
                        return;
                    }
                }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            presentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        }

    }

    private void setCurrentLocation(Location location) {
        this.presentLocation = location;
    }


    private void getWeatherInfo(Bundle savedInstanceState) {

        ComponentName componentName = new ComponentName(this,WeatherDataJobScheduler.class);
        PersistableBundle bundle =new PersistableBundle();
        bundle.putDouble("LATITUDE",13.006);//presentLocation.getLatitude());
        bundle.putDouble("LONGITUDE",87.9000);//presentLocation.getLongitude());
        JobInfo jobInfo = new JobInfo.Builder(JOB_ID,componentName)
                .setPersisted(true)
                .setPeriodic(7200000)
                .setExtras(bundle)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .build();
        if(savedInstanceState == null){
            JobScheduler jobScheduler =(JobScheduler)getSystemService(JOB_SCHEDULER_SERVICE);
            int result = jobScheduler.schedule(jobInfo);
        }
        try {
            FileInputStream fis = this.openFileInput(CACHE_DIR_KEY);
            ObjectInputStream objectInputStream = new ObjectInputStream(fis);
            WeatherEntity weatherEntity = (WeatherEntity) objectInputStream.readObject();
            setWeatherData(weatherEntity);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void setWeatherData(WeatherEntity weatherEntity) {

        currentLocation.setText(weatherEntity.getCurrent_location());
        currentDate.setText(weatherEntity.getCurrent_date());
        currentTemperature.setText(weatherEntity.getCurrent_temperature());
        maxMinTemperature.setText(weatherEntity.getMaxMin_temperature());
        weatherDescription.setText(weatherEntity.getWeather_description());
        currentHumidity.setText(weatherEntity.getCurrent_humidity());
        currentPressure.setText(weatherEntity.getCurrent_pressure());
    }

}
