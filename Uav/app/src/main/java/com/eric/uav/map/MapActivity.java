package com.eric.uav.map;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.PolylineOptions;
import com.bumptech.glide.Glide;
import com.eric.uav.R;
import com.eric.uav.Settings;
import com.eric.uav.applications.ApplicationActivity;
import com.eric.uav.homepage.HomePageActivity;
import com.eric.uav.login.LoginActivity;
import com.eric.uav.profile.ProfileActivity;
import com.eric.uav.utils.Dialog;
import com.eric.uav.utils.HttpUtils;
import com.eric.uav.utils.MarkerUtils;
import com.eric.uav.zxing.android.CaptureActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.xuexiang.xui.widget.imageview.RadiusImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapActivity extends AppCompatActivity implements View.OnClickListener, AMap.OnMapClickListener {
    private MapView mapView;
    private MyLocationStyle myLocationStyle;
    private AMap aMap;

    // 底部栏相关
    private RelativeLayout homepageActivityView;
    private RelativeLayout profileActivityView;
    private RelativeLayout applicationActivityView;


    private TextView moreFuncBtn;
    private LinearLayout logoutBtn;
    private LinearLayout scanScreen;
    private LinearLayout openAdmin;

    private SharedPreferences sharedPreferences;

    private PopupWindow popupWindow;

    private static final int M_PERMISSION_CODE = 1001;

    // 点击过的位置
    private List<LatLng> clickLatLngList = new ArrayList<>();
    private Marker uavMarker;   // 无人机的marker

    // 悬浮球相关内容
    private FloatingActionButton floatBall;
    private LinearLayout ballContent;
    private int[] contentBallId = new int[]{R.id.phone_position_ball, R.id.uav_position_ball, R.id.current_location_ball};
    private FloatingActionButton[] contentBall = new FloatingActionButton[contentBallId.length];
    private int[] contentItemId = new int[]{R.id.phone_position, R.id.uav_position, R.id.current_location};
    private LinearLayout[] contentItem = new LinearLayout[contentItemId.length];
    private AnimatorSet[] addBillTranslate = new AnimatorSet[contentItemId.length];
    private boolean isAdd = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        sharedPreferences = getSharedPreferences("register", MODE_PRIVATE);

        // 初始化样式
        ((TextView) findViewById(R.id.title)).setText("地图");
        findViewById(R.id.homepage_activity_item).setBackgroundResource(R.drawable.home_page);
        findViewById(R.id.map_activity_item).setBackgroundResource(R.drawable.map_select);
        findViewById(R.id.application_activity_item).setBackgroundResource(R.drawable.other);
        findViewById(R.id.personnal_activity_item).setBackgroundResource(R.drawable.mine);

        ((TextView) findViewById(R.id.homepage_activity_item_tips)).setTextColor(getResources().getColor(R.color.no_select_color));
        ((TextView) findViewById(R.id.map_activity_item_tips)).setTextColor(getResources().getColor(R.color.select_color));
        ((TextView) findViewById(R.id.application_activity_item_tips)).setTextColor(getResources().getColor(R.color.no_select_color));
        ((TextView) findViewById(R.id.personnal_activity_item_tips)).setTextColor(getResources().getColor(R.color.no_select_color));
        // 加载头像
        Glide.with(this).load("http://" + Settings.ServerHost + ":" +
                Settings.ServerPort + sharedPreferences.getString("avatar", "/static/assets/img/profile.jpg"))
                .into((RadiusImageView) findViewById(R.id.top_avatar));


        homepageActivityView = findViewById(R.id.homepage_activity);
        homepageActivityView.setOnClickListener(this);
        profileActivityView = findViewById(R.id.personnal_activity);
        profileActivityView.setOnClickListener(this);
        applicationActivityView = findViewById(R.id.application_activity);
        applicationActivityView.setOnClickListener(this);

        // 获取权限
        getPermission();

        // 加载地图样式
        mapView = findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        aMap = mapView.getMap();
        // 定位
        locationMap();
        // 设置地图ui样式
        setMapUi();
        // 设置地图点击事件
        aMap.setOnMapClickListener(this);

        moreFuncBtn = findViewById(R.id.more_func);
        moreFuncBtn.setOnClickListener(this);

        // 悬浮球相关
        initView();
        bindEvents();
    }


    /**
     * 初始化悬浮球相关变量
     */
    public void initView() {
        floatBall = findViewById(R.id.floatBall);
        ballContent = findViewById(R.id.ball_content);
        for (int i = 0; i < contentBallId.length; i++) {
            contentBall[i] = findViewById(contentBallId[i]);
        }

        for (int i = 0; i < contentItemId.length; i++) {
            contentItem[i] = findViewById(contentItemId[i]);
        }

        for (int i = 0; i < addBillTranslate.length; i++) {
            addBillTranslate[i] = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.add_bill_anim);
        }
    }


    /**
     * 添加悬浮窗的动画
     */
    private void bindEvents() {
        floatBall.setOnClickListener(this);
        for (int i = 0; i < contentBallId.length; i++) {
            contentBall[i].setOnClickListener(this);
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.homepage_activity: {
                // 跳转到首页
                Intent intent = new Intent(MapActivity.this, HomePageActivity.class);
                startActivity(intent);
                // 去掉入场动画
                overridePendingTransition(0, 0);
            }
            break;
            case R.id.current_location_ball: {
                Dialog.toastWithoutAppName(MapActivity.this,
                        "当前位置：\n经度：" + aMap.getMyLocation().getLongitude() + "\n纬度：" + aMap.getMyLocation().getLatitude());
            }
            break;
            case R.id.personnal_activity: {
                // 跳转到首页
                Intent intent = new Intent(MapActivity.this, ProfileActivity.class);
                startActivity(intent);
                // 去掉入场动画
                overridePendingTransition(0, 0);
            }
            break;
            case R.id.application_activity: {
                // 跳转到首页
                Intent intent = new Intent(MapActivity.this, ApplicationActivity.class);
                startActivity(intent);
                // 去掉入场动画
                overridePendingTransition(0, 0);
            }
            break;
            case R.id.logout_lin: {
                AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                builder.setTitle("确认退出？");
                builder.setIcon(R.drawable.profile_logout);
                builder.setMessage("是否要退出登录？");
                builder.setPositiveButton("退出", (dialog, which) -> {
                    // 供存储使用
                    SharedPreferences sharedPreferences = getSharedPreferences("register", MODE_PRIVATE);
                    @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("logined", "false");
                    editor.apply();
                    // 跳转到登录页面
                    startActivity(new Intent(MapActivity.this, LoginActivity.class));
                    overridePendingTransition(0, 0);
                });
                builder.setNegativeButton("取消", (dialog, which) -> {
                });
                builder.show();
            }
            break;
            case R.id.more_func: {
                View moreFuncView = getLayoutInflater().inflate(R.layout.popupwindow_more_func, null);
                popupWindow = new PopupWindow(moreFuncView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                popupWindow.setOutsideTouchable(true);    // 点击其他区域能够隐藏popupWindow
                popupWindow.setFocusable(true);    // 设置点击一下出现，再点击隐藏的效果
                popupWindow.showAsDropDown(moreFuncBtn);
                // 设置阴影
                WindowManager.LayoutParams lp = MapActivity.this.getWindow().getAttributes();
                lp.alpha = 0.8f;
                MapActivity.this.getWindow().setAttributes(lp);
                popupWindow.setOnDismissListener(() -> {
                    WindowManager.LayoutParams lpDel = MapActivity.this.getWindow().getAttributes();
                    lpDel.alpha = 1f;
                    MapActivity.this.getWindow().setAttributes(lpDel);
                });

                logoutBtn = moreFuncView.findViewById(R.id.logout_lin);
                logoutBtn.setOnClickListener(this);

                scanScreen = moreFuncView.findViewById(R.id.scan_btn);
                scanScreen.setOnClickListener(this);

                openAdmin = moreFuncView.findViewById(R.id.open_admin);
                openAdmin.setOnClickListener(this);
            }
            break;
            case R.id.scan_btn: {
                // 开始二维码扫描
                startActivity(new Intent(this, CaptureActivity.class));
            }
            break;
            case R.id.open_admin: {
                popupWindow.dismiss();
                Intent intent = new Intent();
                intent.setData(Uri.parse("http://" + Settings.ServerHost + ":" + Settings.ServerPort + "/userManage/index/"));
                intent.setAction(Intent.ACTION_VIEW);
                startActivity(intent);
            }
            break;
            case R.id.phone_position_ball: {
                // 获取当前定位
                // 经度
                double longitude = aMap.getMyLocation().getLongitude();
                // 纬度
                double latitude = aMap.getMyLocation().getLatitude();
                CameraUpdate mCameraUpdate = CameraUpdateFactory.newCameraPosition(
                        new CameraPosition(new LatLng(latitude, longitude), 15, 30, 0));
                aMap.moveCamera(mCameraUpdate);
            }
            break;
            case R.id.uav_position_ball: {
                if (uavMarker != null) {
                    // 如果已经绘制过了，先清除上一次绘制的位置
                    uavMarker.remove();
                    // 刷新地图
                    aMap.reloadMap();
                }
                // 获取无人机定位
                HttpUtils httpUtils = new HttpUtils() {
                    @Override
                    public void callback(String result) {
                        String[] location = result.split("\\|");
                        LatLng latLng = new LatLng(Double.parseDouble(location[0]), Double.parseDouble(location[1]));
                        CameraUpdate mCameraUpdate = CameraUpdateFactory.newCameraPosition(
                                new CameraPosition(latLng, 10, 30, 0));
                        aMap.moveCamera(mCameraUpdate);
                        // 绘制marker
                        uavMarker = MarkerUtils.drawUavMarker(latLng, MapActivity.this, aMap);
                        clickLatLngList.add(0, latLng);
                    }
                };
                Map<String, String> param = new HashMap<>();
                param.put("null", "null");
                httpUtils.sendPost(Settings.routerMap.get("get_location"), param);
            }
            break;
            case R.id.floatBall: {

                floatBall.setImageResource(isAdd ? R.drawable.open_float_btn : R.drawable.close_float_btn);
                isAdd = !isAdd;
                ballContent.setVisibility(isAdd ? View.VISIBLE : View.GONE);
                if (isAdd) {
                    for (int i = 0; i < addBillTranslate.length; i++) {
                        addBillTranslate[i].setTarget(contentItem[i]);
                        addBillTranslate[i].setStartDelay(150 * i);
                        addBillTranslate[i].start();
                    }
                }
            }
            break;
            default:
                break;
        }
    }

    /**
     * 获取当前定位
     */
    public void locationMap() {
        // 初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);
        // 连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle = new MyLocationStyle();
        // 连续定位、蓝点不会移动到地图中心点，地图依照设备方向旋转，并且蓝点会跟随设备移动
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_MAP_ROTATE_NO_CENTER);
        // 设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.interval(2000);
        // 自定义定位图标
//        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.map_location));
        // 设置定位蓝点的Style
        aMap.setMyLocationStyle(myLocationStyle);
        // 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        aMap.setMyLocationEnabled(true);
        // 开启室内地图，缩放到一定级别（>=17）时会显示
        aMap.showIndoorMap(true);
        // 显示3D建筑
        aMap.showBuildings(true);
        // 显示底图文字
        aMap.showMapText(true);
        //显示实时的交通路况
        aMap.setTrafficEnabled(true);
    }

    /**
     * 设置地图的ui样式
     */
    public void setMapUi() {
        UiSettings uiSettings = aMap.getUiSettings();
        // 显示指南针
        uiSettings.setCompassEnabled(true);
        // 设置比例尺
        uiSettings.setScaleControlsEnabled(true);
        //显示默认的定位按钮
        uiSettings.setMyLocationButtonEnabled(true);
        // 不显示缩放按钮
        uiSettings.setZoomControlsEnabled(false);
    }

    /**
     * 申请权限
     */
    public void getPermission() {
        //判断当前Android版本是否大于等于6.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 需要申请权限,为了方便，直接将地图需要的权限都申请,启动就会提示授权全部权限
            if (!(checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    && checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    && checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    && checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    && checkPermission(Manifest.permission.READ_PHONE_STATE))) {
                // 有权限未申请
                // 开启系统权限申请
                String[] mPermissions = {
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE
                };
                requestPermissions(mPermissions, M_PERMISSION_CODE);
            }
        }
    }

    /**
     * 检查指定权限是否允许
     */
    private boolean checkPermission(String permission) {
        return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 获取权限的返回结果
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case M_PERMISSION_CODE:
                if (!(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED
                        && grantResults[3] == PackageManager.PERMISSION_GRANTED
                        && grantResults[4] == PackageManager.PERMISSION_GRANTED)) {
                    // 未得到申请权限的授权，不能执行
                    Toast.makeText(this, "请通过全部权限申请，否则无法执行下一步操作", Toast.LENGTH_SHORT).show();
                    this.finish();
                    // 禁止出场动画
                    overridePendingTransition(0, 0);
                }
                break;
        }
    }


    /**
     * 实现按下返回键提示用户再按一次返回桌面，而不是返回上一个页面
     *
     * @param keyCode
     * @param event
     * @return
     */
    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            // 用户按下了返回键
            if (System.currentTimeMillis() - exitTime > 2000) {
                Dialog.toastWithoutAppName(this, "再按一次退出Uav");
                exitTime = System.currentTimeMillis();
            } else {
                // 退出到桌面
                Intent backHome = new Intent(Intent.ACTION_MAIN);
                backHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                backHome.addCategory(Intent.CATEGORY_HOME);
                startActivity(backHome);
                return true;
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 地图点击事件
     *
     * @param latLng 位置信息
     */
    @Override
    public void onMapClick(LatLng latLng) {
        if (clickLatLngList.size() <= 0) {
            Dialog.toastWithoutAppName(this, "请先获取无人机的位置");
            return;
        }
        // 首先在所点击的位置画一个点
        MarkerUtils.drawClickMarker(latLng, aMap);
        clickLatLngList.add(latLng);
        // 将该点与上一个点连起来
        aMap.addPolyline(new PolylineOptions().
                addAll(clickLatLngList).width(2).color(Color.argb(255, 255, 1, 1)));
    }
}
