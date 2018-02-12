package com.example.luisfly.android_mapping;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.*;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/* the demo is ok */
public class MainActivity extends AppCompatActivity implements MKOfflineMapListener {

    /* to get the location */
    public LocationClient mLocationClient;

    /* show the location */
    private TextView positionText;
    /* show ratio */
    //private TextView ratio_map;
    /* get map control */
    private MapView mapView;
    /* get button */
    private Button download;
    /* Get offline map */
    private MKOfflineMap mOffline;
    /* Map control entity */
    private BaiduMap baiduMap;
    /* judge if the init or not */
    private boolean isFirstLocate = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* get offline map */
        mOffline = new MKOfflineMap();
        mOffline.init(this);

        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        /* get the map initialize  */
        SDKInitializer.initialize(getApplicationContext());
        //setContentView(R.layout.activity_main);
        positionText = (TextView) findViewById(R.id.position_text_view);
        mapView = (MapView) findViewById(R.id.bmpView);
        download = (Button) findViewById(R.id.download);
        //ratio_map = (TextView) findViewById(R.id.ratio);

        List<String> permissionList = new ArrayList<>();


        /* get the map entity */
        baiduMap = mapView.getMap();
        /* turn on the location setting function */
        baiduMap.setMyLocationEnabled(true);

        //MapStatusUpdate update = MapStatusUpdateFactory.zoomTo(12.5f);
        /* update your new attrbute to the map */
        //baidumap.animateMapStatus(update);

        /* set action listener */
        download.setOnClickListener((View v)->{
            Intent intent = new Intent("com.example.luisfly.Main2Activity.ACTION_START");
            startActivity(intent);
        });

        /* to make sure the permission */
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.
                permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.
                permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.
                permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        /* judge the permission is empty or not */
        if (!permissionList.isEmpty()) {
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        } else {
            /* use the requestLocation method */
            requestLocation();
        }
    }

    private void navigateTo(BDLocation location) {
        /* if it is the init move the location */
        if (isFirstLocate) {
            /* get the latitude and longitude now */
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            /* setting the  attribute */
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            /* update the attribute */
            baiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(16f);
            /* update the attribute */
            baiduMap.animateMapStatus(update);
            isFirstLocate = false;
        }

        /* setting your location and the mark */
        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        /* set your location entity */
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        /* pass your location entity to your map entity */
        MyLocationData locationData = locationBuilder.build();
        baiduMap.setMyLocationData(locationData);
    }

    private void start() {
        mOffline.start(7);
        Toast.makeText(this, "start to download the map", Toast.LENGTH_SHORT).show();
    }

    private void requestLocation() {
        initLocation();
        mLocationClient.start();
    }

    /* after add this method the location is ok */
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);
        option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
        mLocationClient.setLocOption(option);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        mapView.onDestroy();
        /* turn off the location enable when exit the program */
        baiduMap.setMyLocationEnabled(false);
    }

    /* to release the resource property */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /* release the resource property */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "you need the permission to use the program",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                            return ;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(this, "unknown error", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }


    @Override
    public void onGetOfflineMapState(int type, int state) {
        switch (type) {
            case MKOfflineMap.TYPE_VER_UPDATE: {
                MKOLUpdateElement update = mOffline.getUpdateInfo(state);
                if (update != null) {
                    Toast.makeText(this, String.format("%s : %d%%", update.cityName, update.ratio),
                            Toast.LENGTH_SHORT).show();
                }
            }
            break;
            case MKOfflineMap.TYPE_NEW_OFFLINE:
                Log.d("OfflineDemo", String.format("add offlinemap num:%d", state));
                break;

        }
    }

    public class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            StringBuilder currentPosition = new StringBuilder();
            currentPosition.append("latitude: ").append(location.getLatitude()).append("\n");
            currentPosition.append("longitude: ").append(location.getLongitude()).append("\n");
            currentPosition.append("address: ").append(location.getAddress()).append("\n");
            currentPosition.append("location way: ");


            if (location.getLocType() == BDLocation.TypeGpsLocation) {
                currentPosition.append("GPS");
                /* use the new method */
                navigateTo(location);
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
                currentPosition.append("Network");
                /* use the new method */
                navigateTo(location);
            } else {
                currentPosition.append("---> error code ");
                currentPosition.append(location.getLocType());
            }
            positionText.setText(currentPosition);
        }
    }
}
