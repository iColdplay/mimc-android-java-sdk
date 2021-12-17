package com.xiaomi.mimcdemo.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.databinding.DataBindingUtil;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;

import com.alibaba.fastjson.JSONObject;
import com.sunmi.scan.Config;
import com.sunmi.scan.Image;
import com.sunmi.scan.ImageScanner;
import com.sunmi.scan.Symbol;
import com.sunmi.scan.SymbolSet;
import com.xiaomi.mimcdemo.R;
import com.xiaomi.mimcdemo.common.CustomKeys;
import com.xiaomi.mimcdemo.databinding.ActivityScanBinding;
import com.xiaomi.mimcdemo.utils.LogUtil;

public class ScanActivity extends Activity implements SurfaceHolder.Callback {

    private static final String TAG = ScanActivity.class.getSimpleName();

    private ActivityScanBinding binding;
    private ImageScanner scanner;
    private Camera mCamera;
    private Handler autoFocusHandler;
    private SurfaceHolder mHolder;
    public static int previewSize_width = 640;
    public static int previewSize_height = 480;
    public boolean use_auto_focus = true;//T1/T2 mini定焦摄像头没有对焦功能,应该改为false
    Image image_data = new Image(previewSize_width, previewSize_height, "Y800");
    StringBuilder sb = new StringBuilder();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNBSBColor(this);
        binding= DataBindingUtil.setContentView(this, R.layout.activity_scan);

        scanner = new ImageScanner();//创建扫描器
        init();

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera = Camera.open();
        } catch (Exception e) {
            mCamera = null;
        }
    }


    private void init() {
        mHolder = binding.surfaceView.getHolder();
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mHolder.addCallback(this);


        scanner = new ImageScanner();//创建扫描器
        scanner.setConfig(Symbol.NONE, Config.ENABLE_MULTILESYMS, 0);//是否开启同一幅图一次解多个条码,0表示只解一个，1为多个,默认0：禁止
        scanner.setConfig(Symbol.QRCODE,Config.ENABLE, 1);//允许识读QR码，默认1:允许
        scanner.setConfig(Symbol.PDF417,Config.ENABLE, 0);//允许识读PDF417码，默认0：禁止
        scanner.setConfig(Symbol.DataMatrix, Config.ENABLE, 0);//允许识读DataMatrix码，默认0：禁止
        scanner.setConfig(Symbol.AZTEC, Config.ENABLE, 0);//允许识读AZTEC码，默认0：禁止
        if(use_auto_focus)
            autoFocusHandler = new Handler();

        binding.scanLine.setAnimation(getAnimation());

        // 展示qr
        binding.imageMyQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent result = new Intent();
                result.putExtra(CustomKeys.KEY_QR_INFO, "my_qr");
                setResult(HomeActivity.ACTIVITY_RESULT_SCAN, result);
                finish();
            }
        });

        // 手动添加
        binding.imageHandAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent result = new Intent();
                result.putExtra(CustomKeys.KEY_QR_INFO, "hand_add");
                setResult(HomeActivity.ACTIVITY_RESULT_SCAN, result);
                finish();
            }
        });
    }

    public TranslateAnimation getAnimation(){
        TranslateAnimation mAnimation = new TranslateAnimation(TranslateAnimation.ABSOLUTE,
                0f,
                TranslateAnimation.ABSOLUTE,0f,
                TranslateAnimation.RELATIVE_TO_PARENT,
                0f,
                TranslateAnimation.RELATIVE_TO_PARENT,
                1.0f);
        mAnimation.setDuration(3000);
        mAnimation.setRepeatCount(-1);
        mAnimation.setRepeatMode(Animation. RESTART);
        mAnimation.setInterpolator(new LinearInterpolator());
        return mAnimation;
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mHolder.getSurface() == null) {
            return;
        }
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
        }
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(previewSize_width, previewSize_height);  //设置预览分辨率
            if (use_auto_focus)
                parameters.setFocusMode(parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            mCamera.setParameters(parameters);
            mCamera.setDisplayOrientation(270);//手持机使用，竖屏显示,T1/T2 mini需要屏蔽掉
            mCamera.setPreviewDisplay(mHolder);
            mCamera.setPreviewCallback(previewCallback);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.d("DBG", "Error starting camera preview: " + e.getMessage());
        }
    }


    /**
     * 预览数据
     */
    Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            image_data.setData(data);
            //解码，返回值为0代表失败，>0表示成功
            int nsyms = scanner.scanImage(image_data);
            if (nsyms != 0) {
                SymbolSet syms = scanner.getResults();//获取解码结果
                //如果允许识读多个条码，则解码结果可能不止一个
                for (Symbol sym : syms) {
                    sb.append(sym.getResult());
                }
                if (!TextUtils.isEmpty(sb.toString())) {
                    Intent result = new Intent();
                    result.putExtra(CustomKeys.KEY_QR_INFO, sb.toString());
                    setResult(HomeActivity.ACTIVITY_RESULT_SCAN, result);
                    finish();
                }
            }
        }
    };

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        if (binding.scanLine.getAnimation() != null) {
            binding.scanLine.getAnimation().cancel();
        }
        super.onDestroy();
    }

    /**
     * 自动对焦回调
     */
    Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            autoFocusHandler.postDelayed(doAutoFocus, 1000);
        }
    };

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (null == mCamera || null == autoFocusCallback) {
                return;
            }
            mCamera.autoFocus(autoFocusCallback);
        }
    };

    /***
     * 修改NavigationBar背景颜色 可自定义颜色
     * */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setNBSBColor(Activity activity) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setNavigationBarColor(activity.getResources().getColor(R.color.transparent));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View decorView = activity.getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            activity.getWindow().setStatusBarColor(getColor(R.color.transparent));
        }
    }

}
