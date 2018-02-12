package com.example.luisfly.android_mapping;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.view.*;


public class Main2Activity extends AppCompatActivity {

    /* download button */
    private Button download;
    /* remove button */
    private Button remove;
    /* update button */
    private Button update;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        download = (Button) findViewById(R.id.offmap_d);
        remove = (Button) findViewById(R.id.offmap_r);
        update = (Button) findViewById(R.id.offmap_u);

        download.setOnClickListener((View v)->{

        });

        remove.setOnClickListener((View v)->{

        });

        update.setOnClickListener((View v)->{

        });

    }
}
