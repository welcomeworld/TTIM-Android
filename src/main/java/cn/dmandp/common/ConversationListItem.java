package cn.dmandp.common;

/**
 * Created by 萌即正义 on 14/03/2018.
 */

public class ConversationListItem {
    private String username;
    private String message;
    private String time;
    private String newMessage;
    private int imageId;

    public ConversationListItem(String username, String message, String time, String newMessage, int imageId) {
        this.username = username;
        this.message = message;
        this.time = time;
        this.imageId = imageId;
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

    public int getImageId() {
        return imageId;
    }

    public String getNewMessage() {
        return newMessage;
    }
}
