package com.xiaomi.mimcdemo.floatball;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.xiaomi.mimcdemo.databinding.ViewFloatBallBinding;
import com.xiaomi.mimcdemo.utils.LogUtil;

public class PhoneFloatBall extends FloatBallView{

    private static final String TAG = PhoneFloatBall.class.getSimpleName();

    private ViewFloatBallBinding binding;
    private WindowManager.LayoutParams layoutParams;

    private static boolean isAttached = false;

    public PhoneFloatBall(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public PhoneFloatBall(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public PhoneFloatBall(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public Handler uiHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 0){
                LogUtil.e(TAG, "正在说话");
                binding.tvState.setVisibility(VISIBLE);
                binding.tvState.setText("正在说话");
                binding.floatBall.setAlpha(0.5F);
            }
            if(msg.what == 1){
                binding.floatBall.setAlpha(1.0F);
                LogUtil.e(TAG, "不在说话");
                binding.tvState.setVisibility(GONE);
            }
        }
    };

    private void initView(Context context) {
        layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        setLayoutParams(layoutParams);
        binding = ViewFloatBallBinding.inflate(LayoutInflater.from(context), this, true);
        setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

            }
        });
    }

    public void show() {
        if (!isAttachedToWindow() && !isAttached) {
            windowManager.addView(this, layoutParams);
            isAttached = true;
        }
    }

    public void dismiss() {
        if (isAttachedToWindow() && isAttached) {
            windowManager.removeViewImmediate(this);
            isAttached = false;
        }
    }

    public void setFloatBallTalking(){
        uiHandler.removeMessages(0);
        uiHandler.sendEmptyMessage(0);
    }

    public void setFloatBallNotTalking(){
        uiHandler.removeMessages(1);
        uiHandler.sendEmptyMessage(1);
    }

}
