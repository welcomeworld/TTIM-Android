/*
 * Copyright (c) 2018. welcomeworld All rights reserved
 */

package cn.dmandp.adapter;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.dmandp.common.OprateOptions;
import cn.dmandp.common.TYPE;
import cn.dmandp.context.TtApplication;
import cn.dmandp.dao.TTIMDaoHelper;
import cn.dmandp.entity.NewFriendRecyclerViewItem;
import cn.dmandp.entity.TTIMPacket;
import cn.dmandp.entity.TTMessage;
import cn.dmandp.service.MessageService;
import cn.dmandp.tt.R;

public class NewFriendRecyclerViewItemAdapter extends RecyclerView.Adapter {
    TTIMDaoHelper daoHelper = new TTIMDaoHelper(MessageService.getInstance());
    SQLiteDatabase database;
    ArrayList<NewFriendRecyclerViewItem> data;
    public NewFriendRecyclerViewItemAdapter(List<NewFriendRecyclerViewItem> data){
        this.data= (ArrayList<NewFriendRecyclerViewItem>) data;
        database=daoHelper.getReadableDatabase();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_newfriend,parent,false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView timeText;
        ImageView photoView;
        TextView nameText;
        TextView contentText;
        Spinner spinner;
        TextView statusText;
        public ViewHolder(View itemView) {
            super(itemView);
            timeText=itemView.findViewById(R.id.recyclerview_newfriend_time);
            photoView=itemView.findViewById(R.id.recyclerview_newfriend_image);
            nameText=itemView.findViewById(R.id.recyclerview_newfriend_name);
            contentText=itemView.findViewById(R.id.recyclerview_newfriend_content);
            spinner=itemView.findViewById(R.id.recyclerview_newfriend_spinner);
            statusText=itemView.findViewById(R.id.recyclerview_newfriend_status);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final ViewHolder viewHolder= (ViewHolder) holder;
        final NewFriendRecyclerViewItem item=data.get(position);
        viewHolder.contentText.setText(item.getMessage());
        viewHolder.nameText.setText(item.getUId()+"");
        SimpleDateFormat format = new SimpleDateFormat("MM月dd号 HH:mm", Locale.CHINA);
        viewHolder.timeText.setText(format.format(new Date(item.getTime())));
        if(item.getStatus()==0){
            viewHolder.spinner.setVisibility(View.VISIBLE);
            viewHolder.statusText.setVisibility(View.GONE);
            viewHolder.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                boolean first=true;
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if(first){
                        first=false;
                        return;
                    }
                    TTIMPacket joinPacket=new TTIMPacket();
                    ByteBuffer changeBuffer=ByteBuffer.allocate(20);
                    switch (position){
                        case 0:
                            changeBuffer.put(OprateOptions.ANSWER);
                            changeBuffer.putInt(item.getUId());
                            changeBuffer.put((byte)1);
                            changeBuffer.flip();
                            joinPacket.setTYPE(TYPE.JOIN_REQ);
                            joinPacket.setBodylength(changeBuffer.remaining());
                            joinPacket.setBody(changeBuffer.array());
                            TtApplication.send(joinPacket);
                            viewHolder.spinner.setVisibility(View.GONE);
                            viewHolder.statusText.setVisibility(View.VISIBLE);
                            viewHolder.statusText.setText("已同意");
                            ContentValues agreeContentValues=new ContentValues();
                            agreeContentValues.put("rstatus",1);
                            database.update("requests",agreeContentValues,"fromid=? and toid=? and rtime=?",new String[]{item.getUId()+"",TtApplication.getSessionContext().getuID()+"",item.getTime()+""});
                            //database.execSQL("update requests set rstatus=? where fromid=? and toid=? and rtime=?",new Object[]{1,item.getUId(),TtApplication.getSessionContext().getuID(),item.getTime()});
                            break;
                        case 1:
                            changeBuffer.put(OprateOptions.ANSWER);
                            changeBuffer.putInt(item.getUId());
                            changeBuffer.put((byte)2);
                            changeBuffer.flip();
                            joinPacket.setTYPE(TYPE.JOIN_REQ);
                            joinPacket.setBodylength(changeBuffer.remaining());
                            joinPacket.setBody(changeBuffer.array());
                            TtApplication.send(joinPacket);
                            viewHolder.spinner.setVisibility(View.GONE);
                            viewHolder.statusText.setVisibility(View.VISIBLE);
                            viewHolder.statusText.setText("已拒绝");
                            ContentValues rejectContentValues=new ContentValues();
                            rejectContentValues.put("rstatus",2);
                            database.update("requests",rejectContentValues,"fromid=? and toid=? and rtime=?",new String[]{item.getUId()+"",TtApplication.getSessionContext().getuID()+"",item.getTime()+""});
                            //database.execSQL("update requests set rstatus=? where fromid=? and toid=? and rtime=?",new Object[]{2,item.getUId(),TtApplication.getSessionContext().getuID(),item.getTime()});
                            break;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }else if(item.getStatus()==1){
            viewHolder.spinner.setVisibility(View.GONE);
            viewHolder.statusText.setVisibility(View.VISIBLE);
            viewHolder.statusText.setText("已同意");
        }else if(item.getStatus()==2){
            viewHolder.spinner.setVisibility(View.GONE);
            viewHolder.statusText.setVisibility(View.VISIBLE);
            viewHolder.statusText.setText("已拒绝");
        }else {
            viewHolder.spinner.setVisibility(View.GONE);
            viewHolder.statusText.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

}
