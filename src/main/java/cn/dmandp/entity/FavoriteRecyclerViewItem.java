/*
 * Copyright (c) 2018. welcomeworld All rights reserved
 */

package cn.dmandp.entity;

import android.graphics.drawable.Drawable;

public class FavoriteRecyclerViewItem {
    private String username;
    private String message;
    private long time;
    private Drawable primaryImage;

    public int getuId() {
        return uId;
    }

    public void setuId(int uId) {
        this.uId = uId;
    }

    private int uId;

    public int getToId() {
        return toId;
    }

    private int toId;

    public FavoriteRecyclerViewItem(int uId, String username, String message, long time, Drawable primaryImage, int toId) {
        this.uId = uId;
        this.username = username;
        this.message = message;
        this.time = time;
        this.primaryImage = primaryImage;
        this.toId = toId;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public Drawable getPrimaryImage() {
        return primaryImage;
    }

    public void setPrimaryImage(Drawable primaryImage) {
        this.primaryImage = primaryImage;
    }
}
