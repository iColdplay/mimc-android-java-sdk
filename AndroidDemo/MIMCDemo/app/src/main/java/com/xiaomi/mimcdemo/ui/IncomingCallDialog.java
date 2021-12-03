package com.xiaomi.mimcdemo.ui;

import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xiaomi.mimcdemo.databinding.DialogCallingBinding;
import com.xiaomi.mimcdemo.databinding.DialogIncomingCallBinding;

public class IncomingCallDialog extends BaseDialogFragment{

    public static final String TAG = IncomingCallDialog.class.getSimpleName();

    private DialogIncomingCallBinding binding;

    private static IncomingCallDialog dialog;

    public static IncomingCallDialog getInstance(){
        if(dialog == null){
            dialog = new IncomingCallDialog();
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if (binding == null) {
            binding = DialogIncomingCallBinding.inflate(inflater, container, false);
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
        message1.what = HomeActivity.MSG_CALL_CLOSE_BY_CLIENT;
        HomeActivity.callHandler.sendMessage(message1);

        binding = null;
    }


    public void performClickCloseButton(){
        if(binding != null) {
            binding.btnClose.performClick();
            binding = null;
        }
    }

}
