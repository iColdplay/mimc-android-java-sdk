package com.xiaomi.mimcdemo.ui;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.xiaomi.mimcdemo.R;
import com.xiaomi.mimcdemo.utils.LogUtil;

import java.util.Objects;

public class BaseDialogFragment extends DialogFragment {
    private static final String TAG = BaseDialogFragment.class.getSimpleName();

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
            window.setLayout(getResources().getDimensionPixelSize(R.dimen.dialog_width), WindowManager.LayoutParams.WRAP_CONTENT);
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

    @Override
    public void onResume() {
        super.onResume();
        Objects.requireNonNull(getView()).setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                LogUtil.e(TAG, "onKey, event:" + event.toString());
                if(event.getAction() == KeyEvent.KEYCODE_BACK){
                    LogUtil.e(TAG, "now back pressed, just dismiss this dialog");
                    BaseDialogFragment.this.dismiss();
                }
                return true;
            }
        });
    }

    protected boolean isInputModel() {
        return false;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        dialog.dismiss();
        LogUtil.e(TAG, "onDismiss()");
    }
}
