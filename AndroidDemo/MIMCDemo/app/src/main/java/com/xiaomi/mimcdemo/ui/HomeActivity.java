package com.xiaomi.mimcdemo.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.tencent.mmkv.MMKV;
import com.xiaomi.mimc.MIMCGroupMessage;
import com.xiaomi.mimc.MIMCMessage;
import com.xiaomi.mimc.MIMCOnlineMessageAck;
import com.xiaomi.mimc.MIMCServerAck;
import com.xiaomi.mimc.MIMCUser;
import com.xiaomi.mimc.common.MIMCConstant;
import com.xiaomi.mimc.data.RtsDataType;
import com.xiaomi.mimcdemo.R;
import com.xiaomi.mimcdemo.av.AudioPlayer;
import com.xiaomi.mimcdemo.av.AudioRecorder;
import com.xiaomi.mimcdemo.av.FFmpegAudioDecoder;
import com.xiaomi.mimcdemo.av.FFmpegAudioEncoder;
import com.xiaomi.mimcdemo.bean.Audio;
import com.xiaomi.mimcdemo.bean.ChatMsg;
import com.xiaomi.mimcdemo.common.CustomKeys;
import com.xiaomi.mimcdemo.common.NetWorkUtils;
import com.xiaomi.mimcdemo.common.UserManager;
import com.xiaomi.mimcdemo.database.Contact;
import com.xiaomi.mimcdemo.databinding.ActivityHomeBinding;
import com.xiaomi.mimcdemo.listener.OnAudioCapturedListener;
import com.xiaomi.mimcdemo.listener.OnAudioDecodedListener;
import com.xiaomi.mimcdemo.listener.OnAudioEncodedListener;
import com.xiaomi.mimcdemo.listener.OnCallStateListener;
import com.xiaomi.mimcdemo.proto.AV;
import com.xiaomi.mimcdemo.utils.LogUtil;
import com.xiaomi.mimcdemo.utils.PingPongAutoReplier;
import com.xiaomi.mimcdemo.utils.ViewUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.SynchronousQueue;

public class HomeActivity extends Activity {

    private static final String TAG = HomeActivity.class.getSimpleName();

    public static final int ACTIVITY_RESULT_SCAN = 10001;
    public static final int ACTIVITY_RESULT_ADD_CONTACT = 10002;

    private ActivityHomeBinding binding;

    private UserManager userManager;

    private MMKV mmkv;

    private long exitTime;

    public static Handler mainHandler;
    public static final int MSG_LOGOUT = 1001;
    public static final int MSG_ADD_CONTACT = 1002;
    public static final int MSG_DELETE_CONTACT = 1003;

    private ContactAdapter contactAdapter;

    private final UserManager.OnHandleMIMCMsgListener onHandleMIMCMsgListener = new UserManager.OnHandleMIMCMsgListener() {

        @Override
        public void onHandleMessage(ChatMsg chatMsg) {
            LogUtil.e(TAG, "HomeActivity onHandleMIMCMsgListener onHandleMessage()");
            PingPongAutoReplier.getInstance().executePingPongRunnable(chatMsg);
        }

        @Override
        public void onHandleGroupMessage(ChatMsg chatMsg) {

        }

        @Override
        public void onHandleStatusChanged(MIMCConstant.OnlineStatus status) {
            LogUtil.e(TAG, "onHandleStatusChanged() invoked");
            LogUtil.e(TAG, status.toString());
        }

        @Override
        public void onHandleServerAck(MIMCServerAck serverAck) {

        }

        @Override
        public void onHandleOnlineMessageAck(MIMCOnlineMessageAck onlineMessageAck) {

        }

        @Override
        public void onHandleCreateGroup(String json, boolean isSuccess) {

        }

        @Override
        public void onHandleQueryGroupInfo(String json, boolean isSuccess) {

        }

        @Override
        public void onHandleQueryGroupsOfAccount(String json, boolean isSuccess) {

        }

        @Override
        public void onHandleJoinGroup(String json, boolean isSuccess) {

        }

        @Override
        public void onHandleQuitGroup(String json, boolean isSuccess) {

        }

        @Override
        public void onHandleKickGroup(String json, boolean isSuccess) {

        }

        @Override
        public void onHandleUpdateGroup(String json, boolean isSuccess) {

        }

        @Override
        public void onHandleDismissGroup(String json, boolean isSuccess) {

        }

        @Override
        public void onHandlePullP2PHistory(String json, boolean isSuccess) {

        }

        @Override
        public void onHandlePullP2THistory(String json, boolean isSuccess) {

        }

        @Override
        public void onHandleSendMessageTimeout(MIMCMessage message) {

        }

        @Override
        public void onHandleSendGroupMessageTimeout(MIMCGroupMessage groupMessage) {

        }

        @Override
        public void onHandleJoinUnlimitedGroup(long topicId, int code, String errMsg) {

        }

        @Override
        public void onHandleQuitUnlimitedGroup(long topicId, int code, String errMsg) {

        }

        @Override
        public void onHandleDismissUnlimitedGroup(String json, boolean isSuccess) {

        }

        @Override
        public void onHandleQueryUnlimitedGroupMembers(String json, boolean isSuccess) {

        }

        @Override
        public void onHandleQueryUnlimitedGroups(String json, boolean isSuccess) {

        }

        @Override
        public void onHandleQueryUnlimitedGroupOnlineUsers(String json, boolean isSuccess) {

        }

        @Override
        public void onPullNotification() {

        }
    };

    public static final int MSG_FINISH_DELAY_MS = 1000;
    public static final int MSG_CALL_INCOMING = 2001;
    public static final int MSG_FINISH = 2002;
    public static final int MSG_CALL_GOING_OUT = 2003;
    public static final int MSG_CALL_CLOSE_BY_HOST = 2004;
    public static final int MSG_CALL_CLOSE_BY_CLIENT = 2005;
    public static Handler callHandler;
    private volatile long callingOutID = -1;

    private AudioRecorder audioRecorder;
    private AudioPlayer audioPlayer;
    private FFmpegAudioEncoder audioEncoder;
    private FFmpegAudioDecoder audioDecoder;

    AudioManager audioManager;
    private BlockingQueue<Audio> audioEncodeQueue;
    private AudioEncodeThread audioEncodeThread;
    private BlockingQueue<AV.MIMCRtsPacket> audioDecodeQueue;
    private AudioDecodeThread audioDecodeThread;

    private volatile boolean nowReceivingVoice = false; //用来控制录制网络数据

    public static String callerName = "未知";

//    public static HandlerThread htOnlineDetector = new HandlerThread("online-detector");
//    public static Handler handlerOnlineDetector;

    public static Collection pongSet;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.e(TAG, "onCreate()");

        setNavigationBarColor(this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);

        audioStuffPrepare();
        // 设置处理MIMC消息监听器
        UserManager.getInstance().setHandleMIMCMsgListener(onHandleMIMCMsgListener);

        userManager = UserManager.getInstance();

        uiGoInitializingLayout();

        if (NetWorkUtils.isNetwork(this)) {
            uiGoLoginLayout();
        } else {
            Toast.makeText(this, "未检测到网络, 请连接网络后重试...", Toast.LENGTH_SHORT).show();
            Thread gonnaFinishThread = new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        Thread.sleep(3 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    finish();
                }
            };
            gonnaFinishThread.start();
        }

        requestPermissions(new String[]{"android.permission.RECORD_AUDIO",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.MODIFY_AUDIO_SETTINGS",
                "android.permission.READ_PHONE_STATE",
                "android.permission.READ_PRIVILEGED_PHONE_STATE",
                "android.permission.CAMERA",
                "android.permission.MODIFY_AUDIO_SETTINGS"}, 0);

        mmkv = MMKV.defaultMMKV();
        String name = mmkv.getString(CustomKeys.KEY_USER_NAME, "");
        assert name != null;
        if (name.length() > 0 && !TextUtils.isEmpty(name)) {
            binding.et.setText(name);
            binding.et.requestFocus();
            binding.et.setSelection(binding.et.getText().toString().length());
        } else {
            binding.et.setText("");
            binding.et.setHint("Please Input ID");
        }

        binding.tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogin();
            }
        });

        mainHandler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == MSG_LOGOUT) {
                    LogUtil.e(TAG, "HomeActivity logout");
                    uiGoLoginLayout();
                }

                if (msg.what == MSG_ADD_CONTACT) {
                    LogUtil.e(TAG, "HomeActivity add contact");
                    uiRefreshContact();
                }

                if (msg.what == MSG_DELETE_CONTACT) {
                    LogUtil.e(TAG, "HomeActivity delete contact");
                    Bundle data = msg.getData();
                    boolean ret = MIMCApplication.getInstance().deleteDataBySN(data.getString(CustomKeys.KEY_SN));
                    if (ret) {
                        Toast.makeText(HomeActivity.this, "删除联系人成功", Toast.LENGTH_SHORT).show();
                    }
                    uiRefreshContact();
                }
            }
        };

        binding.imageQrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.e(TAG, "imageQrcode onClick()");
                doClickQrCodeImage();
            }
        });

        binding.imageScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.e(TAG, "imageScan onClick()");
                doClickScanImage();
            }
        });

        binding.tvNoContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.e(TAG, "add Contact");
                Intent intent = new Intent(HomeActivity.this, AddContactActivity.class);
                startActivityForResult(intent, ACTIVITY_RESULT_ADD_CONTACT);
            }
        });

        binding.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.e(TAG, "add Contact by add icon");
                Intent intent = new Intent(HomeActivity.this, AddContactActivity.class);
                startActivityForResult(intent, ACTIVITY_RESULT_ADD_CONTACT);
            }
        });

        callHandler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == MSG_CALL_INCOMING) {
                    LogUtil.e(TAG, "----------MSG_CALL_INCOMING handle start---------");
                    Bundle data = msg.getData();
                    String fromAccount = data.getString(CustomKeys.KEY_INCOMING_CALL_ACCOUNT);
                    long callId = data.getLong(CustomKeys.KEY_INCOMING_CALL_ID);
                    LogUtil.e(TAG, "fromAccount: " + fromAccount);
                    LogUtil.e(TAG, "callId: " + callId);
                    LogUtil.e(TAG, "we are gonna directly answer the call");

                    callingOutID = callId;
                    assert fromAccount != null;
                    callerName = fromAccount.substring(16);

                    showIncomingCallDialog();

                    UserManager.getInstance().answerCall();
//                    startRecording();
                    audioPlayer.start();

                    nowReceivingVoice = true;

                    Toast.makeText(HomeActivity.this, "接通语音", Toast.LENGTH_SHORT).show();

                    LogUtil.e(TAG, "----------MSG_CALL_INCOMING handle end  ---------");
                }

                if (msg.what == MSG_FINISH) {
                    LogUtil.e(TAG, "----------MSG_FINISH handle start----------");
                    Bundle data = msg.getData();
                    String errorMessage = data.getString("msg");
                    Toast.makeText(HomeActivity.this, "通讯结束 " + errorMessage, Toast.LENGTH_SHORT).show();

                    if (audioPlayer != null) {
                        audioPlayer.stop();
                        nowReceivingVoice = false;
                    }
                    if (audioRecorder != null) {
                        audioRecorder.stop();
                    }

                    IncomingCallDialog.getInstance().performClickCloseButton();
                    CallingDialog.getInstance().performClickCloseButton();
                    LogUtil.e(TAG, "----------MSG_FINISH handle end  ----------");
                }

                if (msg.what == MSG_CALL_GOING_OUT) {
                    LogUtil.e(TAG, "----------MSG_CALL_GOING_OUT handle start----------");

                    doClickOnRecyclerView();

                    Bundle data = msg.getData();
                    String goingOutName = data.getString(CustomKeys.KEY_GOING_OUT_NAME);
                    String goingOutId = data.getString(CustomKeys.KEY_GOING_OUT_ID);
                    String targetCountName = goingOutId + goingOutName;
                    LogUtil.e(TAG, "targetCountName: " + targetCountName);
                    callingOutID = UserManager.getInstance().dialCall(targetCountName, null, "AUDIO".getBytes());
                    if (callingOutID == -1) {
                        toast("拨号失败, 请检查网络");
                        CallingDialog.getInstance().performClickCloseButton();
                    }
                    LogUtil.e(TAG, "----------MSG_CALL_GOING_OUT handle end  ----------");
                }

                if (msg.what == MSG_CALL_CLOSE_BY_HOST) {
                    LogUtil.e(TAG, "----------MSG_CALL_CLOSE_BY_HOST handle start----------");

                    UserManager.getInstance().closeCall(callingOutID);
                    Toast.makeText(HomeActivity.this, "主动呼叫结束", Toast.LENGTH_SHORT).show();

                    if (audioPlayer != null) {
                        audioPlayer.stop();
                    }
                    if (audioRecorder != null) {
                        audioRecorder.stop();
                    }
                    LogUtil.e(TAG, "----------MSG_CALL_CLOSE_BY_HOST handle end  ----------");
                }

                if (msg.what == MSG_CALL_CLOSE_BY_CLIENT) {
                    LogUtil.e(TAG, "----------MSG_CALL_CLOSE_BY_CLIENT handle start----------");

                    UserManager.getInstance().closeCall(callingOutID);
                    callingOutID = -1;
                    Toast.makeText(HomeActivity.this, "通讯结束 ", Toast.LENGTH_SHORT).show();

                    if (audioPlayer != null) {
                        audioPlayer.stop();
                        nowReceivingVoice = false;
                    }
                    if (audioRecorder != null) {
                        audioRecorder.stop();
                    }

                    LogUtil.e(TAG, "----------MSG_CALL_CLOSE_BY_CLIENT handle start----------");
                }
            }
        };

        Set<String> pongs = new HashSet<>();
        pongSet = Collections.synchronizedSet(pongs);

        binding.tvDeviceId.setText(MIMCApplication.getInstance().getSerial());
//        htOnlineDetector.start();
//        handlerOnlineDetector = new Handler(htOnlineDetector.getLooper()){
//            @Override
//            public void handleMessage(Message msg) {
//                super.handleMessage(msg);
//                Bundle data = msg.getData();
//                String fromAccount = data.getString("fromAccount");
//            }
//        };
    }

    private void uiRefreshContact() {
        LogUtil.e(TAG, "now ui refresh contact");

        // 获取所有的联系人信息
        List<Contact> contacts = MIMCApplication.getInstance().queryData();
        if (contacts == null || contacts.size() == 0) {
            LogUtil.e(TAG, "no contact");
            binding.tvNoContact.setVisibility(View.VISIBLE);
            binding.rvContactList.setVisibility(View.GONE);
            return;
        }

        contactAdapter = new ContactAdapter(contacts);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(HomeActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        binding.rvContactList.setLayoutManager(linearLayoutManager);
        binding.rvContactList.setAdapter(contactAdapter);
        binding.tvNoContact.setVisibility(View.GONE);
        binding.rvContactList.setVisibility(View.VISIBLE);

        binding.llContactList.invalidate();

    }

    private void doClickScanImage() {
        Intent intent = new Intent(HomeActivity.this, ScanActivity.class);
        startActivityForResult(intent, ACTIVITY_RESULT_SCAN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_RESULT_SCAN) {
            if (data != null) {
                String qrInfo = data.getStringExtra(CustomKeys.KEY_QR_INFO);
                LogUtil.e(TAG, "qrInfo is: " + qrInfo);
                String sn = qrInfo.substring(0, 16);
                String customName = qrInfo.replaceAll(sn, "");
                LogUtil.e(TAG, "SN is: " + sn);
                LogUtil.e(TAG, "Custom Name: " + customName);

                if (sn.contains(MIMCApplication.getInstance().getSerial())) {
                    LogUtil.e(TAG, "detect it's own id, refuse!");
                    Toast.makeText(HomeActivity.this, "无法添加本机为联系人", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean result = MIMCApplication.getInstance().insertData(customName, sn);
                if (result) {
                    Toast.makeText(HomeActivity.this, "添加联系人成功", Toast.LENGTH_SHORT).show();
                    Message message1 = Message.obtain();
                    message1.what = MSG_ADD_CONTACT;
                    mainHandler.sendMessage(message1);
                }
            } else {
                LogUtil.e(TAG, "activity result is null");
                Toast.makeText(HomeActivity.this, "未获取到联系人信息", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == ACTIVITY_RESULT_ADD_CONTACT) {
            if (data != null) {
                String name = data.getStringExtra(CustomKeys.KEY_USER_NAME);
                String sn = data.getStringExtra(CustomKeys.KEY_SN);

                LogUtil.e(TAG, "Custom Name: " + name);
                LogUtil.e(TAG, "SN is: " + sn);

                boolean ret = MIMCApplication.getInstance().insertData(name, sn);
                if (ret) {
                    Message message1 = Message.obtain();
                    message1.what = MSG_ADD_CONTACT;
                    mainHandler.sendMessage(message1);
                }
            } else {
                LogUtil.e(TAG, "add result is null");
                Toast.makeText(HomeActivity.this, "取消增加联系人", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void doClickQrCodeImage() {
        QRCodeFragment fragment = QRCodeFragment.newInstance();
        fragment.show(getFragmentManager(), QRCodeFragment.TAG);
    }

    private void doClickOnRecyclerView() {
        CallingDialog fragment = CallingDialog.getInstance();
        fragment.show(getFragmentManager(), CallingDialog.TAG);
    }

    private void showIncomingCallDialog() {
        IncomingCallDialog fragment = IncomingCallDialog.getInstance();
        fragment.show(getFragmentManager(), IncomingCallDialog.TAG);
    }

    private void doLogin() {
        if (!NetWorkUtils.isNetwork(HomeActivity.this)) {
            Toast.makeText(HomeActivity.this, "未检测到网络, 请稍后重试", Toast.LENGTH_SHORT).show();
            return;
        }
        if (binding.et.getText().toString().contains("Please Input ID") || TextUtils.isEmpty(binding.et.getText().toString())) {
            Toast.makeText(HomeActivity.this, "请输入有效ID", Toast.LENGTH_SHORT).show();
            return;
        }
        if (binding.et.getText().toString().length() > 10) {
            Toast.makeText(HomeActivity.this, "ID过长, 最大长度为10", Toast.LENGTH_SHORT).show();
            return;
        }

        // 如果缓存中记录了user name, 使用缓存的user name, 如果没有则提示 Please Input ID
        String name = binding.et.getText().toString();
        mmkv.putString(CustomKeys.KEY_USER_NAME, name);

        // 使用ANDROID_ID + CustomName 进行login
        MIMCUser user = userManager.newMIMCUser(MIMCApplication.getInstance().getSerial() + name);
        boolean result = user.login();
        if (result) {
            hideInput();
            Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
            uiGoNormalLayout();
        } else {
            Toast.makeText(this, "登录失败, 请稍后再试", Toast.LENGTH_SHORT).show();
        }

        binding.tvUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogoutDialog logoutDialog = LogoutDialog.newInstance();
                logoutDialog.show(getFragmentManager(), LogoutDialog.TAG);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.e(TAG, "onResume()");
        checkIfHaveRecent();
        uiRefreshContact();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkIfHaveRecent() {
        LogUtil.e(TAG, "checkIfHaveRecent()");
        String recentContact = mmkv.getString(CustomKeys.KEY_RECENT_CONTACT, "");
        if (recentContact.length() == 0) {
            binding.tvRecentName.setText("暂无");
            binding.swipeLayout.setSwipeEnabled(false);
            binding.llSwipeInfo.setBackgroundColor(getColor(R.color.colorPrimaryDark));
        } else {
            binding.tvRecentName.setText(recentContact);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.e(TAG, "onDestroy()");
    }

    @Override
    public void onBackPressed() {
        LogUtil.e(TAG, "onBackPressed()");
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(HomeActivity.this, "再按一次退出", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            MIMCApplication.getInstance().exit();
        }
    }

    private void uiGoLoginLayout() {
        binding.llLogin.setVisibility(View.VISIBLE);
        binding.llInitializing.setVisibility(View.GONE);
        binding.llNormal.setVisibility(View.GONE);
        binding.et.requestFocus();
        binding.et.setSelection(binding.et.getText().toString().length()); // 定位光标到最后一个位置
    }

    private void uiGoInitializingLayout() {
        binding.llLogin.setVisibility(View.GONE);
        binding.llInitializing.setVisibility(View.VISIBLE);
        binding.llNormal.setVisibility(View.GONE);
    }

    private void uiGoNormalLayout() {
        binding.llNormal.setVisibility(View.VISIBLE);
        binding.llInitializing.setVisibility(View.GONE);
        binding.llLogin.setVisibility(View.GONE);

        // 显示 user name
        String name = mmkv.getString(CustomKeys.KEY_USER_NAME, "");
        binding.tvUserName.setText(name);
    }


    /**
     * 隐藏键盘
     */
    protected void hideInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View v = getWindow().peekDecorView();
        if (null != v) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void startRecording() {
        // 开始采集前获取运行时录音权限
        if (checkRecordAudioPermission()) {
            audioRecorder.start();
        }
    }


    private boolean checkRecordAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                Toast.makeText(this, "RECORD_AUDIO permission is denied by user.", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            }
            return false;
        }

        return true;
    }

    // audio stuff
    private volatile boolean exit = false;

    private final OnCallStateListener onCallStateListener = new OnCallStateListener() {
        @Override
        public void onLaunched(String fromAccount, String fromResource, long callId, byte[] data) {
            LogUtil.e(TAG, "onLaunched() nothing to do, fromAccount: " + fromAccount + " callId: " + callId);
        }

        @Override
        public void onAnswered(long callId, boolean accepted, String errMsg) {
            LogUtil.e(TAG, "----------onAnswered() start----------");
            LogUtil.e(TAG, "callId:   " + callId);
            LogUtil.e(TAG, "accepted: " + accepted);
            LogUtil.e(TAG, "errMsg:   " + errMsg);
            if (accepted) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(HomeActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                    }
                });
                // 采集数据
                startRecording();
//                startService();
            } else {
                toast("被拒绝");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CallingDialog.getInstance().performClickCloseButton();
                    }
                });
            }
            LogUtil.e(TAG, "----------onAnswered() end  ----------");
        }

        @Override
        public void handleData(long callId, RtsDataType dataType, byte[] data) {
            AV.MIMCRtsPacket audio;
            try {
                audio = AV.MIMCRtsPacket.parseFrom(data);
                audioDecodeQueue.offer(audio);
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onClosed(long callId, String errMsg) {
            Message msg = Message.obtain();
            msg.what = MSG_FINISH;
            Bundle bundle = new Bundle();
            bundle.putString("msg", errMsg);
            msg.setData(bundle);
            callHandler.sendMessageDelayed(msg, MSG_FINISH_DELAY_MS);
        }
    };

    private final OnAudioCapturedListener onAudioCapturedListener = new OnAudioCapturedListener() {
        @Override
        public void onAudioCaptured(byte[] pcmData) {
            audioEncodeQueue.offer(new Audio(pcmData));
        }
    };

    private final OnAudioEncodedListener onAudioEncodedListener = new OnAudioEncodedListener() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onAudioEncoded(byte[] data, long sequence) {
            AV.MIMCRtsPacket audio = AV.MIMCRtsPacket
                    .newBuilder()
                    .setType(AV.MIMC_RTS_TYPE.AUDIO)
                    .setCodecType(AV.MIMC_RTS_CODEC_TYPE.FFMPEG)
                    .setPayload(ByteString.copyFrom(data))
                    .setSequence(sequence)
                    .build();
            if (-1 == UserManager.getInstance().sendRTSData(callingOutID, audio.toByteArray(), RtsDataType.AUDIO)) {
                LogUtil.e(TAG, String.format("Send audio data fail sequence:%d data.length:%d", sequence, data.length));
            }
        }
    };

    private final OnAudioDecodedListener onAudioDecodedListener = new OnAudioDecodedListener() {

//        volatile boolean isInitialized = false;
//        FileOutputStream fileOutputStream = null;

        int db = 20;
        private double factor = Math.pow(10, db / 20);

        //调节PCM数据音量
        //pData原始音频byte数组，nLen原始音频byte数组长度，data2转换后新音频byte数组，nBitsPerSample采样率，multiple表示Math.pow()返回值
        public int amplifyPCMData(byte[] pData, int nLen, byte[] data2, int nBitsPerSample, float multiple) {
            int nCur = 0;
            if (16 == nBitsPerSample) {
                while (nCur < nLen) {
                    short volum = getShort(pData, nCur);

                    volum = (short) (volum * multiple);

                    data2[nCur] = (byte) (volum & 0xFF);
                    data2[nCur + 1] = (byte) ((volum >> 8) & 0xFF);
                    nCur += 2;
                }

            }
            return 0;
        }

        private short getShort(byte[] data, int start) {
            return (short) ((data[start] & 0xFF) | (data[start + 1] << 8));
        }

        @Override
        public void onAudioDecoded(byte[] data) {
            LogUtil.e(TAG, "onAudioDecoded 音频将在此播放!!!!!!!!!!!!");

            // 放大音量
//            byte[] newData = new byte[data.length];
//            amplifyPCMData(data, data.length, newData, 16, 9.0f);

            audioPlayer.play(data, 0, data.length);

//            audioPlayer.play(newData, 0, newData.length);

//            if (nowReceivingVoice) {
//                if (!isInitialized) {
//                    isInitialized = true;
//                    Log.e(TAG, "now we should save pcm data");
//                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//                    String origin = "/storage/emulated/0/Android/data/com.sunmi.interphone" + "/pcm_receive/";
//                    File dir = new File(origin + sdf.format(new Date(System.currentTimeMillis())));
//                    if (!dir.exists() || !dir.isDirectory()) {
//                        boolean mkdirRet = dir.mkdirs();
//                        if (!mkdirRet) {
//                            Log.e(TAG, "dir.mkdirs failed!!!");
//                        }
//                    }
//                    @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("_HH_mm_ss");// HH:mm:ss
//                    File pcmData = new File(dir, "pcmData" + simpleDateFormat.format(new Date(System.currentTimeMillis())));
//                    try {
//                        boolean createNewFileRet = pcmData.createNewFile();
//                        if (!createNewFileRet) {
//                            Log.e(TAG, "createNewFile failed!!!");
//                        }
//                        fileOutputStream = new FileOutputStream(pcmData);
//                        Log.e(TAG, "fileOutputStream is ready");
//                    } catch (IOException e) {
//                        Log.e(TAG, "fileOutputStream not ready!!!");
//                        e.printStackTrace();
//                    }
//                }
//
//                try {
//                    fileOutputStream.write(data, 0, data.length);
//                    fileOutputStream.flush();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//            } else {
//                isInitialized = false;
//                if (fileOutputStream != null) {
//                    try {
//                        fileOutputStream.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
        }
    };

    private void audioStuffPrepare() {
        audioRecorder = new AudioRecorder();
        UserManager.getInstance().setCallStateListener(onCallStateListener);
        audioRecorder.setOnAudioCapturedListener(onAudioCapturedListener);

        audioPlayer = new AudioPlayer(this, AudioManager.MODE_IN_CALL);
        audioPlayer.start();

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        audioEncoder = new FFmpegAudioEncoder();
        audioEncoder.setOnAudioEncodedListener(onAudioEncodedListener);
        audioEncoder.start();

        audioDecoder = new FFmpegAudioDecoder();
        audioDecoder.setOnAudioDecodedListener(onAudioDecodedListener);
        audioDecoder.start();

        audioEncodeQueue = new LinkedBlockingQueue<>();
        audioEncodeThread = new AudioEncodeThread();
        audioEncodeThread.start();

        audioDecodeQueue = new PriorityBlockingQueue<>(24, new Comparator<AV.MIMCRtsPacket>() {
            @Override
            public int compare(AV.MIMCRtsPacket o1, AV.MIMCRtsPacket o2) {
                if (o1.getSequence() > o2.getSequence()) {
                    return 1;
                } else if (o1.getSequence() == o2.getSequence()) {
                    return 0;
                } else {
                    return -1;
                }
            }
        });
        audioDecodeThread = new AudioDecodeThread();
        audioDecodeThread.start();
    }

    class AudioEncodeThread extends Thread {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void run() {
            while (!exit) {
                try {
                    Audio audio = audioEncodeQueue.take();
                    audioEncoder.codec(audio.getData());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class AudioDecodeThread extends Thread {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void run() {
            while (!exit) {
                try {
                    if (audioDecodeQueue.size() > 12) {
                        Log.w(TAG, String.format("Clear decode queue size:%d", audioDecodeQueue.size()));
                        audioDecodeQueue.clear();
                        continue;
                    }

                    AV.MIMCRtsPacket rtsPacket = audioDecodeQueue.take();
                    audioDecoder.codec(rtsPacket.getPayload().toByteArray());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void toast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(msg))
                    Toast.makeText(HomeActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /***
     * 修改NavigationBar背景颜色 可自定义颜色
     * */
    public static void setNavigationBarColor(Activity activity) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setNavigationBarColor(activity.getResources().getColor(R.color.colorPrimaryDark));
    }

}
