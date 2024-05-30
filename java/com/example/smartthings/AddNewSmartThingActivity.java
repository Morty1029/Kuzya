package com.example.smartthings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class AddNewSmartThingActivity extends AppCompatActivity {
    ListView listDiscoveredST;
    //ArrayList<String> array_list;
    Button btnDiscover;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_new_device_activity);
        btnDiscover = findViewById(R.id.btn_add_new);


        listDiscoveredST = findViewById(R.id.listView);
        List<String> bulb_array_list = new ArrayList<>();
        for(BulbData bulbInfo : MainActivity.listBulbsInf)
            bulb_array_list.add(bulbInfo.getName()+"    IP:"+bulbInfo.getIp()+":"+bulbInfo.getPort());
        ListAdapter adapter = new ListAdapter(this,bulb_array_list);


        listDiscoveredST.setAdapter(adapter);

        listDiscoveredST.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intentGesture = new Intent(getApplicationContext(), GestureCustomization.class);
                intentGesture.putExtra("selectedBulb",bulb_array_list.get(position));
                startActivity(intentGesture);
            }
        });




    }
}
