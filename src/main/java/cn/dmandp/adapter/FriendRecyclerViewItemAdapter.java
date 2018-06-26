/*
 * Copyright (c) 2018. welcomeworld All rights reserved
 */

package cn.dmandp.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import cn.dmandp.entity.FriendRecyclerViewItem;
import cn.dmandp.tt.R;

public class FriendRecyclerViewItemAdapter extends RecyclerView.Adapter<FriendRecyclerViewItemAdapter.ViewHolder> {
    ArrayList<FriendRecyclerViewItem> data;
    private OnItemClickListener onItemClickListener;

    public FriendRecyclerViewItemAdapter(ArrayList<FriendRecyclerViewItem> data) {
        this.data = data;
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int uId);
    }

    public void setOnItemClickListener(FriendRecyclerViewItemAdapter.OnItemClickListener onItemClickListener) {
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
        holder.actionButton.setImageDrawable(currentView.getSubImage());
        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onItemClick(view, currentView.getUId());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
