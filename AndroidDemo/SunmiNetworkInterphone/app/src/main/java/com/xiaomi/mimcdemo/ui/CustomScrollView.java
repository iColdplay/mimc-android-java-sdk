package com.xiaomi.mimcdemo.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;
import android.widget.Toast;

import com.xiaomi.mimcdemo.manager.SDKUserBehaviorManager;
import com.xiaomi.mimcdemo.utils.AppUtil;
import com.xiaomi.mimcdemo.utils.LogUtil;



public class CustomScrollView extends ScrollView {

    private static final String TAG = CustomVoiceView.class.getSimpleName();

    private Handler mainHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Toast.makeText(AppUtil.getContext(), "请检查网络连接", Toast.LENGTH_SHORT).show();
        }
    };

    public CustomScrollView(Context context) {
        super(context);
    }

    public CustomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(!SDKUserBehaviorManager.getInstance().isOnline()){
            LogUtil.e(TAG, "now not online, please check internet");
            mainHandler.sendEmptyMessage(0);
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }
}
