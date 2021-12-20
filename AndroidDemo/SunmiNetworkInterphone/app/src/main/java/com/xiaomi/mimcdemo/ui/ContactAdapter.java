package com.xiaomi.mimcdemo.ui;

import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.xiaomi.mimcdemo.common.CustomKeys;
import com.xiaomi.mimcdemo.common.UserManager;
import com.xiaomi.mimcdemo.database.Contact;
import com.xiaomi.mimcdemo.databinding.DesignContactItemBinding;
import com.xiaomi.mimcdemo.databinding.ItemNameSnBinding;
import com.xiaomi.mimcdemo.utils.AppUtil;
import com.xiaomi.mimcdemo.utils.LogUtil;
import com.xiaomi.mimcdemo.utils.ViewUtil;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private static final String TAG = ContactAdapter.class.getSimpleName();

    private final List<Contact> list;

    private static final long ONLINE_DETECT_TIME_INTERVAL = 1200L;

    public ContactAdapter(List<Contact> list) {
        this.list = list;
    }

    public volatile static boolean shouldShowEditView = false;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        return new ViewHolder(DesignContactItemBinding.inflate(inflater, viewGroup, false));
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
        DesignContactItemBinding binding;

        public ViewHolder(DesignContactItemBinding itemView) {
            super(itemView.getRoot());
            this.binding = itemView;
        }

        void bind(final Contact contact) {
            binding.contactName.setText(contact.getCustomName());
            binding.contactId.setText("ID: " + contact.getSn());
            binding.tvConnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ViewUtil.isFastDoubleClick()) {
                        LogUtil.e(TAG, "this is fast click, just ignore it ");
                        return;
                    }
                    HomeActivity.pongSet.remove(contact.getSn() + contact.getCustomName());
                    UserManager.getInstance().getMIMCUser().sendMessage(contact.getSn() + contact.getCustomName(), "PING".getBytes());
                    boolean ret = false;
                    long start = System.currentTimeMillis();
                    while (System.currentTimeMillis() - start < ONLINE_DETECT_TIME_INTERVAL) {
                        if (HomeActivity.pongSet.contains(contact.getSn() + contact.getCustomName())) {
                            ret = true;
                            break;
                        }
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    // Test!!!!
                    ret = true;

                    if (ret) {
                        LogUtil.e("PINGPONG", "对方在线! PINGNPONG耗时: " + (System.currentTimeMillis() - start) + "ms");

                        binding.exLlDivider.setVisibility(View.VISIBLE);
                        binding.exFlCall.setVisibility(View.VISIBLE);
                        binding.exTvFloatBall.setVisibility(View.VISIBLE);
                        binding.exFloatSwitch.setVisibility(View.VISIBLE);

                        binding.tvConnect.setVisibility(View.GONE);
                        binding.tvDisconnect.setVisibility(View.VISIBLE);

                    } else {
                        LogUtil.e("PINGPONG", "当前对方不在线,请稍后重试");
                        Toast.makeText(AppUtil.getContext(), "当前对方不在线,请稍后重试", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            binding.tvDisconnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    binding.exLlDivider.setVisibility(View.GONE);
                    binding.exFlCall.setVisibility(View.GONE);
                    binding.exTvFloatBall.setVisibility(View.GONE);
                    binding.exFloatSwitch.setVisibility(View.GONE);

                    binding.tvConnect.setVisibility(View.VISIBLE);
                    binding.tvDisconnect.setVisibility(View.GONE);
                }
            });

            if(shouldShowEditView){
                binding.imageUnCheck.setVisibility(View.VISIBLE);
                binding.imageCheck.setVisibility(View.GONE);

                binding.exLlDivider.setVisibility(View.GONE);
                binding.exFlCall.setVisibility(View.GONE);
                binding.exTvFloatBall.setVisibility(View.GONE);
                binding.exFloatSwitch.setVisibility(View.GONE);

                binding.tvConnect.setVisibility(View.GONE);
                binding.tvDisconnect.setVisibility(View.GONE);
            }else {
                binding.imageCheck.setVisibility(View.GONE);
                binding.imageUnCheck.setVisibility(View.GONE);
            }
            binding.imageCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    binding.imageCheck.setVisibility(View.INVISIBLE);
                    binding.imageUnCheck.setVisibility(View.VISIBLE);
                    HomeActivity.contactList.remove(contact.getSn());
                }
            });
            binding.imageUnCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    binding.imageUnCheck.setVisibility(View.INVISIBLE);
                    binding.imageCheck.setVisibility(View.VISIBLE);
                    HomeActivity.contactList.add(contact.getSn());
                }
            });

//            binding.bottomWrapper.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Message message1 = Message.obtain();
//                    message1.what = HomeActivity.MSG_DELETE_CONTACT;
//                    Bundle data = new Bundle();
//                    data.putString(CustomKeys.KEY_SN, contact.getSn());
//                    message1.setData(data);
//                    HomeActivity.mainHandler.sendMessage(message1);
//                }
//            });
//            binding.llSwipeInfo.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//
//                    if (ViewUtil.isFastDoubleClick()) {
//                        LogUtil.e("PINGPONG", "this is fast click, just ignore it ");
//                        return;
//                    }
//
////                    Message message1 = Message.obtain();
////                    message1.what = HomeActivity.MSG_CALL_GOING_OUT;
////                    Bundle data = new Bundle();
////                    data.putString(CustomKeys.KEY_GOING_OUT_NAME, contact.getCustomName());
////                    data.putString(CustomKeys.KEY_GOING_OUT_ID, contact.getSn());
////                    message1.setData(data);
////                    HomeActivity.callHandler.sendMessage(message1);
//
//                    HomeActivity.pongSet.remove(contact.getSn() + contact.getCustomName());
//                    UserManager.getInstance().getMIMCUser().sendMessage(contact.getSn() + contact.getCustomName(), "PING".getBytes());
//                    boolean ret = false;
//                    long start = System.currentTimeMillis();
//                    while (System.currentTimeMillis() - start < ONLINE_DETECT_TIME_INTERVAL) {
//                        if (HomeActivity.pongSet.contains(contact.getSn() + contact.getCustomName())) {
//                            ret = true;
//                            break;
//                        }
//                        try {
//                            Thread.sleep(50);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    if (ret) {
//                        LogUtil.e("PINGPONG", "对方在线! PINGNPONG耗时: " + (System.currentTimeMillis() - start) + "ms");
//                    } else {
//                        LogUtil.e("PINGPONG", "对方不在线!!! 请稍后重试");
//                    }
//
//                }
//            });
            binding.executePendingBindings();
        }
    }
}
