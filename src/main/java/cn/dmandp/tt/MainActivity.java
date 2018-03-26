package cn.dmandp.tt;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import cn.dmandp.common.ConversationListItem;
import cn.dmandp.common.ConversationListItemAdapter;

public class MainActivity extends AppCompatActivity {
    private List<ConversationListItem> conversationList = new ArrayList<ConversationListItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        ConversationListItemAdapter adapter = new ConversationListItemAdapter(MainActivity.this, R.layout.listview_conversation, conversationList);
        ListView listView = (ListView) findViewById(R.id.main_listview);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, ConversationActivity.class);
                startActivity(intent);
            }
        });
    }

    private void init() {
        ConversationListItem user1 = new ConversationListItem("杨泽雄", "这是测试数据", "22:00", "6", R.drawable.logo);
        conversationList.add(user1);
        ConversationListItem user2 = new ConversationListItem("杨泽雄", "这是测试数据", "22:00", "6", R.drawable.logo);
        conversationList.add(user2);
        ConversationListItem user3 = new ConversationListItem("杨泽雄", "这是测试数据", "22:00", "6", R.drawable.logo);
        conversationList.add(user3);
        ConversationListItem user4 = new ConversationListItem("杨泽雄", "这是测试数据", "22:00", "6", R.drawable.logo);
        conversationList.add(user4);
        ConversationListItem user5 = new ConversationListItem("杨泽雄", "这是测试数据", "22:00", "6", R.drawable.logo);
        conversationList.add(user5);
        ConversationListItem user6 = new ConversationListItem("杨泽雄", "这是测试数据", "22:00", "6", R.drawable.logo);
        conversationList.add(user6);
        ConversationListItem user7 = new ConversationListItem("杨泽雄", "这是测试数据", "22:00", "6", R.drawable.logo);
        conversationList.add(user7);
        ConversationListItem user8 = new ConversationListItem("杨泽雄", "这是测试数据", "22:00", "6", R.drawable.logo);
        conversationList.add(user8);
    }
}
