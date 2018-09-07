package cn.dmandp.entity;

import android.graphics.drawable.Drawable;

/**
 * Created by 萌即正义 on 16/03/2018.
 */

public class ChatMessage {
    private Drawable touxiang;
    private String name;
    private String message;
    private long time;
    private int type;

    public int getUid() {
        return uid;
    }

    private int uid;

    public ChatMessage(Drawable touxiang, String name, String message, long time, int type,int uid) {
        this.touxiang = touxiang;
        this.name = name;
        this.message = message;
        this.time = time;
        this.type = type;
        this.uid=uid;
    }

    public Drawable getTouxiang() {
        return touxiang;
    }

    public void setTouxiang(Drawable touxiang) {
        this.touxiang = touxiang;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
