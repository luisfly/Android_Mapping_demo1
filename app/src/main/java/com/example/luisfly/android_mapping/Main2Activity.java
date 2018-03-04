package com.example.luisfly.android_mapping;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;


public class Main2Activity extends AppCompatActivity implements MKOfflineMapListener{

    /* download button */
    private Button download;
    /* remove button */
    private Button remove;
    /* update button */
    private Button update;
    /* offline map */
    private MKOfflineMap mOffline = null;
    /*  cityId */
    private TextView cityId;
    /* city */
    private TextView city;
    /* radio */
    private TextView radio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        mOffline = new MKOfflineMap();
        mOffline.init(this);

        download = (Button) findViewById(R.id.offmap_d);
        remove = (Button) findViewById(R.id.offmap_r);
        update = (Button) findViewById(R.id.offmap_u);
        city = (TextView) findViewById(R.id.city);
        cityId = (TextView) findViewById(R.id.cityid);
        radio = (TextView) findViewById(R.id.radio);


        /* downl)oad the map */
        download.setOnClickListener((View v)->{
            //mOffline.start(Integer.getInteger(cityId.getText()+""));
            mOffline.start(131);
            Toast.makeText(this, cityId.getText() + " start to download",
                    Toast.LENGTH_SHORT).show();
        });

        remove.setOnClickListener((View v)->{

        });

        update.setOnClickListener((View v)->{

        });

        /* show the city list provide user to select */
        city.setOnClickListener((View v)->{
            /* open the city list */
            Intent intent = new Intent(Main2Activity.this, CityList.class);
            startActivityForResult(intent, 1);
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /* you can open several activity to judge by requestCode */
        switch (requestCode) {
            case 1:
                /* return the selection to this activity */
                if (resultCode  == RESULT_OK) {
                    city.setText(data.getStringExtra("city_name"));
                    cityId.setText(data.getStringExtra("city_id"));
                }
                Toast.makeText(this, String.format(data.getStringExtra("city_name") + " : " +
                        data.getStringExtra("city_id")), Toast.LENGTH_SHORT).show();
                break;
            default:
        }
    }

    @Override
    public void onGetOfflineMapState(int type, int state) {
        switch (type) {
            case MKOfflineMap.TYPE_DOWNLOAD_UPDATE: {
                MKOLUpdateElement update = mOffline.getUpdateInfo(state);
                if (update != null) {
                    radio.setText(String.format("%s : %d", update.cityName,update.ratio));
                }
            }
            break;
        }
    }
}
