/*
 * Copyright (c) 2018. welcomeworld All rights reserved
 */

package cn.dmandp.entity;

import android.graphics.drawable.Drawable;

public class FriendRecyclerViewItem {
    private Drawable primaryImage;
    private Drawable subImage;
    private String username;
    private int uId;

    public FriendRecyclerViewItem(int uId, Drawable primaryImage, Drawable subImage, String username) {
        this.primaryImage = primaryImage;
        this.subImage = subImage;
        this.username = username;
        this.uId = uId;
    }

    public Drawable getPrimaryImage() {
        return primaryImage;
    }

    public void setPrimaryImage(Drawable primaryImage) {
        this.primaryImage = primaryImage;
    }

    public Drawable getSubImage() {
        return subImage;
    }

    public void setSubImage(Drawable subImage) {
        this.subImage = subImage;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getUId() {
        return uId;
    }
}
