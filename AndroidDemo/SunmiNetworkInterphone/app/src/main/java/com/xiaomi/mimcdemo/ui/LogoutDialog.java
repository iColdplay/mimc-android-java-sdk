package com.xiaomi.mimcdemo.ui;

import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xiaomi.mimc.MIMCUser;
import com.xiaomi.mimcdemo.common.UserManager;
import com.xiaomi.mimcdemo.databinding.DialogBaseBinding;

public class LogoutDialog extends BaseDialogFragment{

    public static final String TAG = LogoutDialog.class.getSimpleName();
    private DialogBaseBinding binding;

    public static LogoutDialog newInstance(){
        LogoutDialog dialog = new LogoutDialog();
        return dialog;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (binding == null) {
            binding = DialogBaseBinding.inflate(inflater, container, false);
            binding.tvTitle.setText("确定要退出登录么?");
            binding.btnNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            binding.btnOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goLogout();
                    dismiss();
                }
            });
        }
        return binding.getRoot();
    }

    private void goLogout(){
        MIMCUser user = UserManager.getInstance().getMIMCUser();
        if (user != null) {
            user.logout();
            user.destroy();
        }
        Message message1 = Message.obtain();
        message1.what = HomeActivity.MSG_LOGOUT;
        HomeActivity.mainHandler.sendMessage(message1);
    }
}
