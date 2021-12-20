package com.xiaomi.mimcdemo.ui;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class BaseLeakDialog extends Dialog {
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

}
