/*
 * Copyright (c) 2018. welcomeworld All rights reserved
 */

package cn.dmandp.entity;

import android.graphics.drawable.Drawable;

public class NewFriendRecyclerViewItem {
    private String username;
    private String message;
    private Long time;
    private Integer status;
    private Drawable image;
    private int uId;

    public NewFriendRecyclerViewItem(int uId, String username, String message, Long time, Integer status, Drawable image) {
        this.uId = uId;
        this.username = username;
        this.message = message;
        this.time = time;
        this.image = image;
        this.status = status;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public void setStatus(Integer status) {
        this.status = status;
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

    public Long getTime() {
        return time;
    }

    public Drawable getImage() {
        return image;
    }

    public int getUId() {
        return uId;
    }

    public Integer getStatus() {
        return status;
    }
}
