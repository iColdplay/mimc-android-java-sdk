package com.xiaomi.mimcdemo.ui;

import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaomi.mimcdemo.common.CustomKeys;
import com.xiaomi.mimcdemo.databinding.DesignEditUserDialogBinding;
import com.xiaomi.mimcdemo.manager.SDKUserBehaviorManager;
import com.xiaomi.mimcdemo.utils.AppUtil;

public class EditUserTitleFragment extends BaseDialogFragment{

    public static final String TAG = EditUserTitleFragment.class.getSimpleName();

    private DesignEditUserDialogBinding binding;

    public static EditUserTitleFragment newInstance(){
        return new EditUserTitleFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if(binding == null){
            binding = DesignEditUserDialogBinding.inflate(inflater, container, false);
            binding.tvFinishModify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String name = binding.etUser.getText().toString();
                    if(TextUtils.isEmpty(name) || name.length() > 8){
                        Toast.makeText(AppUtil.getContext(), "ID格式不正确", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    boolean ret = SDKUserBehaviorManager.getInstance().reLoginSDKUser(name);
                    if(ret){
                        Message message1 = HomeActivity.mainHandler.obtainMessage();
                        message1.what = HomeActivity.MSG_NEW_USER_NAME;
                        message1.sendToTarget();
                    }
                    dismiss();
                }
            });
        }
        return binding.getRoot();

    }
}
