package cn.dmandp.entity;

import android.graphics.Bitmap;

/**
 * Created by 萌即正义 on 16/03/2018.
 */

public class ChatMessage {
    private Bitmap touxiang;
    private String name;
    private String message;
    private String time;
    private int type;

    public ChatMessage(Bitmap touxiang, String name, String message, String time, int type) {
        this.touxiang = touxiang;
        this.name = name;
        this.message = message;
        this.time = time;
        this.type = type;
    }

    public Bitmap getTouxiang() {
        return touxiang;
    }

    public void setTouxiang(Bitmap touxiang) {
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
