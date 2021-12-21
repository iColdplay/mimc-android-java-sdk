package com.xiaomi.mimcdemo.floatball;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class FloatBallView extends FrameLayout {
    public static final String TAG = "FloatBallView";
    protected WindowManager windowManager;
    /**
     * 记录手指按下时在屏幕上的横坐标的值
     */
    private float xDownInScreen = 0.0f;

    /**
     * 记录手指按下时在屏幕上的纵坐标的值
     */
    private float yDownInScreen = 0.0f;
    /**
     * 记录当前手指位置在屏幕上的横坐标值
     */
    private float xInScreen = 0.0f;

    /**
     * 记录当前手指位置在屏幕上的纵坐标值
     */
    private float yInScreen = 0.0f;

    public FloatBallView(@NonNull Context context) {
        this(context, null);
    }

    public FloatBallView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatBallView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xDownInScreen = event.getRawX();
                yDownInScreen = event.getRawY();
                xInScreen = event.getRawX();
                yInScreen = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getRawX() - xInScreen;
                float dy = event.getRawY() - yInScreen;
                xInScreen = event.getRawX();
                yInScreen = event.getRawY();
                if (Math.abs(xDownInScreen - xInScreen) >= 10 || Math.abs(yDownInScreen - yInScreen) >= 10) {
                    updatePosition(dx, dy);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (Math.abs(xDownInScreen - xInScreen) < 10 && Math.abs(yDownInScreen - yInScreen) < 10) {
                    performClick();
                }
                break;
        }
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    /**
     * 更新球形悬浮窗在屏幕中的位置。
     */
    private void updatePosition(float dx, float dy) {
        WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) this.getLayoutParams();
        mParams.x -= dx;
        mParams.y += dy;
        windowManager.updateViewLayout(this, mParams);
    }
}
