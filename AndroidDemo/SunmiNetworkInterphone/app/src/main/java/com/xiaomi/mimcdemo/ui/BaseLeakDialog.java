package com.xiaomi.mimcdemo.ui;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;

import com.xiaomi.mimcdemo.utils.LogUtil;

public class BaseLeakDialog extends Dialog {
    private static final String TAG = BaseLeakDialog.class.getSimpleName();

    private boolean isDismiss = false;

    public BaseLeakDialog(@NonNull Context context) {
        super(context);
    }

    public BaseLeakDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    public void setOnCancelListener(@Nullable OnCancelListener listener) {

    }

    @Override
    public void setOnShowListener(@Nullable OnShowListener listener) {

    }

    @Override
    public void setOnDismissListener(@Nullable OnDismissListener listener) {

    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        LogUtil.e(TAG, "onKeyDown(), event: " + event.toString());
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            LogUtil.e(TAG, "this key event is BACK");
            BaseLeakDialog.this.dismiss();
            isDismiss = true;
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void show() {
        if (isDismiss) {
            LogUtil.e(TAG, "this dialog we should not use it anymore");
            return;
        }
        super.show();
    }
}
