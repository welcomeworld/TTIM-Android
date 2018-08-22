package cn.dmandp.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.dmandp.entity.ChatMessage;
import cn.dmandp.tt.PersonInfoActivity;
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
    public View getView(int position, View convertView, final ViewGroup parent) {
        final ChatMessage chatMessage = getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            viewHolder = new ViewHolder();
            viewHolder.leftLayout =  view.findViewById(R.id.left_message_layout);
            viewHolder.lefttouxiang =  view.findViewById(R.id.touxiang_left);
            viewHolder.leftcontent =  view.findViewById(R.id.message_content_left);
            viewHolder.leftusername =  view.findViewById(R.id.message_username_left);
            viewHolder.time =  view.findViewById(R.id.message_time);
            viewHolder.rightLayout =  view.findViewById(R.id.right_message_layout);
            viewHolder.righttouxiang = view.findViewById(R.id.touxiang_right);
            viewHolder.rightcontent =  view.findViewById(R.id.message_content_right);
            viewHolder.rightusername =  view.findViewById(R.id.message_username_right);
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
            viewHolder.righttouxiang.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent personInfoIntent=new Intent(parent.getContext(), PersonInfoActivity.class);
                    personInfoIntent.putExtra("uId", chatMessage.getUid());
                    personInfoIntent.putExtra("uName", chatMessage.getName());
                    parent.getContext().startActivity(personInfoIntent);
                }
            });
            viewHolder.rightusername.setText(chatMessage.getName());
            viewHolder.rightcontent.setText(chatMessage.getMessage());
        } else {
            viewHolder.leftLayout.setVisibility(View.VISIBLE);
            viewHolder.rightLayout.setVisibility(View.GONE);
            viewHolder.lefttouxiang.setImageDrawable(chatMessage.getTouxiang());
            viewHolder.leftusername.setText(chatMessage.getName());
            viewHolder.leftcontent.setText(chatMessage.getMessage());
            viewHolder.lefttouxiang.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent personInfoIntent=new Intent(parent.getContext(), PersonInfoActivity.class);
                    personInfoIntent.putExtra("uId", chatMessage.getUid());
                    personInfoIntent.putExtra("uName", chatMessage.getName());
                    parent.getContext().startActivity(personInfoIntent);
                }
            });
        }
            viewHolder.time.setText(format.format(new Date(chatMessage.getTime())));
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
        ImageButton lefttouxiang;
        ImageButton righttouxiang;
        TextView leftusername;
        TextView rightusername;
        TextView leftcontent;
        TextView rightcontent;
        TextView time;
    }
}
