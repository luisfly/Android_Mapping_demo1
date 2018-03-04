package com.example.luisfly.android_mapping;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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
import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.baidu.mapapi.model.LatLng;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNRoutePlanNode.CoordinateType;
import com.baidu.navisdk.adapter.BaiduNaviManager.NaviInitListener;
import com.baidu.navisdk.adapter.BaiduNaviManager.RoutePlanListener;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.baidu.navisdk.adapter.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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
    /* show the map downloading */
    private TextView downloading;
    /* search the road */
    private Button search;
    /* music activity */
    private Button music;
    /* test */
    private Button test;

    /* 导航变量初始化 */
    private static final String APP_FOLDER_NAME = "Android_Mapping_demo1";

    private String mSDCardPath = null;

    private boolean hasRequestComAuth = false;
    private boolean hasInitSuccess = false;

    private final static String authBaseArr[] =
            { Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION };
    private final static String authComArr[] = { Manifest.permission.READ_PHONE_STATE };
    private final static int authBaseRequestCode = 2;
    private final static int authComRequestCode = 3;

    /* 避免重复算路的 list */
    public static List<Activity> activityList = new LinkedList<Activity>();

    public static final String ROUTE_PLAN_NODE = "routePlanNode";
    public static final String SHOW_CUSTOM_ITEM = "showCustomItem";
    public static final String RESET_END_NODE = "resetEndNode";
    public static final String VOID_MODE = "voidMode";
    String authinfo = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* get the map initialize */
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        /* get offline map */
        mOffline = new MKOfflineMap();
        mOffline.init(this);

        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());

        positionText = (TextView) findViewById(R.id.position_text_view);
        mapView = (MapView) findViewById(R.id.bmpView);
        download = (Button) findViewById(R.id.download);
        downloading = (TextView) findViewById(R.id.downloading);
        search = (Button) findViewById(R.id.search);
        music = (Button) findViewById(R.id.music);
        test = (Button) findViewById(R.id.test);
        //ratio_map = (TextView) findViewById(R.id.ratio);

        List<String> permissionList = new ArrayList<>();


        /* get the map entity */
        baiduMap = mapView.getMap();
        /* turn on the location setting function */
        baiduMap.setMyLocationEnabled(true);

        //MapStatusUpdate update = MapStatusUpdateFactory.zoomTo(12.5f);
        /* update your new attrbute to the map */
        //baiduMap.animateMapStatus(update);

        /* set action listener */
        download.setOnClickListener((View v)->{
            Intent intent = new Intent("com.example.luisfly.Main2Activity.ACTION_START");
            startActivity(intent);
        });

        /* set action listener */
        search.setOnClickListener((View v)->{
            routeplanToNavi(CoordinateType.WGS84);
        });

        /* set music listener */
        music.setOnClickListener((View v)->{
            Intent intent = new Intent("com.example.luisfly.MusicPlayer.ACTION_START");
            startActivity(intent);
        });

        /* set music listener */
        test.setOnClickListener((View v)->{
            Intent intent = new Intent("com.example.luisfly.MessageShow.ACTION_START");
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
//            initOfflineMap();
            /* use the requestLocation method */
            requestLocation();
            /* 语音播报功能初始化 */
            if (initDirs()) {
                initNavi();
            }
        }
    }

    /**
     * 离线地图下载
     */
    private void initOfflineMap() {
        ArrayList<MKOLSearchRecord> offlinesupport = mOffline.getOfflineCityList();
        ArrayList<MKOLUpdateElement> localMapList = mOffline.getAllUpdateInfo();
        HashMap<Integer, Integer> localMapListID = new HashMap();

        if (localMapList != null) {
            /* put all downloaded city id into hashmap */
            for (MKOLUpdateElement r : localMapList) {
                localMapListID.put(r.cityID, r.cityID);
            }
        }

        /*if ( localMapListID.get(8) == null ) {
            mOffline.start(8);
            Toast.makeText(this, "start to download the map", Toast.LENGTH_SHORT).show();
        }*/

        /* download all China map */
        /*for (MKOLSearchRecord r : offlinesupport) {
            if( r.cityID <= 9000 && (localMapListID.get(r.cityID) == null) ) {
                mOffline.start(r.cityID);
            }
        }*/
    }

    private void navigateTo(BDLocation location) {
        /* if it is the init move the location */
        if (isFirstLocate) {
            /* get the latitude and longitude now */
           LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            /* setting the attribute */
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
        /* remmeber to change to Hight_Accuracy  */
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        mLocationClient.setLocOption(option);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mOffline.destroy();
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
            case authBaseRequestCode:
                for (int ret : grantResults) {
                    if (ret == 0) {
                        continue;
                    } else {
                        Toast.makeText(MainActivity.this, "缺少导航基本的权限!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                initNavi();
                break;
            case authComRequestCode:
                for (int ret : grantResults) {
                    if (ret == 0) {
                        continue;
                    }
                }
                routeplanToNavi(mCoordinateType);
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
                    //Toast.makeText(this, String.format("%s : %d%%", update.cityName, update.ratio),
                    //        Toast.LENGTH_SHORT).show();
                    downloading.setText(String.format("%s : %d", update.cityName,update.ratio));
                }
            }
            break;
            default:
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


    private boolean initDirs() {
        mSDCardPath = getSdcardDir();
        if (mSDCardPath == null) {
            return false;
        }
        File f = new File(mSDCardPath, APP_FOLDER_NAME);
        if (!f.exists()) {
            try {
                f.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private String getSdcardDir() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }

    /**
     * 验证权限是否完整
     */
    private boolean hasCompletePhoneAuth() {

        PackageManager pm = this.getPackageManager();
        for (String auth : authComArr) {
            if (pm.checkPermission(auth, this.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 基础权限查询
     */
    private void initNavi() {

        BNOuterTTSPlayerCallback ttsCallback = null;

        // 申请权限
        if (android.os.Build.VERSION.SDK_INT >= 23) {

            if (!hasBasePhoneAuth()) {

                this.requestPermissions(authBaseArr, authBaseRequestCode);
                return;

            }
        }

        BaiduNaviManager.getInstance().init(this, mSDCardPath, APP_FOLDER_NAME, new NaviInitListener() {
            @Override
            public void onAuthResult(int status, String msg) {
                if (0 == status) {
                    authinfo = "key校验成功!";
                } else {
                    authinfo = "key校验失败, " + msg;
                }
                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, authinfo, Toast.LENGTH_LONG).show();
                    }
                });
            }

            public void initSuccess() {
                Toast.makeText(MainActivity.this, "百度导航引擎初始化成功", Toast.LENGTH_SHORT).show();
                hasInitSuccess = true;
                initSetting();
            }

            public void initStart() {
                Toast.makeText(MainActivity.this, "百度导航引擎初始化开始", Toast.LENGTH_SHORT).show();
            }

            public void initFailed() {
                Toast.makeText(MainActivity.this, "百度导航引擎初始化失败", Toast.LENGTH_SHORT).show();
            }

        }, null, ttsHandler, ttsPlayStateListener);

    }

    /**
     * 基础权限验证
     * @return
     */
    private boolean hasBasePhoneAuth() {

        PackageManager pm = this.getPackageManager();
        for (String auth : authBaseArr) {
            if (pm.checkPermission(auth, this.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 引擎设置初始化
     */
    private void initSetting() {
        // BNaviSettingManager.setDayNightMode(BNaviSettingManager.DayNightMode.DAY_NIGHT_MODE_DAY);
        BNaviSettingManager
                .setShowTotalRoadConditionBar(BNaviSettingManager.PreViewRoadCondition.ROAD_CONDITION_BAR_SHOW_ON);
        BNaviSettingManager.setVoiceMode(BNaviSettingManager.VoiceMode.Veteran);
        // BNaviSettingManager.setPowerSaveMode(BNaviSettingManager.PowerSaveMode.DISABLE_MODE);
        BNaviSettingManager.setRealRoadCondition(BNaviSettingManager.RealRoadCondition.NAVI_ITS_ON);
        BNaviSettingManager.setIsAutoQuitWhenArrived(true);
        Bundle bundle = new Bundle();
        // 必须设置APPID，否则会静音
        bundle.putString(BNCommonSettingParam.TTS_APP_ID, "10835756");
        BNaviSettingManager.setNaviSdkParam(bundle);
    }

    /**
     * 内部TTS播报状态回传handler
     */
    private Handler ttsHandler = new Handler() {
        public void handleMessage(Message msg) {
            int type = msg.what;
            switch (type) {
                case BaiduNaviManager.TTSPlayMsgType.PLAY_START_MSG: {
                    // showToastMsg("Handler : TTS play start");
                    break;
                }
                case BaiduNaviManager.TTSPlayMsgType.PLAY_END_MSG: {
                    // showToastMsg("Handler : TTS play end");
                    break;
                }
                default:
                    break;
            }
        }
    };

    /**
     * 内部TTS播报状态回调接口
     */
    private BaiduNaviManager.TTSPlayStateListener ttsPlayStateListener = new BaiduNaviManager.TTSPlayStateListener() {

        @Override
        public void playEnd() {
            // showToastMsg("TTSPlayStateListener : TTS play end");
        }

        @Override
        public void playStart() {
            // showToastMsg("TTSPlayStateListener : TTS play start");
        }
    };

    private CoordinateType mCoordinateType = null;
    /**
     * 算路功能
     * @param coType
     */
    private void routeplanToNavi(CoordinateType coType) {
        mCoordinateType = coType;
        if (!hasInitSuccess) {
            Toast.makeText(MainActivity.this, "还未初始化!", Toast.LENGTH_SHORT).show();
        }
        // 权限申请
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            // 保证导航功能完备
            if (!hasCompletePhoneAuth()) {
                if (!hasRequestComAuth) {
                    hasRequestComAuth = true;
                    this.requestPermissions(authComArr, authComRequestCode);
                    return;
                } else {
                    Toast.makeText(MainActivity.this, "没有完备的权限!", Toast.LENGTH_SHORT).show();
                }
            }

        }
        BNRoutePlanNode sNode = null;
        BNRoutePlanNode eNode = null;
        switch (coType) {
            case GCJ02: {
                sNode = new BNRoutePlanNode(116.30142, 40.05087, "百度大厦", null, coType);
                eNode = new BNRoutePlanNode(116.39750, 39.90882, "北京天安门", null, coType);
                break;
            }
            case WGS84: {
                sNode = new BNRoutePlanNode(116.300821, 40.050969, "百度大厦", null, coType);
                eNode = new BNRoutePlanNode(116.397491, 39.908749, "北京天安门", null, coType);
                break;
            }
            case BD09_MC: {
                sNode = new BNRoutePlanNode(12947471, 4846474, "百度大厦", null, coType);
                eNode = new BNRoutePlanNode(12958160, 4825947, "北京天安门", null, coType);
                break;
            }
            case BD09LL: {
                sNode = new BNRoutePlanNode(116.30784537597782, 40.057009624099436, "百度大厦", null, coType);
                eNode = new BNRoutePlanNode(116.40386525193937, 39.915160800132085, "北京天安门", null, coType);
                break;
            }
            default:
                ;
        }
        if (sNode != null && eNode != null) {
            List<BNRoutePlanNode> list = new ArrayList<BNRoutePlanNode>();
            list.add(sNode);
            list.add(eNode);

            // 开发者可以使用旧的算路接口，也可以使用新的算路接口,可以接收诱导信息等
            // BaiduNaviManager.getInstance().launchNavigator(this, list, 1, true, new DemoRoutePlanListener(sNode));
            BaiduNaviManager.getInstance().launchNavigator(this, list, 1, true, new DemoRoutePlanListener(sNode),
                    eventListerner);
        }
    }

    /**
     * BNEventDialog 以及 BNEventHandler 两个类都是由这个监听器调用
     */
    BaiduNaviManager.NavEventListener eventListerner = new BaiduNaviManager.NavEventListener() {

        @Override
        public void onCommonEventCall(int what, int arg1, int arg2, Bundle bundle) {
            BNEventHandler.getInstance().handleNaviEvent(what, arg1, arg2, bundle);
        }
    };

    /**
     * 算路并将结果传送到显示算路结果的 activity 中
     */
    public class DemoRoutePlanListener implements RoutePlanListener {

        private BNRoutePlanNode mBNRoutePlanNode = null;

        public DemoRoutePlanListener(BNRoutePlanNode node) {
            mBNRoutePlanNode = node;
        }

        @Override
        public void onJumpToNavigator() {

            // 设置途径点以及resetEndNode会回调该接口

            for (Activity ac : activityList) {

                if (ac.getClass().getName().endsWith("BNDemoGuideActivity")) {

                    return;
                }
            }
            Intent intent = new Intent(MainActivity.this, GuideActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(ROUTE_PLAN_NODE, (BNRoutePlanNode) mBNRoutePlanNode);
            intent.putExtras(bundle);
            startActivity(intent);

        }

        @Override
        public void onRoutePlanFailed() {
            Toast.makeText(MainActivity.this, "算路失败", Toast.LENGTH_SHORT).show();
        }
    }

}

