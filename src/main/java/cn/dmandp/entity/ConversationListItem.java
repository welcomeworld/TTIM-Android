package cn.dmandp.entity;

import android.graphics.Bitmap;

/**
 * Created by 萌即正义 on 14/03/2018.
 */

public class ConversationListItem {
    private String username;
    private String message;
    private String time;
    private String newMessage;
    private Bitmap image;
    private int uId;

    public ConversationListItem(int uId, String username, String message, String time, String newMessage, Bitmap image) {
        this.uId = uId;
        this.username = username;
        this.message = message;
        this.time = time;
        this.image = image;
        this.newMessage = newMessage;
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

    public Bitmap getImage() {
        return image;
    }

    public int getUId() {
        return uId;
    }

    public String getNewMessage() {
        return newMessage;
    }
}
