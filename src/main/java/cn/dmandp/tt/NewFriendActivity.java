/*
 * Copyright (c) 2018. welcomeworld All rights reserved
 */

package cn.dmandp.tt;

import android.os.Bundle;
import android.support.annotation.Nullable;

import cn.dmandp.view.RefreshRecyclerView;

public class NewFriendActivity extends BaseActivity {
    RefreshRecyclerView newFriendRecyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newfriend);
        newFriendRecyclerView = findViewById(R.id.recyclerview_newfriend_main);
    }
}
