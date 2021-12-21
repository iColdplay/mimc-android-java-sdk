package com.xiaomi.mimcdemo.database;

public class Contact {
    private String customName;
    private String sn;
    private int missCall;

    public Contact(String customName, String sn, int missCall){
        this.customName = customName;
        this.sn = sn;
        this.missCall = missCall;
    }

    public String getCustomName() {
        return customName;
    }

    public String getSn() {
        return sn;
    }

    public int getMissCall() {
        return missCall;
    }

    public void setMissCall(int missCall) {
        this.missCall = missCall;
    }
}
