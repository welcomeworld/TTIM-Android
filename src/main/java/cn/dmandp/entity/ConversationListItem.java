package cn.dmandp.entity;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * Created by 萌即正义 on 14/03/2018.
 */

public class ConversationListItem {
    private String username;
    private String message;
    private String time;
    private String newMessage;
    private Drawable image;
    private int uId;

    public ConversationListItem(int uId, String username, String message, String time, String newMessage, Drawable image) {
        this.uId = uId;
        this.username = username;
        this.message = message;
        this.time = time;
        this.image = image;
        this.newMessage = newMessage;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setNewMessage(String newMessage) {
        this.newMessage = newMessage;
    }

    public void setImage(Drawable image) {
        this.image = image;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public String getTime() {
        return time;
    }

    public Drawable getImage() {
        return image;
    }

    public int getUId() {
        return uId;
    }

    public String getNewMessage() {
        return newMessage;
    }
}
