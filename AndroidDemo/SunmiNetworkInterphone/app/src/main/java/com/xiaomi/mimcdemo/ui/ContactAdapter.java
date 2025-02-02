package com.xiaomi.mimcdemo.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.xiaomi.mimc.example.LogUtils;
import com.xiaomi.mimcdemo.common.CustomKeys;
import com.xiaomi.mimcdemo.common.UserManager;
import com.xiaomi.mimcdemo.database.Contact;
import com.xiaomi.mimcdemo.databinding.DesignContactItemBinding;
import com.xiaomi.mimcdemo.databinding.ItemNameSnBinding;
import com.xiaomi.mimcdemo.floatball.PhoneFloatBall;
import com.xiaomi.mimcdemo.manager.AudioEventManager;
import com.xiaomi.mimcdemo.manager.ContactManager;
import com.xiaomi.mimcdemo.manager.SDKUserBehaviorManager;
import com.xiaomi.mimcdemo.service.TalkService;
import com.xiaomi.mimcdemo.utils.AppUtil;
import com.xiaomi.mimcdemo.utils.LogUtil;
import com.xiaomi.mimcdemo.utils.ViewUtil;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private static final String TAG = ContactAdapter.class.getSimpleName();

    private final List<Contact> list;

    private static final long ONLINE_DETECT_TIME_INTERVAL = 7 * 1000L;

    public ContactAdapter(List<Contact> list) {
        this.list = list;
    }

    public volatile static boolean shouldShowEditView = false;

    public volatile static boolean isAnythingInConnection = false;

    public List<Contact> getList() {
        return list;
    }

    private static PhoneFloatBall floatBall = new PhoneFloatBall(AppUtil.getContext());

    private static Handler floatBallHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                floatBall.show();
            }
            if (msg.what == 1) {
                floatBall.dismiss();
            }
            if (msg.what == 2) {
                floatBall.setFloatBallTalking();
            }
            if (msg.what == 3) {
                floatBall.setFloatBallNotTalking();
            }
        }
    };

    public static void showFloatBall() {
        floatBallHandler.sendEmptyMessage(0);
    }

    public static void dismissFloatBall() {
        floatBallHandler.sendEmptyMessage(1);
    }

    public static void setFloatBallTalking() {
        floatBallHandler.sendEmptyMessage(2);
    }

    public static void setFloatBallNotTalking() {
        floatBallHandler.sendEmptyMessage(3);
    }

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

    public void swapItem(int fromPosition, int toPosition) {
        Collections.swap(list, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void missCallHappened(int fromPosition) {
        if (isAnythingInConnection) {
            Collections.swap(list, fromPosition, 1);
            notifyItemMoved(fromPosition, 1);
        } else {
            Collections.swap(list, fromPosition, 0);
            notifyItemMoved(fromPosition, 0);
        }
    }

    @Override
    public int getItemCount() {
        return this.list == null ? 0 : this.list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        DesignContactItemBinding binding;
        Contact contact;

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        Handler uiHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (binding != null) {
                    if (msg.what == 0) {
                        swapItem(getAdapterPosition(), 0);
                        binding.exLlDivider.setVisibility(View.VISIBLE);
                        binding.exFlCall.setVisibility(View.VISIBLE);
                        binding.exTvFloatBall.setVisibility(View.VISIBLE);
                        binding.exFloatSwitch.setVisibility(View.VISIBLE);
                        binding.tvConnect.setVisibility(View.GONE);
                        binding.tvDisconnect.setVisibility(View.VISIBLE);

                        isAnythingInConnection = true;
                        TalkService.contactName = contact.getCustomName();
                        TalkService.contactSN = contact.getSn();
                    }
                    if (msg.what == 1) {
                        Toast.makeText(AppUtil.getContext(), "当前对方不在线,请稍后重试", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        private static final int MSG_GO_SPEAK = 1001;
        private static final int MSG_STOP_SPEAK = 1002;

        Handler longTouchHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == MSG_GO_SPEAK) {
                    binding.customVoiceView.setVisibility(View.VISIBLE);
                    binding.customVoiceView.startAni();
                    binding.tvUnderMic.setText("正在对讲");
                    MainApplication.getInstance().vibrate();

                    AudioEventManager.getInstance().sendMessageCallOutgoing(contact.getCustomName(), contact.getSn());
                }

                if (msg.what == MSG_STOP_SPEAK) {
                    binding.customVoiceView.stopAni();
                    binding.customVoiceView.setVisibility(View.INVISIBLE);
                    binding.tvUnderMic.setText("按住对讲");
                    isLongTouchHandled = false;
                    MainApplication.getInstance().vibrate();

                    AudioEventManager.getInstance().sendMessageToStopCall();
                }
            }
        };

        Handler missCallHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                binding.tvMissCall.setVisibility(View.GONE);
            }
        };

        private void send100msToActivateSpeak(String name, String sn) {
            Message message1 = longTouchHandler.obtainMessage();
            Bundle data = new Bundle();
            data.putString("name", name);
            data.putString("sn", sn);
            message1.setData(data);
            message1.what = MSG_GO_SPEAK;
            longTouchHandler.sendMessageDelayed(message1, 500);
        }

        private void cancelActivateSpeak() {
            longTouchHandler.removeMessages(MSG_GO_SPEAK);
        }

        private void sendMessageStopSpeak() {
            Message message1 = longTouchHandler.obtainMessage();
            message1.what = MSG_STOP_SPEAK;
            longTouchHandler.sendMessage(message1);


        }

        public ViewHolder(DesignContactItemBinding itemView) {
            super(itemView.getRoot());
            this.binding = itemView;
        }

        volatile boolean isLongTouchHandled = false;

        private final View.OnTouchListener touchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        send100msToActivateSpeak(contact.getCustomName(), contact.getSn());
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        cancelActivateSpeak();
                        sendMessageStopSpeak();
                        break;
                }
                return true;
            }
        };

        @SuppressLint("ClickableViewAccessibility")
        void bind(final Contact contact) {
            this.contact = contact;
            int missCall = contact.getMissCall();
            if (missCall > 0) {
                binding.tvMissCall.setVisibility(View.VISIBLE);
                binding.tvMissCall.setText("占线未接通: " + String.valueOf(missCall));
            } else {
                binding.tvMissCall.setVisibility(View.GONE);
            }
            binding.imageNormalBack.setOnTouchListener(touchListener);
            binding.contactName.setText(contact.getCustomName());
            binding.contactId.setText("ID: " + contact.getSn());
            binding.tvConnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isAnythingInConnection) {
                        LogUtil.e(TAG, "something is in connection, so we just ignore any other connection");
                        return;
                    }
                    if (ViewUtil.isFastDoubleClick()) {
                        LogUtil.e(TAG, "this is fast click, just ignore it ");
                        return;
                    }

                    // 足够条件触发连接, 则清除掉当前联系人的miss call数据
                    boolean clearRet = ContactManager.getInstance().updateMissCallTo0(contact.getCustomName(), contact.getSn());
                    if (clearRet) {
                        LogUtil.e(TAG, "miss call数据已清楚");
                    }
                    // 关闭miss call数据的显示
                    missCallHandler.sendEmptyMessage(0);

                    final boolean[] ret = {false};
                    final long start = System.currentTimeMillis();
                    final Message message1 = HomeActivity.mainHandler.obtainMessage();
                    message1.what = HomeActivity.MSG_SHOW_LOADING;
                    message1.sendToTarget();
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            HomeActivity.pongSet.remove(contact.getSn() + contact.getCustomName());
                            UserManager.getInstance().getMIMCUser().sendMessage(contact.getSn() + contact.getCustomName(), "PING".getBytes());

                            while (System.currentTimeMillis() - start < ONLINE_DETECT_TIME_INTERVAL) {
                                if (HomeActivity.pongSet.contains(contact.getSn() + contact.getCustomName())) {
                                    ret[0] = true;
                                    break;
                                }
                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            Message message2 = HomeActivity.mainHandler.obtainMessage();
                            message2.what = HomeActivity.MSG_HIDE_LOADING;
                            HomeActivity.mainHandler.sendMessage(message2);

                            if (ret[0]) {
                                LogUtil.e("PINGPONG", "对方在线! PINGNPONG耗时: " + (System.currentTimeMillis() - start) + "ms");
//                                binding.exLlDivider.setVisibility(View.VISIBLE);
//                                binding.exFlCall.setVisibility(View.VISIBLE);
//                                binding.exTvFloatBall.setVisibility(View.VISIBLE);
//                                binding.exFloatSwitch.setVisibility(View.VISIBLE);
//                                binding.tvConnect.setVisibility(View.GONE);
//                                binding.tvDisconnect.setVisibility(View.VISIBLE);
                                Message message11 = uiHandler.obtainMessage();
                                message11.what = 0;
                                uiHandler.sendMessage(message11);
                            } else {
                                LogUtil.e("PINGPONG", "当前对方不在线,请稍后重试");
                                Message message11 = uiHandler.obtainMessage();
                                message11.what = 1;
                                uiHandler.sendMessage(message11);
                            }
                        }
                    };
                    executorService.execute(runnable);
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

                    isAnythingInConnection = false;
                    TalkService.contactName = null;
                    TalkService.contactSN = null;
                    floatBall.dismiss();
                }
            });

            if (shouldShowEditView) {
                binding.imageUnCheck.setVisibility(View.VISIBLE);
                binding.imageCheck.setVisibility(View.GONE);

                binding.exLlDivider.setVisibility(View.GONE);
                binding.exFlCall.setVisibility(View.GONE);
                binding.exTvFloatBall.setVisibility(View.GONE);
                binding.exFloatSwitch.setVisibility(View.GONE);

                binding.tvConnect.setVisibility(View.GONE);
                binding.tvDisconnect.setVisibility(View.GONE);
            } else {
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

            // 悬浮球
            binding.exFloatSwitch.setChecked(false);
            binding.exFloatSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        floatBall.show();
                    } else {
                        floatBall.dismiss();
                    }
                }
            });

            binding.executePendingBindings();
        }
    }
}
