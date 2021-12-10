package com.xiaomi.mimcdemo.utils;

import android.os.Bundle;
import android.os.Message;

import com.xiaomi.mimcdemo.bean.ChatMsg;
import com.xiaomi.mimcdemo.common.UserManager;
import com.xiaomi.mimcdemo.ui.HomeActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PingPongAutoReplier {

    private static final String TAG = PingPongAutoReplier.class.getSimpleName();

    private static final String PING = "ping";
    private static final String PONG = "pong";

    private static PingPongAutoReplier instance;

    public synchronized static PingPongAutoReplier getInstance() {
        if (instance == null) instance = new PingPongAutoReplier();
        return instance;
    }

    private PingPongAutoReplier() {
        executorService = newCachedThreadPool();
    }

    private final ExecutorService executorService;

    public ExecutorService newCachedThreadPool() {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE, 10L, TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>());
    }

    public void executePingPongRunnable(ChatMsg chatMsg){
        PingPongRunnable runnable = new PingPongRunnable(chatMsg);
        executorService.execute(runnable);
    }

    static class PingPongRunnable implements Runnable{

        private final ChatMsg chatMsg;

        PingPongRunnable(ChatMsg msg){
            chatMsg = msg;
        }

        @Override
        public void run() {
            String fromAccount = chatMsg.getFromAccount();
            String payload = new String(chatMsg.getMsg().getPayload());
            if(payload.toLowerCase().contains(PING)){
                LogUtil.e(TAG, "receive PING from account: " + fromAccount);
                LogUtil.e(TAG, "reply with PONG message");
                UserManager.getInstance().getMIMCUser().sendMessage(fromAccount, PONG.getBytes());
            }
            if(payload.toLowerCase().contains(PONG)){
                LogUtil.e(TAG, "receive PONG from account: " + fromAccount);
                LogUtil.e(TAG, "deal with it");
                boolean ret = HomeActivity.pongSet.add(fromAccount);
                LogUtil.e(TAG, "add fromAccount into set: " + ret);
//                Message message1 = Message.obtain();
//                Bundle data = new Bundle();
//                data.putString("fromAccount", fromAccount);
//                message1.setData(data);
//                HomeActivity.handlerOnlineDetector.sendMessage(message1);
            }
        }
    }

}
