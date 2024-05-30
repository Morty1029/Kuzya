package com.example.smartthings;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;


import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {
    private Button btnstartServiceButton;
    private Button btnAddNew;
    private static final String TAG = "Main";
    static List<BulbData> listBulbsInf;
    Map<String, List<Map<String, String>>> bulbSettings;
    String path = Environment.getExternalStorageDirectory().getAbsolutePath().toString() + "/Download/SmartThingsJ.json";

    @RequiresApi(api = Build.VERSION_CODES.R)
    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_page);
        btnAddNew = findViewById(R.id.btn_add_new);
        btnstartServiceButton = findViewById(R.id.btn_start_service);
        File file = new File(path);

        SetPermissions();

        if (!file.exists()) {
            btnstartServiceButton.setEnabled(false);
            ColorStateList colorStateList = ColorStateList.valueOf(getResources().getColor(R.color.grey_light));
            ViewCompat.setBackgroundTintList(btnstartServiceButton, colorStateList);
        } else {
            btnstartServiceButton.setEnabled(true);
            ColorStateList colorStateList = ColorStateList.valueOf(getResources().getColor(R.color.orange_dark));
            ViewCompat.setBackgroundTintList(btnstartServiceButton, colorStateList);
        }


        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
        Python py = Python.getInstance();
        final PyObject pyobj = py.getModule("manager");


        btnstartServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BufferedReader reader = null;
                StringBuilder stringBuilder = null;
                try {
                    reader = new BufferedReader(new FileReader(path));
                    stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Gson gson = new Gson();
                bulbSettings = gson.fromJson(stringBuilder.toString(), new TypeToken<Map<String, List<Map<String, String>>>>() {}.getType());
                startCameraService();

            }
        });


        btnAddNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PyObject object = pyobj.callAttr("discover_all_smart_things");
                try {
                    parsingMsg(object.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startAddNewActivity();
            }
        });
        //PyObject object = pyobj.callAttr("invoked_commands", "192.168.0.103", "color_set", 8);

    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    void SetPermissions() {
        if (!checkPerm(Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.FOREGROUND_SERVICE, Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.SYSTEM_ALERT_WINDOW},
                    0);
        }
        if (!Environment.isExternalStorageManager()) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            startActivity(intent);
        }


    }

    private boolean checkPerm(String perm) {
        int permCheck = ContextCompat.checkSelfPermission(this, perm);
        return (permCheck == PackageManager.PERMISSION_GRANTED);
    }

    private void parsingMsg(String msg) throws JSONException {
        Gson gson = new Gson();
        listBulbsInf = gson.fromJson(msg, new TypeToken<List<BulbData>>() {
        }.getType());
    }

    protected void onDestroy() {
        super.onDestroy();
        Intent serviceIntent = new Intent(this, CameraService.class);
        stopService(serviceIntent);
    }

    private void startCameraService() {
        Intent serviceIntent = new Intent(this, CameraService.class);
        startService(serviceIntent);
    }

    private void stopCameraService() {
        Intent serviceIntent = new Intent(this, CameraService.class);
        stopService(serviceIntent);
    }

    private void startAddNewActivity() {
        Intent add_new_ST = new Intent(this, AddNewSmartThingActivity.class);
        startActivity(add_new_ST);
    }
}
