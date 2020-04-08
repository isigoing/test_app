package com.example.myfirstapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    TextView textViewPosition;
    TextView textViewCoverage;
    int counter;
    String strength = "";
    int jsonCounter = 0;
    ArrayList<String> jsonArray = new ArrayList<>();
    LocationManager locationManager;
    Context context;
    String json;

    LocationListener locationListener = new LocationListener() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        public void onLocationChanged(Location location) {
            double lat = location.getLatitude();
            double lon = location.getLongitude();

            textViewPosition.setText("Latitude: " + lat + "\n" + "Longitude: " + lon);
            getSignalStrength();
            textViewCoverage.setText(strength);


            try {
                json = new JSONObject()
                        .put("position", new JSONObject()
                                .put("latitude", lat)
                                .put("longitude", lon))
                        .put("strength", strength).toString();
                jsonArray.add(json);

            } catch (JSONException e) {
                e.printStackTrace();
            }



            if (jsonArray.size() == 10) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Ready to send");
                        try {
                            String ipadress = "";
                            URL url = new URL("http://["+ ipadress + "]:8080/data");
                            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                            urlConnection.setRequestMethod("POST");
                            urlConnection.setRequestProperty("Content-Type", "application/json");
                            urlConnection.setRequestProperty("Accept", "application/json");
                            urlConnection.setDoOutput(true);
                            urlConnection.setDoInput(true);
                            urlConnection.connect();
                            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
                            writer.write(String.valueOf(jsonArray));
                            writer.flush();
                            writer.close();
                            out.close();
                            Log.i("STATUS", String.valueOf(urlConnection.getResponseCode()));
                            Log.i("MSG", urlConnection.getResponseMessage());
                            urlConnection.disconnect();
                            jsonArray.clear();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();
            }

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("Latitude", "disable");
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d("Latitude", "enable");
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d("Latitude", "status");
        }
    };


    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println(" ------------------- here we start  ------------");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewPosition = (TextView) findViewById(R.id.textViewPosition);
        textViewCoverage = (TextView) findViewById(R.id.textViewCoverage);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);


    }

    public void getPositionAndCoverage(View view) {
        textViewPosition.setText("this is my position" + counter);
        textViewCoverage.setText("beeeeeeeeeeeeeep" + counter++);
    }


    public void sendMessage(View view) {
        //todo add something
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.editText);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);

    }

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void getSignalStrength() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        @SuppressLint("MissingPermission") List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();   //This will give info of all sims present inside your mobile
        if (cellInfos != null) {
            for (int i = 0; i < cellInfos.size(); i++) {
                if (cellInfos.get(i).isRegistered()) {
                    if (cellInfos.get(i) instanceof CellInfoWcdma) {
                        @SuppressLint("MissingPermission") CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) telephonyManager.getAllCellInfo().get(0);
                        CellSignalStrengthWcdma cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();
                        strength = String.valueOf(cellSignalStrengthWcdma.getDbm());
                    } else if (cellInfos.get(i) instanceof CellInfoGsm) {
                        @SuppressLint("MissingPermission") CellInfoGsm cellInfogsm = (CellInfoGsm) telephonyManager.getAllCellInfo().get(0);
                        CellSignalStrengthGsm cellSignalStrengthGsm = cellInfogsm.getCellSignalStrength();
                        strength = String.valueOf(cellSignalStrengthGsm.getDbm());
                    } else if (cellInfos.get(i) instanceof CellInfoLte) {
                        @SuppressLint("MissingPermission") CellInfoLte cellInfoLte = (CellInfoLte) telephonyManager.getAllCellInfo().get(0);
                        CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
                        strength = String.valueOf(cellSignalStrengthLte.getDbm());
                    }
                }
            }
        }
    }
}

