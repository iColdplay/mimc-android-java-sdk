package com.xiaomi.mimcdemo.manager;

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
        boolean ret = false;
        return ret;
    }
}
