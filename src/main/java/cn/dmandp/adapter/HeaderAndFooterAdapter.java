/*
 * Copyright (c) 2018. welcomeworld All rights reserved
 */

package cn.dmandp.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

public class HeaderAndFooterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public SparseArray<View> headerViews;
    public SparseArray<View> footViews;
    public RecyclerView.Adapter adapter;
    private static final int HEADER_BASE = 1000000;
    private static final int FOOTER_BASE = 2000000;

    public HeaderAndFooterAdapter(RecyclerView.Adapter adapter) {
        this.adapter = adapter;
        this.headerViews = new SparseArray<>();
        this.footViews = new SparseArray<>();
        this.adapter.registerAdapterDataObserver(dataObserver);

    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (isHeaderViewType(viewType)) {
            //头部item
            return new RecyclerView.ViewHolder(headerViews.get(viewType)) {
            };
        } else if (isFooterViewType(viewType)) {
            //尾部item
            return new RecyclerView.ViewHolder(footViews.get(viewType)) {
            };
        }
        return adapter.onCreateViewHolder(parent, viewType);
    }

    private boolean isHeaderViewType(int viewType) {
        return headerViews.indexOfKey(viewType) >= 0;
    }

    private boolean isFooterViewType(int viewType) {
        return footViews.indexOfKey(viewType) >= 0;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (isFooterPosition(position) || isHeaderPosition(position)) {
            return;
        }
        adapter.onBindViewHolder(holder, position - headerViews.size());
    }

    @Override
    public int getItemViewType(int position) {
        if (isHeaderPosition(position)) {
            return headerViews.keyAt(position);
        }
        if (isFooterPosition(position)) {
            return footViews.keyAt(position - headerViews.size() - adapter.getItemCount());
        }
        return adapter.getItemViewType(position - headerViews.size());
    }

    @Override
    public int getItemCount() {
        return adapter.getItemCount() + headerViews.size() + footViews.size();
    }

    public void addHeaderView(View view) {
        if (headerViews.indexOfValue(view) >= 0) {
            //view have been in headers
            return;
        }
        headerViews.put(HEADER_BASE + headerViews.size(), view);
        notifyDataSetChanged();
    }

    public void addFooterView(View view) {
        if (footViews.indexOfValue(view) >= 0) {
            //view have been in footers
            return;
        }
        footViews.put(FOOTER_BASE + footViews.size(), view);
        notifyDataSetChanged();
    }

    public boolean isHeaderPosition(int position) {
        return position < headerViews.size();
    }

    public boolean isFooterPosition(int position) {
        return position >= (headerViews.size() + adapter.getItemCount());
    }

    public View getHeaderView(int position) {
        return headerViews.get(headerViews.keyAt(position));
    }

    RecyclerView.AdapterDataObserver dataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            notifyItemRangeChanged(positionStart + headerViews.size(), itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
            notifyItemRangeChanged(positionStart + headerViews.size(), itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            notifyItemRangeInserted(positionStart + headerViews.size(), itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            notifyItemRangeRemoved(positionStart + headerViews.size(), itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            super.onItemRangeMoved(fromPosition + headerViews.size(), toPosition, itemCount);
        }
    };
}
