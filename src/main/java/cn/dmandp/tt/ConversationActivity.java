package cn.dmandp.tt;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.dmandp.adapter.MessageAdapter;
import cn.dmandp.common.OprateOptions;
import cn.dmandp.common.TYPE;
import cn.dmandp.context.TtApplication;
import cn.dmandp.dao.TTIMDaoHelper;
import cn.dmandp.entity.ChatMessage;
import cn.dmandp.entity.TTIMPacket;
import cn.dmandp.entity.TTMessage;

/**
 * Created by 萌即正义 on 15/03/2018.
 */

public class ConversationActivity extends BaseActivity implements View.OnClickListener {
    public List<ChatMessage> getMessages() {
        return messages;
    }

    List<ChatMessage> messages = new ArrayList<ChatMessage>();
    EditText messagetext;
    Button send;
    private TTIMDaoHelper daoHelper = new TTIMDaoHelper(this);
    private SQLiteDatabase database;

    public MessageAdapter getAdapter() {
        return adapter;
    }

    MessageAdapter adapter;
    private int chatUserId;
    private int currentUserId;
    private String currentUserName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        database = daoHelper.getReadableDatabase();
        SharedPreferences data = getSharedPreferences("data", MODE_PRIVATE);
        currentUserId = data.getInt("currentUserId", -1);
        currentUserName = data.getString("currentUserName", "未登录");
        Bundle bundle = getIntent().getExtras();
        chatUserId = bundle.getInt("uId");
        send = (Button) findViewById(R.id.conversation_send);
        messagetext = (EditText) findViewById(R.id.conversation_messagetext);
        send.setOnClickListener(this);
        ListView messagelistview = (ListView) findViewById(R.id.conversation_listview);
        datainit();
        adapter = new MessageAdapter(ConversationActivity.this, R.layout.listview_message, messages);
        messagelistview.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.conversation_send:
                TTIMPacket messagePacket = new TTIMPacket();
                messagePacket.setTYPE(TYPE.SEND_REQ);
                TTMessage.Builder builder = TTMessage.newBuilder();
                builder.setMContent(messagetext.getText().toString());
                builder.setMFromId(currentUserId);
                builder.setMToId(chatUserId);
                builder.setMTime(System.currentTimeMillis());
                TTMessage message = builder.build();
                messagePacket.setBodylength(message.toByteArray().length);
                messagePacket.setBody(message.toByteArray());
                TtApplication.send(messagePacket);
                ChatMessage newMessage = new ChatMessage(R.drawable.ty, "暂定", currentUserName, messagetext.getText() + "", 200L, 0);
                messagetext.setText("");
                messages.add(newMessage);
                adapter.notifyDataSetChanged();
            default:
                break;
        }
    }

    public void datainit() {
        Cursor message = database.rawQuery("select * from messages where Fromid=? and Toid=? order by Mtime asc limit 20", new String[]{chatUserId + "", currentUserId + ""});
        while (message.moveToNext()) {
            Bitmap photo = BitmapFactory.decodeFile(getFilesDir() + "/head_portrait/" + chatUserId + ".png");
            if (photo == null) {
                photo = BitmapFactory.decodeResource(getResources(), R.drawable.ty);
                Log.e(TAG, "do not have file" + chatUserId + ".png");
            }
            SimpleDateFormat format = new SimpleDateFormat("HH:MM");
            String mcontent = message.getString(message.getColumnIndex("mcontent"));
            Long mtime = message.getLong(message.getColumnIndex("Mtime"));
            messages.add(new ChatMessage(R.drawable.ty, "暂定", chatUserId + "", mcontent, mtime, 0));
        }
    }
}
