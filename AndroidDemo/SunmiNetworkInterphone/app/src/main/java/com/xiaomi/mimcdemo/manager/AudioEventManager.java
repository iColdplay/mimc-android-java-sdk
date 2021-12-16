package com.xiaomi.mimcdemo.manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.xiaomi.mimc.MIMCGroupMessage;
import com.xiaomi.mimc.MIMCMessage;
import com.xiaomi.mimc.MIMCOnlineMessageAck;
import com.xiaomi.mimc.MIMCServerAck;
import com.xiaomi.mimc.common.MIMCConstant;
import com.xiaomi.mimc.data.RtsDataType;
import com.xiaomi.mimcdemo.av.AudioPlayer;
import com.xiaomi.mimcdemo.av.AudioRecorder;
import com.xiaomi.mimcdemo.av.FFmpegAudioDecoder;
import com.xiaomi.mimcdemo.av.FFmpegAudioEncoder;
import com.xiaomi.mimcdemo.bean.Audio;
import com.xiaomi.mimcdemo.bean.ChatMsg;
import com.xiaomi.mimcdemo.common.CustomKeys;
import com.xiaomi.mimcdemo.common.UserManager;
import com.xiaomi.mimcdemo.listener.OnAudioCapturedListener;
import com.xiaomi.mimcdemo.listener.OnAudioDecodedListener;
import com.xiaomi.mimcdemo.listener.OnAudioEncodedListener;
import com.xiaomi.mimcdemo.listener.OnCallStateListener;
import com.xiaomi.mimcdemo.proto.AV;
import com.xiaomi.mimcdemo.utils.AppUtil;
import com.xiaomi.mimcdemo.utils.LogUtil;
import com.xiaomi.mimcdemo.utils.PingPongAutoReplier;

import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/***
 * 音频事件管理
 */
public class AudioEventManager {

    private static final String TAG = AudioEventManager.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")
    private static AudioEventManager instance;

    private AudioEventManager() {
        mContext = AppUtil.getContext();
        init();
    }

    // 加锁单例实例化 保证多线程实例化唯一性
    public synchronized static AudioEventManager getInstance() {
        if (instance == null) {
            instance = new AudioEventManager();
        }
        return instance;
    }

    private Context mContext;

    public AudioEventCallback getAudioEventCallback() {
        return audioEventCallback;
    }

    public void setAudioEventCallback(AudioEventCallback audioEventCallback) {
        this.audioEventCallback = audioEventCallback;
    }

    public interface AudioEventCallback{

    }

    private AudioEventCallback audioEventCallback;

    private void init() {
        // 使用htCall 作为call独立处理线程
        htCall = new HandlerThread("call_handler_thread");
        callHandler = new Handler(htCall.getLooper()) {
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

                    UserManager.getInstance().answerCall();

                    audioPlayer.start();

                    Toast.makeText(mContext, "对方正在说话", Toast.LENGTH_SHORT).show();
                    LogUtil.e(TAG, "----------MSG_CALL_INCOMING handle end  ---------");
                }

                if (msg.what == MSG_FINISH) {
                    LogUtil.e(TAG, "----------MSG_FINISH handle start----------");
                    Bundle data = msg.getData();
                    String errorMessage = data.getString("msg");
                    Toast.makeText(mContext, "通讯结束 " + errorMessage, Toast.LENGTH_SHORT).show();

                    if (audioPlayer != null) {
                        audioPlayer.stop();
                    }
                    if (audioRecorder != null) {
                        audioRecorder.stop();
                    }

                    setPttIdle(); //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~状态转换为Speaking

                    LogUtil.e(TAG, "----------MSG_FINISH handle end  ----------");
                }

                if (msg.what == MSG_CALL_GOING_OUT) {
                    LogUtil.e(TAG, "----------MSG_CALL_GOING_OUT handle start----------");

                    Bundle data = msg.getData();
                    String goingOutName = data.getString(CustomKeys.KEY_GOING_OUT_NAME);
                    String goingOutId = data.getString(CustomKeys.KEY_GOING_OUT_ID);
                    String targetCountName = goingOutId + goingOutName;
                    LogUtil.e(TAG, "targetCountName: " + targetCountName);
                    callingOutID = UserManager.getInstance().dialCall(targetCountName, null, "AUDIO".getBytes());
                    if (callingOutID == -1) {
                        Toast.makeText(mContext, "拨号失败, 请检查网络", Toast.LENGTH_SHORT).show();
                    }
                    LogUtil.e(TAG, "----------MSG_CALL_GOING_OUT handle end  ----------");
                }

                if (msg.what == MSG_CALL_CLOSE_BY_HOST) {
                    LogUtil.e(TAG, "----------MSG_CALL_CLOSE_BY_HOST handle start----------");

                    UserManager.getInstance().closeCall(callingOutID);

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
                    Toast.makeText(mContext, "通讯结束 ", Toast.LENGTH_SHORT).show();

                    if (audioPlayer != null) {
                        audioPlayer.stop();
                    }
                    if (audioRecorder != null) {
                        audioRecorder.stop();
                    }
                    LogUtil.e(TAG, "----------MSG_CALL_CLOSE_BY_CLIENT handle start----------");
                }
            }
        };

        audioPrepare();

        userManager = UserManager.getInstance();
    }

    public static String callerName = "未知";
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
    private volatile boolean exit = false;

    private UserManager userManager;

    private HandlerThread htCall;
    private Handler callHandler;
    public static final int MSG_FINISH_DELAY_MS = 1000;
    public static final int MSG_CALL_INCOMING = 2001;
    public static final int MSG_FINISH = 2002;
    public static final int MSG_CALL_GOING_OUT = 2003;
    public static final int MSG_CALL_CLOSE_BY_HOST = 2004;
    public static final int MSG_CALL_CLOSE_BY_CLIENT = 2005;

    private static volatile String PTT_STATUS = "idle";
    private final static String PTT_STATUS_IDLE = "idle";
    private final static String PTT_STATUS_SPEAKING = "speaking";
    private final static String PTT_STATUS_LISTENING = "listening";

    public synchronized boolean isPttIdle() {
        return PTT_STATUS.equals(PTT_STATUS_IDLE);
    }

    public synchronized boolean isPttSpeaking() {
        return PTT_STATUS.equals(PTT_STATUS_SPEAKING);
    }

    public synchronized boolean ispPttListening() {
        return PTT_STATUS.equals(PTT_STATUS_LISTENING);
    }

    public synchronized boolean setPttIdle() {
        PTT_STATUS = PTT_STATUS_IDLE;
        return true;
    }

    public synchronized boolean setPttSpeaking() {
        PTT_STATUS = PTT_STATUS_SPEAKING;
        return true;
    }

    public synchronized boolean setPttListening() {
        PTT_STATUS = PTT_STATUS_LISTENING;
        return true;
    }

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
                // 对方接受语音请求
                Toast.makeText(mContext, "您可以开始说话了", Toast.LENGTH_SHORT).show();
                // 开始录音并上送到服务器
                startRecording();
            } else {
                // 对方未接受语音请求
                Toast.makeText(mContext, "当前用户正忙", Toast.LENGTH_SHORT).show();
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

//        int db = 20;
//        private double factor = Math.pow(10, db / 20);

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
            audioPlayer.play(data, 0, data.length);
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void startRecording() {
        audioRecorder.start();
    }

    private void audioPrepare() {
        audioRecorder = new AudioRecorder();
        UserManager.getInstance().setCallStateListener(onCallStateListener);
        audioRecorder.setOnAudioCapturedListener(onAudioCapturedListener);

        audioPlayer = new AudioPlayer(mContext, AudioManager.MODE_IN_CALL);
        audioPlayer.start();

        audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

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

    /**
     * 传入 account信息 以及 callID信息, 提交事件到callHandler处理来电任务, 作为唯一的来电事件触发入口, 由UserManager调用
     * @param fromAccount
     * @param callId
     * @return
     */
    public synchronized boolean sendMessageCallIncoming(String fromAccount, long callId) {
        if (fromAccount == null || TextUtils.isEmpty(fromAccount)) {
            LogUtil.e(TAG, "param error");
            Toast.makeText(mContext, "异常呼叫, 请稍后再试", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!isPttIdle()) {
            Toast.makeText(mContext, "网络对讲正忙, 请稍后再试", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            LogUtil.e(TAG, "IDLE, but now CALL INCOMING");

            setPttListening(); //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~状态变化为Listening

            Message message1 = callHandler.obtainMessage();
            message1.what = MSG_CALL_INCOMING;
            Bundle data = new Bundle();
            data.putString(CustomKeys.KEY_INCOMING_CALL_ACCOUNT, fromAccount);
            data.putLong(CustomKeys.KEY_INCOMING_CALL_ID, callId);
            message1.setData(data);
            message1.sendToTarget();
            return true;
        }
    }

}
