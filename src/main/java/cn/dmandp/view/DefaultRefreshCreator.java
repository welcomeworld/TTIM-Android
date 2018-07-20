/*
 * Copyright (c) 2018. welcomeworld All rights reserved
 */

package cn.dmandp.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

import cn.dmandp.tt.R;

public class DefaultRefreshCreator extends RefreshViewCreator {
    // 加载数据的ImageView
    private TextView mRefreshIv;

    @Override
    public void setRefreshView(View view) {
        //View refreshView = LayoutInflater.from(context).inflate(R.layout.recyclerview_refresh, parent, false);
        //mRefreshIv = refreshView.findViewById(R.id.refresh_text);
        mRefreshIv = view.findViewById(R.id.refresh_text);
        //return refreshView;
    }

    @Override
    public View getRefreshView(Context context, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.recyclerview_refresh, parent, false);
        mRefreshIv = view.findViewById(R.id.refresh_text);
        return view;
    }

    @Override
    public void onPull(int currentDragHeight, int refreshViewHeight, int currentRefreshStatus) {

    }

    @Override
    public void onRefreshing() {
        mRefreshIv.setText("刷新中");
    }

    @Override
    public void onStopRefresh() {
        mRefreshIv.setText("下拉刷新");
    }
}
