/*
 * Copyright (c) 2018. welcomeworld All rights reserved
 */

package cn.dmandp.adapter;

import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import cn.dmandp.entity.FavoriteRecyclerViewItem;
import cn.dmandp.tt.R;

public class FavoriteRecyclerViewItemAdapter extends RecyclerView.Adapter<FavoriteRecyclerViewItemAdapter.ViewHolder> {
    private ArrayList<FavoriteRecyclerViewItem> data;
    private OnItemClickListener onItemClickListener;

    public FavoriteRecyclerViewItemAdapter(ArrayList<FavoriteRecyclerViewItem> data) {
        this.data = data;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_favorite, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final FavoriteRecyclerViewItem currentView = data.get(position);
        holder.imageView.setImageDrawable(currentView.getPrimaryImage());
        holder.usernameTextView.setText(currentView.getUsername());
        holder.contentTextView.setText(currentView.getMessage());
        SimpleDateFormat format = new SimpleDateFormat("yy/MM/dd", Locale.CHINA);
        holder.timeTextView.setText(format.format(new Date(currentView.getTime())));
        if (onItemClickListener != null) {
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(v, currentView);
                }
            });
            holder.shareButton.setOnClickListener(new View.OnClickListener() {
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

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView usernameTextView;
        TextView contentTextView;
        TextView timeTextView;
        ImageButton deleteButton;
        ImageButton shareButton;

        private ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.recyclerview_favorite_primaryimage);
            usernameTextView = itemView.findViewById(R.id.recyclerview_favorite_username);
            contentTextView = itemView.findViewById(R.id.recyclerview_favorite_content);
            timeTextView = itemView.findViewById(R.id.recyclerview_favorite_time);
            deleteButton = itemView.findViewById(R.id.recyclerview_favorite_delete);
            shareButton = itemView.findViewById(R.id.recyclerview_favorite_share);
        }
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, FavoriteRecyclerViewItem currentView);
    }
}
