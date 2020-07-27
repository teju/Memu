package com.memu.modules;

import java.io.Serializable;

/**
 * Created by chanpyaeaung on 5/4/16.
 */
public class BeanGCM implements Serializable {

    public static final String OBJ_NAME = "BeanGCM";
    private static final long serialVersionUID = 5280164088757079832L;

    private String dt, collapse_key, t, alert, sound, message;
    private int pushid,type;

    public BeanGCM() {}

    public String getDt() {
        return dt;
    }

    public void setDt(String dt) {
        this.dt = dt;
    }

    public String getCollapse_key() {
        return collapse_key;
    }

    public void setCollapse_key(String collapse_key) {
        this.collapse_key = collapse_key;
    }

    public String getT() {
        return t;
    }

    public void setT(String t) {
        this.t = t;
    }

    public String getAlert() {
        return alert;
    }

    public void setAlert(String alert) {
        this.alert = alert;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public int getPushid() {
        return pushid;
    }

    public void setPushid(int pushid) {
        this.pushid = pushid;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
