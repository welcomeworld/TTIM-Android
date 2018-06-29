package cn.dmandp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.protobuf.Message;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.dmandp.common.OprateOptions;
import cn.dmandp.common.TYPE;
import cn.dmandp.context.TtApplication;
import cn.dmandp.entity.ChatMessage;
import cn.dmandp.entity.TTIMPacket;
import cn.dmandp.entity.TTMessage;
import cn.dmandp.tt.R;

/**
 * Created by 萌即正义 on 16/03/2018.
 */

public class MessageAdapter extends ArrayAdapter<ChatMessage> {
    private int resourceId;
    private OnItemClickListener onItemClickListener;

    public MessageAdapter(@NonNull Context context, int resource, @NonNull List objects) {
        super(context, resource, objects);
        this.resourceId = resource;
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, ChatMessage message);
    }

    public void setOnItemClickListener(MessageAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ChatMessage chatMessage = getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            viewHolder = new ViewHolder();
            viewHolder.leftLayout = (LinearLayout) view.findViewById(R.id.left_message_layout);
            viewHolder.lefttouxiang = (ImageView) view.findViewById(R.id.touxiang_left);
            viewHolder.leftcontent = (TextView) view.findViewById(R.id.message_content_left);
            viewHolder.leftusername = (TextView) view.findViewById(R.id.message_username_left);
            viewHolder.time = (TextView) view.findViewById(R.id.message_time);
            viewHolder.rightLayout = (LinearLayout) view.findViewById(R.id.right_message_layout);
            viewHolder.righttouxiang = (ImageView) view.findViewById(R.id.touxiang_right);
            viewHolder.rightcontent = (TextView) view.findViewById(R.id.message_content_right);
            viewHolder.rightusername = (TextView) view.findViewById(R.id.message_username_right);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.CHINA);
        if (chatMessage.getType() == 0) {
            viewHolder.leftLayout.setVisibility(View.GONE);
            viewHolder.rightLayout.setVisibility(View.VISIBLE);
            viewHolder.righttouxiang.setImageDrawable(chatMessage.getTouxiang());
            viewHolder.time.setText(format.format(new Date(chatMessage.getTime())));
            viewHolder.rightusername.setText(chatMessage.getName());
            viewHolder.rightcontent.setText(chatMessage.getMessage());
        } else {
            viewHolder.leftLayout.setVisibility(View.VISIBLE);
            viewHolder.rightLayout.setVisibility(View.GONE);
            viewHolder.lefttouxiang.setImageDrawable(chatMessage.getTouxiang());
            viewHolder.time.setText(format.format(new Date(chatMessage.getTime())));
            viewHolder.leftusername.setText(chatMessage.getName());
            viewHolder.leftcontent.setText(chatMessage.getMessage());
        }
        if (onItemClickListener != null) {
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onItemClickListener.onItemClick(v, chatMessage);
                    return true;
                }
            });
        }
        return view;
    }

    class ViewHolder {
        LinearLayout leftLayout;
        LinearLayout rightLayout;
        ImageView lefttouxiang;
        ImageView righttouxiang;
        TextView leftusername;
        TextView rightusername;
        TextView leftcontent;
        TextView rightcontent;
        TextView time;
    }
}
