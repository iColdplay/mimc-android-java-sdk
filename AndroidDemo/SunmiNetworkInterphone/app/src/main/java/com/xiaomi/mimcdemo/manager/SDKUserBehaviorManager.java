package com.xiaomi.mimcdemo.manager;

import com.tencent.mmkv.MMKV;
import com.xiaomi.mimc.MIMCGroupMessage;
import com.xiaomi.mimc.MIMCMessage;
import com.xiaomi.mimc.MIMCOnlineMessageAck;
import com.xiaomi.mimc.MIMCServerAck;
import com.xiaomi.mimc.MIMCUser;
import com.xiaomi.mimc.common.MIMCConstant;
import com.xiaomi.mimcdemo.bean.ChatMsg;
import com.xiaomi.mimcdemo.common.CustomKeys;
import com.xiaomi.mimcdemo.common.NetWorkUtils;
import com.xiaomi.mimcdemo.common.UserManager;
import com.xiaomi.mimcdemo.ui.MainApplication;
import com.xiaomi.mimcdemo.utils.AppUtil;
import com.xiaomi.mimcdemo.utils.LogUtil;
import com.xiaomi.mimcdemo.utils.PingPongAutoReplier;

import java.util.Objects;

/***
 * SDK User 状态管理
 */
public class SDKUserBehaviorManager {

    private static final String TAG = SDKUserBehaviorManager.class.getSimpleName();

    private static SDKUserBehaviorManager instance;

    private SDKUserBehaviorManager(){
        mmkv = MMKV.defaultMMKV();
        userManager = UserManager.getInstance();
        UserManager.getInstance().setHandleMIMCMsgListener(onHandleMIMCMsgListener);
    }

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
            if(status.equals(MIMCConstant.OnlineStatus.ONLINE)){
                isUserOnline = true;
            }
            if(status.equals(MIMCConstant.OnlineStatus.OFFLINE)){
                isUserOnline = false;
            }
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

    public synchronized static SDKUserBehaviorManager getInstance(){
        if(instance == null){
            instance = new SDKUserBehaviorManager();
        }
        return instance;
    }

    private MMKV mmkv;
    private UserManager userManager;

    private volatile boolean isUserOnline = false;
    private boolean isOnline(){
        return isUserOnline;
    }

    /*
     * s.1 判断有无网络
     * s.2 使用mmkv中存储的用户名进行登录
     */
    public synchronized boolean loginSDKUser(){
        LogUtil.e(TAG, "loginSDKUser");
        if(!NetWorkUtils.isNetwork(Objects.requireNonNull(AppUtil.getContext()))){
            LogUtil.e(TAG, "没有网络, 请检查网络后重试");
            return false;
        }
        String name = mmkv.getString(CustomKeys.KEY_USER_NAME, "默认名称");
        MIMCUser user = userManager.newMIMCUser(MainApplication.getInstance().getSerial() + name);
        return user.login();
    }



}
