package cn.dmandp.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import cn.dmandp.entity.ConversationListItem;
import cn.dmandp.tt.R;


/**
 * Created by 萌即正义 on 14/03/2018.
 */

public class ConversationListItemAdapter extends RecyclerView.Adapter<ConversationListItemAdapter.ViewHolder> {
    private ArrayList<ConversationListItem> recycleViewData;
    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public ConversationListItemAdapter(List<ConversationListItem> data) {
        recycleViewData = (ArrayList<ConversationListItem>) data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final ConversationListItem currentView = recycleViewData.get(position);
        holder.imageView.setImageDrawable(currentView.getImage());
        holder.userTextView.setText(currentView.getUsername());
        holder.messageTextView.setText(currentView.getMessage());
        holder.timeTextView.setText(currentView.getTime());
        holder.newMessageTextView.setText(currentView.getNewMessage());
        if (Integer.parseInt(currentView.getNewMessage()) < 1) {
            holder.newMessageTextView.setVisibility(View.GONE);
        } else {
            holder.newMessageTextView.setVisibility(View.VISIBLE);
        }
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
        return recycleViewData == null ? 0 : recycleViewData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView userTextView;
        TextView messageTextView;
        TextView timeTextView;
        TextView newMessageTextView;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.conversationlistitem_image);
            userTextView = itemView.findViewById(R.id.conversationlistitem_user);
            messageTextView = itemView.findViewById(R.id.conversationlistitem_message);
            timeTextView = itemView.findViewById(R.id.conversationlistitem_time);
            newMessageTextView = itemView.findViewById(R.id.conversationlistitem_newmessage);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int uId);
    }
}
