/*
 * Copyright (c) 2018. welcomeworld All rights reserved
 */

package cn.dmandp.adapter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import cn.dmandp.entity.ConversationListItem;
import cn.dmandp.entity.FriendRecyclerViewItem;
import cn.dmandp.tt.ConversationActivity;
import cn.dmandp.tt.MainActivity;
import cn.dmandp.tt.R;

public class FriendRecyclerViewItemAdapter extends RecyclerView.Adapter<FriendRecyclerViewItemAdapter.ViewHolder> {
    ArrayList<FriendRecyclerViewItem> data;
    private OnItemClickListener onItemClickListener;

    public FriendRecyclerViewItemAdapter(ArrayList<FriendRecyclerViewItem> data) {
        this.data = data;
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, FriendRecyclerViewItem currentView);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }



    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView usernameTextView;
        ImageButton actionButton;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.recyclerview_friend_image);
            usernameTextView = itemView.findViewById(R.id.recyclerview_friend_username);
            actionButton = itemView.findViewById(R.id.recyclerview_friend_action);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_friend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final FriendRecyclerViewItem currentView = data.get(position);
        holder.imageView.setImageDrawable(currentView.getPrimaryImage());
        holder.usernameTextView.setText(currentView.getUsername());
        if (onItemClickListener != null) {
            holder.actionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(v, currentView);
                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onItemClick(view, currentView);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
