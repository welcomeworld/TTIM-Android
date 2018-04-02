package cn.dmandp.tt;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.dmandp.entity.ConversationListItem;
import cn.dmandp.adapter.ConversationListItemAdapter;
import cn.dmandp.context.SessionContext;
import cn.dmandp.context.TtApplication;
import cn.dmandp.netio.Result;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private List<ConversationListItem> conversationList = new ArrayList<ConversationListItem>();
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new MainTask().execute();

    }

    @Override
    public void onClick(View v) {

    }

    class MainTask extends AsyncTask<String, String, Result> {

        @Override
        protected Result doInBackground(String... strings) {
            Result result = new Result();
            SessionContext sessionContext = ((TtApplication) getApplication()).getSessionContext();
            if (!sessionContext.isLogin()) {
                SharedPreferences data = getSharedPreferences("data", MODE_PRIVATE);
                int currentUserId = data.getInt("currentUserId", -1);
                if (currentUserId == -1) {
                    result.setResultStatus((byte) 0);
                    Log.e("MainActivity", "没有登录用户" + currentUserId);
                } else {
                    String currentUserPassword = data.getString("currentUserPassword", "NULL");
                    Log.e("MainActivity", currentUserId + ":" + currentUserPassword);
                    sessionContext.setuID(currentUserId);
                    //sessionContext.setBindUser();
                    result.setResultStatus((byte) 1);
                }
            } else {
                result.setResultStatus((byte) 1);
            }
            return result;
        }

        @SuppressLint("RestrictedApi")
        @Override
        protected void onPostExecute(Result result) {
            if (result.getResultStatus() == 0) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                drawerLayout = findViewById(R.id.main_drawerlayout);
                toolbar = findViewById(R.id.main_toolbar);
                toolbar.inflateMenu(R.menu.action_menu_main);
                toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_main_add:
                                Toast.makeText(MainActivity.this, "your click the add friends!", Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.menu_main_scan:
                                Toast.makeText(MainActivity.this, "your click the scan!", Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });
                ((MenuBuilder) toolbar.getMenu()).setOptionalIconsVisible(true);
                drawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer) {

                };
                drawerLayout.addDrawerListener(drawerToggle);
                drawerToggle.syncState();
                NavigationView navigationView = findViewById(R.id.main_navigation);
                navigationView.setItemIconTintList(null);
                recyclerView = findViewById(R.id.main_recyclerview);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
                recyclerView.setLayoutManager(linearLayoutManager);
                linearLayoutManager.setOrientation(OrientationHelper.VERTICAL);
                Bitmap photo = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/photo.png");
                if (photo == null) {
                    Log.e("MainActivity", "do not have file");
                }
                ConversationListItem user1 = new ConversationListItem("杨泽雄", "这是测试数据", "22:00", "6", photo);
                conversationList.add(user1);
                ConversationListItem user2 = new ConversationListItem("杨泽雄", "这是测试数据", "22:00", "6", photo);
                conversationList.add(user2);
                ConversationListItem user3 = new ConversationListItem("杨泽雄", "这是测试数据", "22:00", "6", photo);
                conversationList.add(user3);
                ConversationListItem user4 = new ConversationListItem("杨泽雄", "这是测试数据", "22:00", "6", photo);
                conversationList.add(user4);
                ConversationListItem user5 = new ConversationListItem("杨泽雄", "这是测试数据", "22:00", "6", photo);
                conversationList.add(user5);
                ConversationListItem user6 = new ConversationListItem("杨泽雄", "这是测试数据", "22:00", "6", photo);
                conversationList.add(user6);
                ConversationListItem user7 = new ConversationListItem("杨泽雄", "这是测试数据", "22:00", "6", photo);
                conversationList.add(user7);
                ConversationListItem user8 = new ConversationListItem("杨泽雄", "这是测试数据", "22:00", "6", photo);
                conversationList.add(user8);
                ConversationListItem user9 = new ConversationListItem("杨泽雄", "这是测试数据", "22:00", "6", photo);
                conversationList.add(user9);
                ConversationListItem user10 = new ConversationListItem("杨泽雄", "这是测试数据", "22:00", "6", photo);
                conversationList.add(user10);
                ConversationListItem user11 = new ConversationListItem("杨泽雄", "这是测试数据", "22:00", "6", photo);
                conversationList.add(user11);
                ConversationListItem user12 = new ConversationListItem("杨泽雄", "这是测试数据", "22:00", "6", photo);
                conversationList.add(user12);
                ConversationListItem user13 = new ConversationListItem("杨泽雄", "这是测试数据", "22:00", "6", photo);
                conversationList.add(user13);
                ConversationListItem user14 = new ConversationListItem("杨泽雄", "这是测试数据", "22:00", "6", photo);
                conversationList.add(user14);
                ConversationListItem user15 = new ConversationListItem("杨泽雄", "这是测试数据", "22:00", "6", photo);
                conversationList.add(user15);
                ConversationListItem user16 = new ConversationListItem("杨泽雄", "这是测试数据", "22:00", "6", photo);
                conversationList.add(user16);
                ConversationListItem user17 = new ConversationListItem("杨泽雄", "这是测试数据", "22:00", "6", photo);
                conversationList.add(user17);
                ConversationListItemAdapter conversationListItemAdapter = new ConversationListItemAdapter(conversationList);
                recyclerView.setAdapter(conversationListItemAdapter);
                //recyclerView.addItemDecoration(new DividerGridItemDecoration(this));
                //recyclerView.setItemAnimator( new DefaultItemAnimator());
            }
        }
    }
}
