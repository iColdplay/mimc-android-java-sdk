package com.xiaomi.mimcdemo.database;

public class Contact {
    private String customName;
    private String sn;

    public Contact(String customName, String sn){
        this.customName = customName;
        this.sn = sn;
    }

    public String getCustomName() {
        return customName;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }
}
