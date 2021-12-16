package com.xiaomi.mimcdemo.ui;

import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xiaomi.mimcdemo.databinding.DialogCallingBinding;

public class CallingDialog extends BaseDialogFragment {

    public static final String TAG = CallingDialog.class.getSimpleName();

    private DialogCallingBinding binding;

    private static CallingDialog dialog;

    public static CallingDialog getInstance() {
        if (dialog == null) {
            dialog = new CallingDialog();
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if (binding == null) {
            binding = DialogCallingBinding.inflate(inflater, container, false);
            binding.btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    close();
                }
            });
        }
        return binding.getRoot();
    }

    private void close() {
        dismiss();
        // tell HomeActivity call is over
        Message message1 = Message.obtain();
//        message1.what = HomeActivity.MSG_CALL_CLOSE_BY_HOST;
//        HomeActivity.callHandler.sendMessage(message1);

        binding = null;
    }

    public void performClickCloseButton(){
        if(binding == null){
            return;
        }
        binding.btnClose.performClick();
        binding = null;
    }

}
