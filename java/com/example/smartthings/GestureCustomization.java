package com.example.smartthings;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GestureCustomization extends AppCompatActivity {
    ImageView viewOk;
    ImageView viewLike;
    ImageView viewDislike;
    ImageView viewRock;
    Spinner spinnerOk;
    Spinner spinnerRock;
    Spinner spinnerLike;
    Spinner spinnerDislike;
    int selected_itemOk;
    int selected_itemDislike;
    int selected_itemLike;
    int selected_itemRock;
    Button btnSubmit;
    Map<String, List<Map<String, String>>> bulbSettings;
    List<Map<String, String>> listGestureAndCommands = new ArrayList<>();
    int GESTURE_COUNT = 4;
    String path = Environment.getExternalStorageDirectory().getAbsolutePath().toString() + "/Download/" + "SmartThingsJ.json";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gesture_customization_activity);

        Intent intent = getIntent();
        String selectedBulb = intent.getStringExtra("selectedBulb");


        viewOk = findViewById(R.id.imageViewOk);
        viewOk.setImageResource(R.drawable.ok);

        viewLike = findViewById(R.id.imageViewLike);
        viewLike.setImageResource(R.drawable.like);

        viewDislike = findViewById(R.id.imageViewDislike);
        viewDislike.setImageResource(R.drawable.dislike);

        viewRock = findViewById(R.id.imageViewRock);
        viewRock.setImageResource(R.drawable.rock);

        String[] arraySpinner = new String[]{"nothing", "switch on", "switch off", "red light",
                                            "white light", "green light", "orange light",
                                            "purple light", "yellow light", "cyan light","blue light"};
        spinnerOk = findViewById(R.id.spinner_Ok);
        spinnerRock = findViewById(R.id.spinner_Rock);
        spinnerLike = findViewById(R.id.spinner_Like);
        spinnerDislike = findViewById(R.id.spinner_Dislike);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerOk.setAdapter(adapter);
        spinnerRock.setAdapter(adapter);
        spinnerLike.setAdapter(adapter);
        spinnerDislike.setAdapter(adapter);
        spinnerOk.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected_itemOk = spinnerOk.getSelectedItemPosition();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinnerRock.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected_itemRock = spinnerRock.getSelectedItemPosition();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinnerLike.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected_itemLike = spinnerLike.getSelectedItemPosition();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinnerDislike.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected_itemDislike = spinnerDislike.getSelectedItemPosition();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnSubmit = findViewById(R.id.btn_submit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                List<String> gestures = new ArrayList<>(Arrays.asList("rock", "ok", "like", "dislike"));
                List<Integer> commands = new ArrayList<>(Arrays.asList(selected_itemRock, selected_itemOk, selected_itemLike, selected_itemDislike));
                for (int i = 0; i < GESTURE_COUNT; i++) {
                    Map<String, String> gestureAndCommand = new HashMap<>();
                    gestureAndCommand.put(gestures.get(i), arraySpinner[commands.get(i)]);
                    listGestureAndCommands.add(gestureAndCommand);
                }
                bulbSettings = new HashMap<>();
                bulbSettings.put(selectedBulb, listGestureAndCommands);

                Gson gson = new Gson();
                String json = gson.toJson(bulbSettings);

                File file = new File(path);
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    try {
                        clearTheFile(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


                FileWriter writer =null;
                try {
                    writer=  new FileWriter(file, true);
                    writer.write(json);
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();

            }
        });

    }

    public static void clearTheFile(File file) throws IOException {
        FileWriter fwOb = new FileWriter(file, false);
        PrintWriter pwOb = new PrintWriter(fwOb, false);
        pwOb.flush();
        pwOb.close();
        fwOb.close();
    }

}
