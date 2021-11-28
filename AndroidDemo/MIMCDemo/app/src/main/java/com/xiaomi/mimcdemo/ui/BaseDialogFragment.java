package com.xiaomi.mimcdemo.ui;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.xiaomi.mimcdemo.R;

public class BaseDialogFragment extends DialogFragment {
    protected Window window;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Dialog);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().setCanceledOnTouchOutside(false);
        window = getDialog().getWindow();
        if (window != null) {
            window.setLayout(getResources().getDimensionPixelSize(R.dimen.dialog_width),
                    WindowManager.LayoutParams.WRAP_CONTENT);
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 20210609-043224 Bojack add for Dialog keep screen on
            if (!isInputModel()) {
                window.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            }
        }
    }

    // 20210623-073749 bojack solve DialogFragment memory leak problem
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new BaseLeakDialog(getContext(), getTheme());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (window != null && !isInputModel()) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        }
    }

    protected boolean isInputModel() {
        return false;
    }
}
