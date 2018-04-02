package cn.dmandp.tt;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import cn.dmandp.adapter.MessageAdapter;
import cn.dmandp.entity.ChatMessage;

/**
 * Created by 萌即正义 on 15/03/2018.
 */

public class ConversationActivity extends Activity implements View.OnClickListener {
    List<ChatMessage> messages = new ArrayList<ChatMessage>();
    EditText messagetext;
    Button send;
    MessageAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        send = (Button) findViewById(R.id.conversation_send);
        messagetext = (EditText) findViewById(R.id.conversation_messagetext);
        send.setOnClickListener(this);
        ListView messagelistview = (ListView) findViewById(R.id.conversation_listview);
        adapter = new MessageAdapter(ConversationActivity.this, R.layout.listview_message, messages);
        messagelistview.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.conversation_send:
                ChatMessage newMessage = new ChatMessage(R.drawable.ty, "暂定", "二流子", messagetext.getText() + "", 200L, 0);
                messages.add(newMessage);
                adapter.notifyDataSetChanged();
            default:
                break;
        }
    }
}
