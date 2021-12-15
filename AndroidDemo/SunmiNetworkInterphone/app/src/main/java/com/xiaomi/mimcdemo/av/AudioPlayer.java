package com.xiaomi.mimcdemo.av;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.LoudnessEnhancer;
import android.media.audiofx.NoiseSuppressor;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.lang.reflect.Method;

import static android.media.AudioTrack.*;
import static com.xiaomi.mimcdemo.common.Constant.*;

/**
 * Created by houminjiang on 18-6-6.
 */

public class AudioPlayer implements Player {
    private int minBufferSize = 0;
    private AudioTrack audioTrack;
    private boolean isPlayStarted = false;
    private Context context;
    private int defaultAudioMode;
    private static final String TAG = "AudioPlayer";


    public AudioPlayer(Context context, int defaultAudioMode) {
        this.context = context;
        this.defaultAudioMode = defaultAudioMode;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean start() {
        return startPlayer(DEFAULT_PLAY_STREAM_TYPE, DEFAULT_AUDIO_SAMPLE_RATE, DEFAULT_PLAY_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT);
    }

    @Override
    public void stop() {
        stopPlayer();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private synchronized boolean startPlayer(int streamType, int sampleRateInHz, int channelConfig, int audioFormat) {
        if (isPlayStarted) {
            Log.w(TAG, "Audio player started.");
            return false;
        }

        minBufferSize = 2 * AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        if (minBufferSize == AudioTrack.ERROR_BAD_VALUE) {
            Log.w(TAG, "Invalid parameters.");
            return false;
        }

        // 使用新的参数创建audioTrack
//        int m_out_buf_size = AudioTrack.getMinBufferSize(44100,
//                AudioFormat.CHANNEL_OUT_MONO,
//                AudioFormat.ENCODING_PCM_16BIT);
//
//        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 8000,
//                AudioFormat.CHANNEL_OUT_MONO,
//                AudioFormat.ENCODING_PCM_16BIT,
//                m_out_buf_size,
//                AudioTrack.MODE_STREAM);
//        audioTrack.setStereoVolume(1f, 1f);

        audioTrack = new AudioTrack((new AudioAttributes.Builder())
            .setLegacyStreamType(streamType)
            .build(),
            (new AudioFormat.Builder())
                .setChannelMask(channelConfig)
                .setEncoding(audioFormat)
                .setSampleRate(sampleRateInHz)
                .build(),
            minBufferSize,
            DEFAULT_PLAY_MODE, AudioManager.AUDIO_SESSION_ID_GENERATE);
        if (audioTrack.getState() == AudioTrack.STATE_UNINITIALIZED) {
            Log.w(TAG, "AudioTrack initialize fail.");
            return false;
        }

//        // try to make more volume in play flow() start
//        LoudnessEnhancer enhancer = new LoudnessEnhancer(audioTrack.getAudioSessionId());
//        NoiseSuppressor.create(audioTrack.getAudioSessionId());
//        AcousticEchoCanceler.create(audioTrack.getAudioSessionId());
//
//        enhancer.setTargetGain(10);
//        enhancer.setEnabled(true);
//        // try to make more volume in play flow() end


        isPlayStarted = true;
        audioTrack.play();
        setAudioMode(defaultAudioMode);
        Log.i(TAG, "Start audio player success.");

        return true;
    }

    private void stopPlayer() {
        if (!isPlayStarted) {
            return;
        }

        isPlayStarted = false;
        if (audioTrack.getState() == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack.stop();
        }
        audioTrack.release();
        setAudioMode(AudioManager.MODE_NORMAL);
        Log.i(TAG, "Stop audio player success.");
    }

    public boolean play(byte[] audioData, int offsetInBytes, int sizeInBytes) {
        if (!isPlayStarted) {
            Log.w(TAG, "Audio player not started.");
            return false;
        }

        int result = audioTrack.write(audioData, offsetInBytes, sizeInBytes);

        if (result == ERROR_INVALID_OPERATION) {
            Log.w(TAG, "The track isn't properly initialized.");
        } else if (result == ERROR_BAD_VALUE) {
            Log.w(TAG, "The parameters don't resolve to valid data and indexes.");
        } else if (result == ERROR_DEAD_OBJECT) {
            Log.w(TAG, "The AudioTrack is not valid anymore and needs to be recreated.");
        } else if (result == ERROR) {
            Log.w(TAG, "Other error.");
        }
        Log.d(TAG, "Played:" + result + " bytes.");

        return true;
    }
    private void setAudioMode(int mode) {
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
//        if (mode == AudioManager.MODE_NORMAL) {
//            audioManager.setSpeakerphoneOn(false);
//        } else if (mode == AudioManager.MODE_IN_COMMUNICATION || mode == AudioManager.MODE_IN_CALL) {
//            if(isBluetoothHeadsetConnected() || audioManager.isWiredHeadsetOn()) {
//                audioManager.setSpeakerphoneOn(false);
//            } else {
//                audioManager.setSpeakerphoneOn(true);
//            }
//        }
//        audioManager.setMode(mode);

        audioManager.setSpeakerphoneOn(true); //无论如何 打开外放
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
    }

    public static boolean isBluetoothHeadsetConnected() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()
                && mBluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothHeadset.STATE_CONNECTED;
    }

}
