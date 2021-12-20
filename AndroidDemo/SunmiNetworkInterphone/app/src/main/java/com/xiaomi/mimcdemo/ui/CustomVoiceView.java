package com.xiaomi.mimcdemo.ui;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;

import com.xiaomi.mimcdemo.R;

public class CustomVoiceView extends View {

    private static final String TAG = CustomVoiceView.class.getSimpleName();

    private Paint paint = new Paint();
    private int alpha1 = 255;
    private int alpha2 = 170;
    private int alpha3 = 170;

    private int x1 = 0;
    private int x2 = 0;
    private int x3 = 0;

    private boolean x1GoingUp = true;
    private boolean x2GoingUp = true;
    private boolean x3GoingUp = true;

    public CustomVoiceView(Context context) {
        super(context);
        init();
    }

    private Handler animationHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            invalidate();
            animationHandler.sendEmptyMessageDelayed(0, 200);
        }
    };

    public CustomVoiceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomVoiceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CustomVoiceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(getContext().getColor(R.color.yellow));
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(3);
        paint.setAlpha(x1 % alpha1);

        if (x1 + 10 >= alpha1) {
            x1GoingUp = false;
        }
        if (x1 - 10 <= 0) {
            x1GoingUp = true;
        }
        if (x1GoingUp) {
            x1 = x1 + 10;
        } else {
            x1 = x1 - 10;
        }

        canvas.drawCircle(getWidth() / 2, getHeight() / 2, 124, paint);
        paint.setStrokeWidth(2);
        paint.setAlpha(x2 % alpha2);

        if (x2 + 10 >= alpha2) {
            x2GoingUp = false;
        }
        if (x2 - 10 <= 0) {
            x2GoingUp = true;
        }
        if (x2GoingUp) {
            x2 = x2 + 10;
        } else {
            x2 = x2 - 10;
        }

        canvas.drawCircle(getWidth() / 2, getHeight() / 2, 134, paint);
        paint.setStrokeWidth(1);
        paint.setAlpha(x3 % alpha3);

        if (x3 + 10 >= alpha3) {
            x3GoingUp = false;
        }
        if (x3 - 10 <= 0) {
            x3GoingUp = true;
        }
        if(x3GoingUp){
            x3 = x3 + 10;
        }else {
            x3 = x3 - 10;
        }


        canvas.drawCircle(getWidth() / 2, getHeight() / 2, 144, paint);
    }

    public void startAni() {
        animationHandler.sendEmptyMessage(0);
    }

    public void stopAni() {
        animationHandler.removeMessages(0);
    }

}
