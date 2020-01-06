package com.eric.uav.applications.look_album.image_viewer;

import android.annotation.SuppressLint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.eric.uav.R;
import com.eric.uav.applications.look_album.DataTransform;

public class ImageViewerActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView imageView;
    private RelativeLayout linearLayout;
    private Button txtBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        imageView = findViewById(R.id.image_viewer_image);
        imageView.setImageBitmap(DataTransform.imageBitmap);

        linearLayout = findViewById(R.id.image_viewer_wrap);
        linearLayout.setOnClickListener(view -> {
            // 关闭当前的activity
            ImageViewerActivity.this.finish();
            overridePendingTransition(0, 0);
        });

        txtBtn = findViewById(R.id.share_btn);
        txtBtn.setOnClickListener(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            // 用户按下了返回键
            this.finish();
            overridePendingTransition(0, 0);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.share_btn: {
                @SuppressLint("InflateParams") View popupView = getLayoutInflater().inflate(R.layout.share_popup_window, null);
                PopupWindow popupWindow = new PopupWindow(popupView, WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.WRAP_CONTENT);
                // 设置背景图片
                popupWindow.setBackgroundDrawable(new BitmapDrawable());
                // 设置点击一下出现，再点击隐藏的效果
                popupWindow.setFocusable(true);
                popupWindow.setOutsideTouchable(true);
                // 平移动画相对于手机屏幕的底部开始，X轴不变，Y轴从1变0
                TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0,
                        Animation.RELATIVE_TO_PARENT, 1, Animation.RELATIVE_TO_PARENT, 0);
                animation.setInterpolator(new AccelerateInterpolator());
                animation.setDuration(200);
                // 设置popupWindow的显示位置，此处是在手机屏幕底部且水平居中的位置
                popupWindow.showAtLocation(findViewById(R.id.image_viewer_wrap), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                popupView.startAnimation(animation);
                // 设置阴影
                WindowManager.LayoutParams lp = ImageViewerActivity.this.getWindow().getAttributes();
                lp.alpha = 0.3f;
                ImageViewerActivity.this.getWindow().setAttributes(lp);
                popupWindow.setOnDismissListener(() -> {
                    WindowManager.LayoutParams lpDel = ImageViewerActivity.this.getWindow().getAttributes();
                    lpDel.alpha = 1f;
                    ImageViewerActivity.this.getWindow().setAttributes(lpDel);
                });

                popupWindow.showAsDropDown(txtBtn);
            }
            break;
            default:
                break;
        }
    }
}