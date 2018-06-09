package cn.dmandp.tt;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import cn.dmandp.common.MyDividerItemDecoration;
import cn.dmandp.common.TYPE;
import cn.dmandp.entity.ConversationListItem;
import cn.dmandp.adapter.ConversationListItemAdapter;
import cn.dmandp.context.SessionContext;
import cn.dmandp.context.TtApplication;
import cn.dmandp.entity.TTUser;
import cn.dmandp.netio.Result;
import cn.dmandp.service.MessageService;
import cn.dmandp.view.LoadView;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private SessionContext sessionContext;
    private List<ConversationListItem> conversationList = new ArrayList<ConversationListItem>();
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private RecyclerView recyclerView;
    LoadView loadView;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //start service
        Intent intent = new Intent(this, MessageService.class);
        startService(intent);
        //Login verify
        sessionContext = ((TtApplication) getApplication()).getSessionContext();
        if (!sessionContext.isLogin()) {
            SharedPreferences data = getSharedPreferences("data", MODE_PRIVATE);
            int currentUserId = data.getInt("currentUserId", -1);
            if (currentUserId == -1) {
                //SharedPreferences save nothing so go to LoginActivity
                intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                return;
            } else {
                //SharedPreferences have id and password so login directly
                String currentUserPassword = data.getString("currentUserPassword", "NULL");
                new MainTask().execute(currentUserId + "", currentUserPassword);
            }
        }
        //View initialization
        loadView = findViewById(R.id.main_loadview);

        drawerLayout = findViewById(R.id.main_drawerlayout);
        //------toolbar initialization start
        toolbar = findViewById(R.id.main_toolbar);
        toolbar.inflateMenu(R.menu.action_menu_main);
        //set toolbar item clickListener
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
        //display toolbar item icon
        ((MenuBuilder) toolbar.getMenu()).setOptionalIconsVisible(true);
        //-----toolbar initialization end
        drawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer) {
        };
        drawerLayout.addDrawerListener(drawerToggle);
        //drawerLayout associate with toolbar
        drawerToggle.syncState();
        //-----NavigationView initialization start
        NavigationView navigationView = findViewById(R.id.main_navigation);
        //set itemIcon color
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_menu_item_about:
                        break;
                    case R.id.navigation_menu_item_logout:
                        Toast.makeText(MainActivity.this, "your click the " + item.getTitle(), Toast.LENGTH_SHORT).show();
                        break;
                }
                return false;
            }
        });
        //-----NavigationView initialization end
        //recyclerView initialization
        recyclerView = findViewById(R.id.main_recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(linearLayoutManager);
        linearLayoutManager.setOrientation(OrientationHelper.VERTICAL);
        Bitmap photo = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/photo.png");
        if (photo == null) {
            Log.e("MainActivity", "do not have file");
        }
        ConversationListItem user1 = new ConversationListItem(1, "test", "test data", "22:00", "99", photo);
        conversationList.add(user1);
        ConversationListItem user2 = new ConversationListItem(1, "test", "test data", "22:00", "99", photo);
        conversationList.add(user2);
        ConversationListItem user3 = new ConversationListItem(1, "test", "test data", "22:00", "99", photo);
        conversationList.add(user3);
        ConversationListItem user4 = new ConversationListItem(1, "test", "test data", "22:00", "99", photo);
        conversationList.add(user4);
        ConversationListItemAdapter conversationListItemAdapter = new ConversationListItemAdapter(conversationList);
        conversationListItemAdapter.setOnItemClickListener(new ConversationListItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int uId) {
                Intent intent = new Intent(MainActivity.this, ConversationActivity.class);
                intent.putExtra("uId", uId);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(conversationListItemAdapter);
        recyclerView.addItemDecoration(new MyDividerItemDecoration());
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void onClick(View v) {

    }

    class MainTask extends AsyncTask<String, String, Result> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Result doInBackground(String... strings) {
            while (!sessionContext.isLogin()) {
                int uId;
                try {
                    uId = Integer.parseInt(strings[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
                String uPassword = strings[1];
                SocketChannel socketChannel = sessionContext.getSocketChannel();
                if (socketChannel == null) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                try {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(514);
                    byteBuffer.put(TYPE.LOGIN_REQ);
                    TTUser.Builder builder = TTUser.newBuilder();
                    builder.setUId(uId);
                    builder.setUPassword(uPassword);
                    TTUser loginuser = builder.build();
                    byte[] body = loginuser.toByteArray();
                    byteBuffer.putInt(body.length);
                    byteBuffer.put(body);
                    byteBuffer.flip();
                    while (byteBuffer.hasRemaining()) {
                        socketChannel.write(byteBuffer);
                    }
                    sessionContext.setAttribute("loginpassword", uPassword);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Result result) {
        }
    }

    public void dataInit() {

    }

}
