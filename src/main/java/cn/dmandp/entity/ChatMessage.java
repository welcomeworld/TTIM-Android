package cn.dmandp.entity;

/**
 * Created by 萌即正义 on 16/03/2018.
 */

public class ChatMessage {
    private String title;
    private int touxiang;
    private String name;
    private String message;
    private long time;
    private int type;

    public ChatMessage(int touxiang, String title, String name, String message, long time, int type) {
        this.touxiang = touxiang;
        this.title = title;
        this.name = name;
        this.message = message;
        this.time = time;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getTouxiang() {
        return touxiang;
    }

    public void setTouxiang(int touxiang) {
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
