package com.xiaomi.mimcdemo.ui;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaomi.mimc.logger.Log;
import com.xiaomi.mimcdemo.R;
import com.xiaomi.mimcdemo.common.CustomKeys;
import com.xiaomi.mimcdemo.databinding.ActivityEditContactBinding;
import com.xiaomi.mimcdemo.utils.LogUtil;

public class AddContactActivity extends Activity {

    private static final String TAG = AddContactActivity.class.getSimpleName();

    private ActivityEditContactBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_contact);

        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveContact();
            }
        });
    }

    private void saveContact() {
        String name = binding.edtName.getText().toString();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(AddContactActivity.this, "请输入名称", Toast.LENGTH_SHORT).show();
            return;
        }
        String sn = binding.edtNo.getText().toString();
        if (TextUtils.isEmpty(sn)) {
            Toast.makeText(AddContactActivity.this, "请输入ID", Toast.LENGTH_SHORT).show();
            return;
        }

        LogUtil.e(TAG, "name: " + name);
        LogUtil.e(TAG, "sn: " + sn);
        if (sn.length() != 16) {
            Toast.makeText(AddContactActivity.this, "ID应当为16个字符", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(CustomKeys.KEY_USER_NAME, name);
        intent.putExtra(CustomKeys.KEY_SN, sn);
        setResult(HomeActivity.ACTIVITY_RESULT_ADD_CONTACT, intent);
        finish();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
