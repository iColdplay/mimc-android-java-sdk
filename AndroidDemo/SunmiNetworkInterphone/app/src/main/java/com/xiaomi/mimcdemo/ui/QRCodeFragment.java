package com.xiaomi.mimcdemo.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tencent.mmkv.MMKV;
import com.xiaomi.mimcdemo.common.CustomKeys;
import com.xiaomi.mimcdemo.databinding.DesignQrcodeDialogBinding;
import com.xiaomi.mimcdemo.databinding.DialogQrcodeBinding;
import com.xiaomi.mimcdemo.utils.LogUtil;
import com.xiaomi.mimcdemo.utils.QrCodeUtil;

public class QRCodeFragment extends BaseDialogFragment {

    public static final String TAG = QRCodeFragment.class.getSimpleName();

    private static final int width = 250;
    private static final int height = 250;

    private DesignQrcodeDialogBinding binding;

    public static QRCodeFragment newInstance() {
        QRCodeFragment dialog = new QRCodeFragment();
        return dialog;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (binding == null) {
            binding = DesignQrcodeDialogBinding.inflate(inflater, container, false);
            String qrInfo = MainApplication.getInstance().getSerial() + MMKV.defaultMMKV().getString(CustomKeys.KEY_USER_NAME, "默认名称");
            LogUtil.e(TAG, "qrInfo is: " + qrInfo);
            String id = MainApplication.getInstance().getSerial();
            binding.tvMyId.setText("我的ID: " + id);
            binding.imageQrcode.setImageBitmap(QrCodeUtil.createQRCodeBitmap(qrInfo, width, height));
            binding.tvCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }
        return binding.getRoot();
    }

}
