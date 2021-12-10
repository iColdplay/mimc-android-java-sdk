package com.xiaomi.mimcdemo.ui;

import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xiaomi.mimcdemo.common.CustomKeys;
import com.xiaomi.mimcdemo.common.UserManager;
import com.xiaomi.mimcdemo.database.Contact;
import com.xiaomi.mimcdemo.databinding.ItemNameSnBinding;
import com.xiaomi.mimcdemo.utils.LogUtil;
import com.xiaomi.mimcdemo.utils.ViewUtil;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder>{

    private final List<Contact> list;

    public ContactAdapter(List<Contact> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        return new ViewHolder(ItemNameSnBinding.inflate(inflater, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        viewHolder.bind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return this.list == null ? 0 : this.list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemNameSnBinding binding;
        boolean isOnClickRunning = false;

        public ViewHolder(ItemNameSnBinding itemView) {
            super(itemView.getRoot());
            this.binding = itemView;
        }

        void bind(final Contact contact) {
            binding.tvCustomName.setText(contact.getCustomName());
            binding.tvSn.setText(contact.getSn());
            binding.bottomWrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Message message1 = Message.obtain();
                    message1.what = HomeActivity.MSG_DELETE_CONTACT;
                    Bundle data = new Bundle();
                    data.putString(CustomKeys.KEY_SN, contact.getSn());
                    message1.setData(data);
                    HomeActivity.mainHandler.sendMessage(message1);
                }
            });
            binding.llSwipeInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    if(ViewUtil.isFastDoubleClick()){
                        LogUtil.e("PINGPONG", "this is fast click, just ignore it ");
                        return;
                    }

//                    Message message1 = Message.obtain();
//                    message1.what = HomeActivity.MSG_CALL_GOING_OUT;
//                    Bundle data = new Bundle();
//                    data.putString(CustomKeys.KEY_GOING_OUT_NAME, contact.getCustomName());
//                    data.putString(CustomKeys.KEY_GOING_OUT_ID, contact.getSn());
//                    message1.setData(data);
//                    HomeActivity.callHandler.sendMessage(message1);

                    HomeActivity.pongSet.remove(contact.getSn() + contact.getCustomName());
                    UserManager.getInstance().getMIMCUser().sendMessage(contact.getSn() + contact.getCustomName(), "PING".getBytes());
                    boolean ret = false;
                    long start = System.currentTimeMillis();
                    while (System.currentTimeMillis() - start < 1200){
                        if(HomeActivity.pongSet.contains(contact.getSn()+contact.getCustomName())){
                            ret = true;
                            break;
                        }
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if(ret){
                        LogUtil.e("PINGPONG", "对方在线! PINGNPONG耗时: " + (System.currentTimeMillis() - start) + "ms");
                    }else {
                        LogUtil.e("PINGPONG", "对方不在线!!! 请稍后重试");
                    }

                }
            });
            binding.executePendingBindings();
        }
    }
}
