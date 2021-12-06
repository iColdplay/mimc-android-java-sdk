package com.xiaomi.mimcdemo.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tencent.mmkv.MMKV;
import com.xiaomi.mimcdemo.common.CustomKeys;
import com.xiaomi.mimcdemo.databinding.DialogQrcodeBinding;
import com.xiaomi.mimcdemo.utils.LogUtil;
import com.xiaomi.mimcdemo.utils.QrCodeUtil;

public class ChannelQRCodeFragment extends BaseDialogFragment{

    public static final String TAG = ChannelQRCodeFragment.class.getSimpleName();

    private static final int width = 250;
    private static final int height = 250;

    private DialogQrcodeBinding binding;

    private static String qrInfo;

    public static ChannelQRCodeFragment newInstance(String info){
        qrInfo = info;
        ChannelQRCodeFragment fragment = new ChannelQRCodeFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (binding == null) {
            LogUtil.e(TAG, "qrInfo is: " + qrInfo);
            binding = DialogQrcodeBinding.inflate(inflater, container, false);
            binding.imageQrcode.setImageBitmap(QrCodeUtil.createQRCodeBitmap(qrInfo, width, height));
            binding.btnOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }
        return binding.getRoot();
    }

}
