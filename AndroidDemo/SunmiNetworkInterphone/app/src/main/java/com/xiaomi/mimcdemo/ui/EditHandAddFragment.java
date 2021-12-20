package com.xiaomi.mimcdemo.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.xiaomi.mimcdemo.common.CustomKeys;
import com.xiaomi.mimcdemo.databinding.DesignEditHandAddDialogBinding;
import com.xiaomi.mimcdemo.utils.AppUtil;

public class EditHandAddFragment extends BaseDialogFragment {

    public static final String TAG = EditHandAddFragment.class.getSimpleName();

    private DesignEditHandAddDialogBinding binding;

    public static EditHandAddFragment newInstance() {
        return new EditHandAddFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if (binding == null) {
            binding = DesignEditHandAddDialogBinding.inflate(inflater, container, false);
            binding.tvCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            binding.tvConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String name = binding.etUserName.getText().toString();
                    if (TextUtils.isEmpty(name) || name.length() > 8) {
                        Toast.makeText(AppUtil.getContext(), "昵称格式不正确", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String id = binding.etUserId.getText().toString();
                    if (TextUtils.isEmpty(id) || id.length() != 16) {
                        Toast.makeText(AppUtil.getContext(), "ID格式不正确", Toast.LENGTH_SHORT).show();

                    }
                    Message message1 = HomeActivity.mainHandler.obtainMessage();
                    message1.what = HomeActivity.MSG_ADD_CONTACT;
                    Bundle data = new Bundle();
                    data.putString(CustomKeys.KEY_USER_NAME, name);
                    data.putString(CustomKeys.KEY_SN, id);
                    message1.setData(data);
                    message1.sendToTarget();
                    dismiss();
                }
            });
        }
        return binding.getRoot();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }
}
