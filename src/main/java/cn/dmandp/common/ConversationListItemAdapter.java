package cn.dmandp.common;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.dmandp.tt.R;


/**
 * Created by 萌即正义 on 14/03/2018.
 */

public class ConversationListItemAdapter extends ArrayAdapter<ConversationListItem> {
    private int resourceid;

    public ConversationListItemAdapter(@NonNull Context context, int resource, @NonNull List<ConversationListItem> objects) {
        super(context, resource, objects);
        resourceid = resource;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ConversationListItem conversationListItem = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceid, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.conversationlistitem_image);
        TextView userTextView = (TextView) view.findViewById(R.id.conversationlistitem_user);
        TextView messageTextView = (TextView) view.findViewById(R.id.conversationlistitem_message);
        TextView timeTextView = (TextView) view.findViewById(R.id.conversationlistitem_time);
        TextView newMessageTextView = (TextView) view.findViewById(R.id.conversationlistitem_newmessage);
        imageView.setImageResource(conversationListItem.getImageId());
        userTextView.setText(conversationListItem.getUsername());
        messageTextView.setText(conversationListItem.getMessage());
        timeTextView.setText(conversationListItem.getTime());
        newMessageTextView.setText(conversationListItem.getNewMessage());
        newMessageTextView.setVisibility(View.GONE);
        return view;
    }
}
