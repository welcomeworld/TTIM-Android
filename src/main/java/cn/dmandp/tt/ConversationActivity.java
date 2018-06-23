package cn.dmandp.tt;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.dmandp.adapter.MessageAdapter;
import cn.dmandp.common.TYPE;
import cn.dmandp.context.SessionContext;
import cn.dmandp.context.TtApplication;
import cn.dmandp.dao.TTIMDaoHelper;
import cn.dmandp.entity.ChatMessage;
import cn.dmandp.entity.ConversationListItem;
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

    public Map<Integer, List<ChatMessage>> getAllMessages() {
        return allMessages;
    }

    private Map<Integer, List<ChatMessage>> allMessages = new HashMap<Integer, List<ChatMessage>>();

    public int getChatUserId() {
        return chatUserId;
    }

    private int chatUserId;
    private int currentUserId;
    private String currentUserName;

    public String getChatUserName() {
        return chatUserName;
    }

    public void setChatUserName(String chatUserName) {
        this.chatUserName = chatUserName;
    }

    private String chatUserName;

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
        Cursor friendCursor = database.rawQuery("select * from friends where uid=? and friendid=?", new String[]{currentUserId + "", chatUserId + ""});
        if (friendCursor.moveToNext()) {
            chatUserName = friendCursor.getString(friendCursor.getColumnIndex("Uname"));
        } else {
            chatUserName = "未知";
        }
        friendCursor.close();
        //select messages list
        if (allMessages.get(chatUserId) != null) {
            messages = allMessages.get(chatUserId);
        } else {
            messages = new ArrayList<ChatMessage>();
            allMessages.put(chatUserId, messages);
        }

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
                Bitmap userPhoto = BitmapFactory.decodeFile(getFilesDir() + "/head_portrait/" + currentUserId + ".png");
                if (userPhoto == null) {
                    userPhoto = BitmapFactory.decodeResource(getResources(), R.drawable.ty);
                }
                RoundedBitmapDrawable userRoundedBitmapDrawable = RoundedBitmapDrawableFactory.create(null, userPhoto);
                userRoundedBitmapDrawable.setCircular(true);
                SimpleDateFormat format = new SimpleDateFormat("HH:MM");
                ChatMessage newMessage = new ChatMessage(userRoundedBitmapDrawable, currentUserName, messagetext.getText() + "", format.format(new Date(message.getMTime())), 0);
                messagetext.setText("");
                messages.add(newMessage);
                adapter.notifyDataSetChanged();
                MainActivity mainActivity = (MainActivity) SessionContext.activities.get("MainActivity");
                if (mainActivity != null) {
                    List<ConversationListItem> conversationList = mainActivity.getConversationList();
                    for (int i = 0; i < conversationList.size(); i++) {
                        if (conversationList.get(i).getUId() == message.getMToId()) {
                            conversationList.get(i).setMessage(message.getMContent());
                            conversationList.get(i).setTime(format.format(new Date(message.getMTime())));
                            mainActivity.getConversationListItemAdapter().notifyItemChanged(i);
                            break;
                        }
                    }
                }
                try {
                    database.execSQL("insert into messages values(?,?,?,?)", new Object[]{message.getMContent(), message.getMTime(), message.getMFromId(), message.getMToId()});
                } catch (Exception e) {
                }
            default:
                break;
        }
    }

    public void datainit() {
        Cursor message = database.rawQuery("select * from messages where (Fromid=? and Toid=?) or (Fromid=? and Toid=?)order by Mtime asc", new String[]{chatUserId + "", currentUserId + "", currentUserId + "", chatUserId + ""});
        while (message.moveToNext()) {
            Bitmap photo = BitmapFactory.decodeFile(getFilesDir() + "/head_portrait/" + chatUserId + ".png");
            Bitmap userPhoto = BitmapFactory.decodeFile(getFilesDir() + "/head_portrait/" + currentUserId + ".png");
            if (photo == null) {
                photo = BitmapFactory.decodeResource(getResources(), R.drawable.ty);
            }
            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(null, photo);
            roundedBitmapDrawable.setCircular(true);
            if (userPhoto == null) {
                userPhoto = BitmapFactory.decodeResource(getResources(), R.drawable.ty);
            }
            RoundedBitmapDrawable userRoundedBitmapDrawable = RoundedBitmapDrawableFactory.create(null, userPhoto);
            userRoundedBitmapDrawable.setCircular(true);
            SimpleDateFormat format = new SimpleDateFormat("HH:MM");
            String mcontent = message.getString(message.getColumnIndex("mcontent"));
            Long mtime = message.getLong(message.getColumnIndex("Mtime"));
            int fromid = message.getInt(message.getColumnIndex("Fromid"));
            if (fromid == currentUserId) {
                messages.add(new ChatMessage(userRoundedBitmapDrawable, currentUserName + "", mcontent, format.format(new Date(mtime)), 0));
            } else {
                messages.add(new ChatMessage(roundedBitmapDrawable, chatUserName + "", mcontent, format.format(new Date(mtime)), 1));
            }
        }
        message.close();
    }
}
