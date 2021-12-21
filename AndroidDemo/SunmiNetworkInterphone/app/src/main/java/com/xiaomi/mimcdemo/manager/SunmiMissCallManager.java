package com.xiaomi.mimcdemo.manager;

import android.os.Bundle;
import android.os.Message;

import com.xiaomi.mimcdemo.common.CustomKeys;
import com.xiaomi.mimcdemo.ui.HomeActivity;
import com.xiaomi.mimcdemo.utils.LogUtil;

/***
 * 未接来电管理
 */
public class SunmiMissCallManager {

    private static final String TAG = SunmiMissCallManager.class.getSimpleName();

    private static SunmiMissCallManager instance;

    private SunmiMissCallManager(){}

    public synchronized static SunmiMissCallManager getInstance(){
        if (instance == null) {
            instance = new SunmiMissCallManager();
        }
        return instance;
    }

    public boolean missCallHappened(String fromAccount){
        String contactName = fromAccount.substring(16);
        String sn = fromAccount.replace(contactName, "");
        LogUtil.e(TAG, "missCallHappened, contactName:" + contactName + " sn:" + sn);
        boolean ret = ContactManager.getInstance().updateDataByMissCallHappened(contactName, sn);
        if(ret){
            LogUtil.e(TAG, "miss call, let home ActivityKnow");
            Message message1 = Message.obtain();
            Bundle data = new Bundle();
            data.putString(CustomKeys.KEY_SN, sn);
            message1.setData(data);
            message1.what = HomeActivity.MSG_MISS_CALL_HAPPENED;
//            HomeActivity.mainHandler.sendMessage(message1); // recycler view 无法调整未显示UI的position
        }
        return ret;
    }
}
