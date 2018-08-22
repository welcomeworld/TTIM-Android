/*
 * Copyright (c) 2018. welcomeworld All rights reserved
 */

package cn.dmandp.tt;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.util.TimeZone;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.dmandp.adapter.HeaderAndFooterAdapter;
import cn.dmandp.adapter.NewFriendRecyclerViewItemAdapter;
import cn.dmandp.common.MyDividerItemDecoration;
import cn.dmandp.dao.TTIMDaoHelper;
import cn.dmandp.entity.NewFriendRecyclerViewItem;
import cn.dmandp.view.RefreshRecyclerView;

public class NewFriendActivity extends BaseActivity {
    TTIMDaoHelper daoHelper = new TTIMDaoHelper(this);
    SQLiteDatabase database;
    private int currentUserId;
    RefreshRecyclerView newFriendRecyclerView;
    NewFriendRecyclerViewItemAdapter newFriendRecyclerViewItemAdapter;
    List<NewFriendRecyclerViewItem> newFriendRecyclerViewItemList=new ArrayList<NewFriendRecyclerViewItem>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newfriend);
        Toolbar toolbar=findViewById(R.id.conversation_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        database=daoHelper.getReadableDatabase();
        SharedPreferences data = getSharedPreferences("data", MODE_PRIVATE);
        currentUserId = data.getInt("currentUserId", -1);
        newFriendRecyclerView = findViewById(R.id.recyclerview_newfriend_main);
        Cursor cursor=database.rawQuery("select * from requests where toid=? and rtype=? order by rtime desc",new String[]{currentUserId+"",0+""});
        while(cursor.moveToNext()){
            newFriendRecyclerViewItemList.add(new NewFriendRecyclerViewItem(cursor.getInt(cursor.getColumnIndex("fromid")),cursor.getInt(cursor.getColumnIndex("fromid"))+"",cursor.getString(cursor.getColumnIndex("rcontent")),cursor.getLong(cursor.getColumnIndex("rtime")),cursor.getInt(cursor.getColumnIndex("rstatus")),null));
        }
        newFriendRecyclerViewItemAdapter=new NewFriendRecyclerViewItemAdapter(newFriendRecyclerViewItemList);
        newFriendRecyclerView.setAdapter(new HeaderAndFooterAdapter(newFriendRecyclerViewItemAdapter));
        LinearLayoutManager newFriendRecyclerViewLayoutManager=new LinearLayoutManager(NewFriendActivity.this);
        newFriendRecyclerViewLayoutManager.setOrientation(RecyclerView.VERTICAL);
        newFriendRecyclerView.setLayoutManager(newFriendRecyclerViewLayoutManager);
        newFriendRecyclerView.setItemAnimator(new DefaultItemAnimator());
        newFriendRecyclerView.addItemDecoration(new MyDividerItemDecoration());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}

