package com.example.luisfly.android_mapping;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;

import java.util.ArrayList;

public class CityList extends AppCompatActivity implements MKOfflineMapListener{

    /* offline map */
    private MKOfflineMap mOffline = null;
    /* city list */
    private ListView city_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_list);

        /* init controller */
        city_list = (ListView) findViewById(R.id.city_list);

        mOffline = new MKOfflineMap();
        mOffline.init(this);

        /* get all support city list */
        ArrayList<MKOLSearchRecord> city_record = mOffline.getOfflineCityList();
        /*  */
        ArrayList<String> allCities = new ArrayList<String>();

        /* make adapter */
        if(city_record != null) {
            for(MKOLSearchRecord r: city_record) {
                allCities.add(String.format(r.cityName + " : " + r.cityID + " -- " + this.formatDataSize(r.dataSize)));
            }
        }


        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, allCities);
        city_list.setAdapter(adapter);

        /* if you click a selection it will return the select to the abo */
        city_list.setOnItemClickListener((AdapterView<?> parent, View v, int position, long id)->{
            Intent intent = new Intent();
            MKOLSearchRecord res = city_record.get(position);
            intent.putExtra("city_name", res.cityName);
            intent.putExtra("city_id", res.cityID+"" );
            setResult(RESULT_OK, intent);
            finish();
        });
    }

    private String formatDataSize(long size) {
        String ret = "";
        if (size < (1024 * 1024)) {
            ret = String.format("%dk", size / 1024);
        } else {
            ret = String.format("%.1fM", size / (1024 * 1024.0));
        }
        return ret;
    }

    @Override
    public void onGetOfflineMapState(int i, int i1) {

    }
}
