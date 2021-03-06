/*
 * Copyright (c) 2018. welcomeworld All rights reserved
 */

package cn.dmandp.tt;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.dmandp.adapter.FavoriteRecyclerViewItemAdapter;
import cn.dmandp.adapter.FriendRecyclerViewItemAdapter;
import cn.dmandp.adapter.HeaderAndFooterAdapter;
import cn.dmandp.adapter.MyViewPagerAdapter;
import cn.dmandp.common.MyDividerItemDecoration;
import cn.dmandp.common.OprateOptions;
import cn.dmandp.common.TYPE;
import cn.dmandp.dao.TTIMDaoHelper;
import cn.dmandp.entity.ConversationListItem;
import cn.dmandp.adapter.ConversationListItemAdapter;
import cn.dmandp.context.SessionContext;
import cn.dmandp.context.TtApplication;
import cn.dmandp.entity.FavoriteRecyclerViewItem;
import cn.dmandp.entity.FriendRecyclerViewItem;
import cn.dmandp.entity.TTIMPacket;
import cn.dmandp.entity.TTMessage;
import cn.dmandp.entity.TTUser;
import cn.dmandp.netio.FileThread;
import cn.dmandp.netio.Result;
import cn.dmandp.service.MessageService;
import cn.dmandp.utils.ThemeUtil;
import cn.dmandp.view.DefaultRefreshCreator;
import cn.dmandp.view.MyViewPager;
import cn.dmandp.view.RefreshRecyclerView;

public class MainActivity extends BaseActivity {
    public MainHandler handler = new MainHandler();
    HeaderAndFooterAdapter headerAndFooterAdapter;
    HeaderAndFooterAdapter refreshFriendAdapter;
    HeaderAndFooterAdapter refreshFavoriteAdapter;
    private SessionContext sessionContext;
    private TTIMDaoHelper daoHelper = new TTIMDaoHelper(this);
    private SQLiteDatabase database;
    private int currentUserId;
    private String currentUserName;
    public static final int STOP_MESSAGE_REFRESH = 1;
    public static final int STOP_FRIEND_REFRESH = 2;
    public static final int STOP_FAVORITE_REFRESH = 3;
    ArrayList<View> viewContainter = new ArrayList<>();
    private List<ConversationListItem> conversationList = new ArrayList<>();
    private ArrayList<FriendRecyclerViewItem> friendRecyclerViewData = new ArrayList<>();
    private ConversationListItemAdapter conversationListItemAdapter;
    private FriendRecyclerViewItemAdapter friendRecyclerViewItemAdapter;
    private FavoriteRecyclerViewItemAdapter favoriteRecyclerViewItemAdapter;
    private ArrayList<FavoriteRecyclerViewItem> favoriteRecyclerViewData = new ArrayList<>();
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private AppBarLayout appBarLayout;
    private RefreshRecyclerView recyclerView;
    private RefreshRecyclerView friendListView;
    private View newFriendView;
    private RefreshRecyclerView favoriteListView;
    private MyViewPager viewPager;
    private BottomNavigationView bottomNavigationView;
    private View headerView;
    NavigationView navigationView;
    public List<ConversationListItem> getConversationList() {
        return conversationList;
    }
    public ArrayList<FriendRecyclerViewItem> getFriendRecyclerViewData() {
        return friendRecyclerViewData;
    }
    public ConversationListItemAdapter getConversationListItemAdapter() {
        return conversationListItemAdapter;
    }
    public FriendRecyclerViewItemAdapter getFriendRecyclerViewItemAdapter() {
        return friendRecyclerViewItemAdapter;
    }
    public RefreshRecyclerView getRecyclerView() {
        return recyclerView;
    }
    public View getNewFriendView() {
        return newFriendView;
    }
    public FavoriteRecyclerViewItemAdapter getFavoriteRecyclerViewItemAdapter() {
        return favoriteRecyclerViewItemAdapter;
    }
    public ArrayList<FavoriteRecyclerViewItem> getFavoriteRecyclerViewData() {
        return favoriteRecyclerViewData;
    }
    public View getHeaderView() {
        return headerView;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        database = daoHelper.getReadableDatabase();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawerLayout = findViewById(R.id.main_drawerLayout);
        appBarLayout = findViewById(R.id.main_appBarLayout);
        toolbar = findViewById(R.id.main_toolbar);
        navigationView = findViewById(R.id.main_navigation);

        //Login verify
        sessionContext = TtApplication.getSessionContext();
        SharedPreferences data = getSharedPreferences("data", MODE_PRIVATE);
        currentUserId = data.getInt("currentUserId", -1);
        currentUserName = data.getString("currentUserName", "未登录");
        if (!sessionContext.isLogin()) {
            if (currentUserId == -1) {
                //SharedPreferences save nothing so go to LoginActivity
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                return;
            } else {
                //SharedPreferences have id and password so login directly
                String currentUserPassword = data.getString("currentUserPassword", "NULL");
                new MainTask().execute(currentUserId + "", currentUserPassword);
            }
        }
        toolbarAndNavigationInit();

       viewPagerInit();

        dataInit();
    }

    private void viewPagerInit() {
        //-----viewpager initialization start
        viewPager = findViewById(R.id.main_viewpager);
        viewPager.setOffscreenPageLimit(2);
        View messageView = LayoutInflater.from(this).inflate(R.layout.viewpager_main_message, viewPager,false);
        View friendView = LayoutInflater.from(this).inflate(R.layout.viewpager_main_friend, viewPager,false);
        View favoriteView = LayoutInflater.from(this).inflate(R.layout.viewpager_main_favorite, viewPager,false);
        viewContainter.add(messageView);
        viewContainter.add(friendView);
        viewContainter.add(favoriteView);
        MyViewPagerAdapter viewPagerAdapter = new MyViewPagerAdapter(viewContainter);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        //-----viewpager initialization end

        //message recyclerView initialization
        recyclerView = messageView.findViewById(R.id.viewpager_message_recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(linearLayoutManager);
        linearLayoutManager.setOrientation(OrientationHelper.VERTICAL);
        conversationListItemAdapter = new ConversationListItemAdapter(conversationList);
        conversationListItemAdapter.setOnItemClickListener(new ConversationListItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int uId) {
                Intent intent = new Intent(MainActivity.this, ConversationActivity.class);
                intent.putExtra("uId", uId);
                startActivity(intent);
                SharedPreferences messageSharedPreferences = getSharedPreferences("message", MODE_PRIVATE);
                SharedPreferences.Editor editor = messageSharedPreferences.edit();
                int messageCount = messageSharedPreferences.getInt(uId + ":" + currentUserId, 0);
                editor.putInt(uId + ":" + currentUserId, 0);
                editor.apply();
                if (messageCount != 0) {
                    for (ConversationListItem item : conversationList) {
                        if (item.getUId() == uId) {
                            item.setNewMessage("0");
                            conversationListItemAdapter.notifyItemChanged(conversationList.indexOf(item));
                            break;
                        }
                    }
                }
            }
        });
        headerAndFooterAdapter = new HeaderAndFooterAdapter(conversationListItemAdapter);
        recyclerView.setAdapter(headerAndFooterAdapter);
        recyclerView.addRefreshViewCreator(new DefaultRefreshCreator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration());
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setOnRefreshListener(new RefreshRecyclerView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                TTIMPacket packet = new TTIMPacket();
                packet.setTYPE(TYPE.RECEIVE_REQ);
                final byte[] receivebody = {1};
                packet.setBodylength(receivebody.length);
                packet.setBody(receivebody);
                TtApplication.send(packet);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (recyclerView.getmCurrentRefreshStatus() == recyclerView.REFRESH_STATUS_REFRESHING) {
                            Message msg = Message.obtain();
                            msg.what = STOP_MESSAGE_REFRESH;
                            handler.sendMessage(msg);
                            Log.d(TAG, "NOT MESSAGE REFRESH");
                        }
                    }
                }).start();
            }
        });
        //messageRecyclerView initialization end

        //friendRecyclerView initialization start
        friendListView = friendView.findViewById(R.id.viewpager_friend_recyclerview);
        LinearLayoutManager friendLinearLayoutManager = new LinearLayoutManager(MainActivity.this);
        friendListView.setLayoutManager(friendLinearLayoutManager);
        friendLinearLayoutManager.setOrientation(OrientationHelper.VERTICAL);
        friendRecyclerViewItemAdapter = new FriendRecyclerViewItemAdapter(friendRecyclerViewData);
        friendRecyclerViewItemAdapter.setOnItemClickListener(new FriendRecyclerViewItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, FriendRecyclerViewItem currentView) {
                if (view.getId() == R.id.recyclerview_friend_layout) {
                    Intent personInfoIntent = new Intent(MainActivity.this, PersonInfoActivity.class);
                    personInfoIntent.putExtra("uId", currentView.getUId());
                    personInfoIntent.putExtra("uName", currentView.getUsername());
                    startActivity(personInfoIntent);
                } else if (R.id.recyclerview_friend_action == view.getId()) {
                    int uId = currentView.getUId();
                    Intent intent = new Intent(MainActivity.this, ConversationActivity.class);
                    intent.putExtra("uId", uId);
                    startActivity(intent);
                    SharedPreferences messageSharedPreferences = getSharedPreferences("message", MODE_PRIVATE);
                    SharedPreferences.Editor editor = messageSharedPreferences.edit();
                    int messageCount = messageSharedPreferences.getInt(uId + ":" + currentUserId, -1);
                    editor.putInt(uId + ":" + currentUserId, 0);
                    editor.apply();
                    if (messageCount != -1) {
                        for (ConversationListItem item : conversationList) {
                            if (item.getUId() == uId) {
                                item.setNewMessage("0");
                                conversationListItemAdapter.notifyItemChanged(conversationList.indexOf(item));
                                break;
                            }
                        }
                    } else {
                        //new conversationListItem
                        Cursor cursor = database.rawQuery("select * from friends where uid=? and friendid=?", new String[]{currentUserId + "", uId + ""});
                        if (cursor.moveToNext()) {
                            Bitmap photo = BitmapFactory.decodeFile(getFilesDir() + "/head_portrait/" + uId + ".png");
                            if (photo == null) {
                                photo = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
                                Bundle fileBundle = new Bundle();
                                fileBundle.putInt("uid",uId);
                                fileBundle.putByte("type", TYPE.USERPHOTO_GET_REQ);
                                if(MessageService.getInstance()!=null){
                                    new FileThread(MainActivity.this, fileBundle, MessageService.getInstance().getHandler()).start();
                                }
                                Log.d("MainActivity", "do not have file" + uId + ".png");
                            }
                            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), photo);
                            roundedBitmapDrawable.setCircular(true);
                            SimpleDateFormat format = new SimpleDateFormat("HH:mm",Locale.CHINA);
                            conversationList.add(0, new ConversationListItem(uId, cursor.getString(cursor.getColumnIndex("Uname")), "", format.format(new Date(System.currentTimeMillis())), 0 + "", roundedBitmapDrawable));
                            conversationListItemAdapter.notifyItemInserted(0);
                        }
                        cursor.close();
                    }
                }
            }
        });
        refreshFriendAdapter = new HeaderAndFooterAdapter(friendRecyclerViewItemAdapter);
        friendListView.setAdapter(refreshFriendAdapter);
        View friendRefreshView = LayoutInflater.from(this).inflate(R.layout.recyclerview_refresh, friendListView, false);
        refreshFriendAdapter.addHeaderView(friendRefreshView);
        newFriendView = LayoutInflater.from(this).inflate(R.layout.recyclerview_friend_header, friendListView, false);
        SharedPreferences messageSharedPreferences = getSharedPreferences("message", MODE_PRIVATE);
        final SharedPreferences.Editor editor = messageSharedPreferences.edit();
        int newFriendCount = messageSharedPreferences.getInt("newfriend" + currentUserId, 0);
        if(newFriendCount==0){
            newFriendView.findViewById(R.id.friend_header_notification).setVisibility(View.GONE);
        }else{
            newFriendView.findViewById(R.id.friend_header_notification).setVisibility(View.VISIBLE);
        }
        newFriendView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newFriendIntent = new Intent(MainActivity.this, NewFriendActivity.class);
                startActivity(newFriendIntent);
                findViewById(R.id.friend_header_notification).setVisibility(View.GONE);
                editor.putInt("newfriend" + currentUserId,0);
                editor.apply();
            }
        });
        refreshFriendAdapter.addHeaderView(newFriendView);
        friendListView.addItemDecoration(new MyDividerItemDecoration());
        friendListView.setItemAnimator(new DefaultItemAnimator());
        friendListView.addRefreshViewCreator(new DefaultRefreshCreator());
        friendListView.setOnRefreshListener(new RefreshRecyclerView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                TTIMPacket friendpacket = new TTIMPacket();
                friendpacket.setTYPE(TYPE.FRIENDS_REQ);
                ByteBuffer friendByteBuffer = ByteBuffer.allocate(50);
                friendByteBuffer.put(OprateOptions.GET);
                friendByteBuffer.flip();
                friendpacket.setBodylength(friendByteBuffer.remaining());
                friendpacket.setBody(friendByteBuffer.array());
                TtApplication.send(friendpacket);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (friendListView.getmCurrentRefreshStatus() == friendListView.REFRESH_STATUS_REFRESHING) {
                            Message msg = Message.obtain();
                            msg.what = STOP_FRIEND_REFRESH;
                            handler.sendMessage(msg);
                            Log.d(TAG, "NOT Friend REFRESH");
                        }
                    }
                }).start();
            }
        });

        //friendRecyclerView initialization end
        favoriteListView = favoriteView.findViewById(R.id.viewpager_favorite_recyclerview);
        LinearLayoutManager favoriteListViewLayoutManager = new LinearLayoutManager(MainActivity.this);
        favoriteListViewLayoutManager.setOrientation(OrientationHelper.VERTICAL);
        favoriteListView.setLayoutManager(favoriteListViewLayoutManager);
        favoriteRecyclerViewItemAdapter = new FavoriteRecyclerViewItemAdapter(favoriteRecyclerViewData);
        favoriteRecyclerViewItemAdapter.setOnItemClickListener(new FavoriteRecyclerViewItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, FavoriteRecyclerViewItem currentView) {
                if (view.getId() == R.id.recyclerview_favorite_delete) {
                    TTIMPacket favoritePacket = new TTIMPacket();
                    TTMessage.Builder builder = TTMessage.newBuilder();
                    builder.setMContent(currentView.getMessage());
                    builder.setMTime(currentView.getTime());
                    builder.setMToId(currentView.getToId());
                    builder.setMFromId(currentView.getuId());
                    TTMessage favoritemessage = builder.build();
                    favoritePacket.setTYPE(TYPE.FAVORITE_REQ);
                    favoritePacket.setBodylength(favoritemessage.toByteArray().length + 1);
                    byte[] body = new byte[favoritemessage.toByteArray().length + 1];
                    body[0] = OprateOptions.DELETE;
                    System.arraycopy(favoritemessage.toByteArray(), 0, body, 1, body.length - 1);
                    favoritePacket.setBody(body);
                    TtApplication.send(favoritePacket);
                    //delete from local
                    try {
                        database.delete("favorite","saveuserid=? and mtime=? and fromid=? and toid=?",new String[]{currentUserId+"", favoritemessage.getMTime()+"", favoritemessage.getMFromId()+"", favoritemessage.getMToId()+""});
                        //database.execSQL("delete from favorite where saveuserid=? and mtime=? and fromid=? and toid=?;", new Object[]{currentUserId, favoritemessage.getMTime(), favoritemessage.getMFromId(), favoritemessage.getMToId()});
                        favoriteRecyclerViewData.remove(currentView);
                        favoriteRecyclerViewItemAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                } else if (view.getId() == R.id.recyclerview_favorite_share) {
                    Intent shareIntent=new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT,currentView.getMessage());
                    startActivity(Intent.createChooser(shareIntent,"分享到"));
                }
            }
        });
        refreshFavoriteAdapter = new HeaderAndFooterAdapter(favoriteRecyclerViewItemAdapter);
        favoriteListView.setAdapter(refreshFavoriteAdapter);
        favoriteListView.setItemAnimator(new DefaultItemAnimator());
        favoriteListView.addRefreshViewCreator(new DefaultRefreshCreator());
        favoriteListView.setOnRefreshListener(new RefreshRecyclerView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                TTIMPacket favoritePacket = new TTIMPacket();
                favoritePacket.setTYPE(TYPE.FAVORITE_REQ);
                favoritePacket.setBodylength(1);
                byte[] favoritebody = new byte[1];
                favoritebody[0] = OprateOptions.GET;
                favoritePacket.setBody(favoritebody);
                TtApplication.send(favoritePacket);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (favoriteListView.getmCurrentRefreshStatus() == favoriteListView.REFRESH_STATUS_REFRESHING) {
                            Message msg = Message.obtain();
                            msg.what = STOP_FAVORITE_REFRESH;
                            handler.sendMessage(msg);
                            Log.d(TAG, "NOT Favorite REFRESH");
                        }
                    }
                }).start();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if(notificationManager!=null){
            notificationManager.cancelAll();
        }
    }

    @SuppressLint("HandlerLeak")
    public class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STOP_MESSAGE_REFRESH:
                    recyclerView.onStopRefresh();
                    break;
                case STOP_FRIEND_REFRESH:
                    friendListView.onStopRefresh();
                    break;
                case STOP_FAVORITE_REFRESH:
                    favoriteListView.onStopRefresh();
                    break;
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
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
        Log.i("MainActivity", "dataInit");
        SharedPreferences currentUserPreferences = getSharedPreferences("data", MODE_PRIVATE);
        int currentUserId = currentUserPreferences.getInt("currentUserId", -1);
        SharedPreferences messagePreferences = getSharedPreferences("message", MODE_PRIVATE);
        Cursor cursor = database.rawQuery("select * from friends where uid=?", new String[]{currentUserId + ""});
        while (cursor.moveToNext()) {
            Log.i("MainActivity", "have friends");
            int friendid = cursor.getInt(cursor.getColumnIndex("friendid"));
            int messagecount = messagePreferences.getInt(friendid + ":" + currentUserId, -1);
            String uname = cursor.getString(cursor.getColumnIndex("Uname"));
            Bitmap photo = BitmapFactory.decodeFile(getFilesDir() + "/head_portrait/" + friendid + ".png");
            if (photo == null) {
                photo = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
                Bundle fileBundle = new Bundle();
                fileBundle.putInt("uid",friendid);
                fileBundle.putByte("type", TYPE.USERPHOTO_GET_REQ);
                if(MessageService.getInstance()!=null){
                    new FileThread(MainActivity.this, fileBundle, MessageService.getInstance().getHandler()).start();
                }
                Log.d("MainActivity", "do not have file" + friendid + ".png");
            }
            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), photo);
            roundedBitmapDrawable.setCircular(true);
            friendRecyclerViewData.add(new FriendRecyclerViewItem(friendid, roundedBitmapDrawable, null, uname));
            Cursor message = database.rawQuery("select * from messages where (Fromid=? and Toid=?) or (Fromid=? and Toid=?) order by Mtime desc limit 1", new String[]{friendid + "", currentUserId + "", currentUserId + "", friendid + ""});
            if (messagecount != -1 && message.moveToNext()) {
                SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.CHINA);
                String mcontent = message.getString(message.getColumnIndex("mcontent"));
                Long mtime = message.getLong(message.getColumnIndex("Mtime"));
                conversationList.add(new ConversationListItem(friendid, uname, mcontent, format.format(new Date(mtime)), messagecount + "", roundedBitmapDrawable));
            }
            message.close();
        }
        cursor.close();
        friendRecyclerViewItemAdapter.notifyDataSetChanged();
        Bitmap photo = BitmapFactory.decodeFile(getFilesDir() + "/head_portrait/" + currentUserId + ".png");
        if (photo == null) {
            photo = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
            Bundle fileBundle = new Bundle();
            fileBundle.putInt("uid",currentUserId);
            fileBundle.putByte("type", TYPE.USERPHOTO_GET_REQ);
            if(MessageService.getInstance()!=null){
                new FileThread(MainActivity.this, fileBundle, MessageService.getInstance().getHandler()).start();
            }
            Log.d("MainActivity", "do not have file" + currentUserId + ".png");
        }
        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), photo);
        roundedBitmapDrawable.setCircular(true);
        ImageButton headerPhotoView = headerView.findViewById(R.id.navigation_photo_header);
        headerPhotoView.setImageDrawable(roundedBitmapDrawable);
        Cursor favorites = database.rawQuery("select * from favorite where  saveuserid=? order by Mtime desc", new String[]{currentUserId + ""});
        while (favorites.moveToNext()) {
            Bitmap favoritephoto = BitmapFactory.decodeFile(getFilesDir() + "/head_portrait/" + favorites.getInt(favorites.getColumnIndex("fromid")) + ".png");
            if (favoritephoto == null) {
                favoritephoto = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
                Bundle fileBundle = new Bundle();
                fileBundle.putInt("uid",favorites.getInt(favorites.getColumnIndex("fromid")));
                fileBundle.putByte("type", TYPE.USERPHOTO_GET_REQ);
                if(MessageService.getInstance()!=null){
                    new FileThread(MainActivity.this, fileBundle, MessageService.getInstance().getHandler()).start();
                }
                Log.d("MainActivity", "do not have file" + currentUserId + ".png");
            }
            RoundedBitmapDrawable roundedBitmapDrawable2 = RoundedBitmapDrawableFactory.create(getResources(), favoritephoto);
            roundedBitmapDrawable2.setCircular(true);
            String uname = "未知";
            if (favorites.getInt(favorites.getColumnIndex("fromid")) == currentUserId) {
                uname = currentUserName;
            } else {
                Cursor favoriteuser = database.rawQuery("select * from friends where uid=? and friendid=?", new String[]{currentUserId + "", favorites.getInt(favorites.getColumnIndex("fromid")) + ""});
                if (favoriteuser.moveToNext()) {
                    uname = favoriteuser.getString(favoriteuser.getColumnIndex("Uname"));
                }
                favoriteuser.close();

            }
            favoriteRecyclerViewData.add(new FavoriteRecyclerViewItem(favorites.getInt(favorites.getColumnIndex("fromid")), uname, favorites.getString(favorites.getColumnIndex("mcontent")), favorites.getLong(favorites.getColumnIndex("mtime")), roundedBitmapDrawable2, favorites.getInt(favorites.getColumnIndex("toid"))));
        }
        favorites.close();
        favoriteRecyclerViewItemAdapter.notifyDataSetChanged();
    }

    @SuppressLint("RestrictedApi")
    private void toolbarAndNavigationInit(){
        //View initialization
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                recyclerView.setRefreshEnable(verticalOffset == 0);
                friendListView.setRefreshEnable(verticalOffset == 0);
                favoriteListView.setRefreshEnable(verticalOffset == 0);
            }
        });
        //------toolbar initialization start
        toolbar.inflateMenu(R.menu.action_menu_main);
        //set toolbar item clickListener
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_main_add:
                        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                        final AlertDialog dialog=builder.setTitle("添加好友")
                                .setPositiveButton("申请", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        TTMessage.Builder messageBuilder=TTMessage.newBuilder();
                                        messageBuilder.setMFromId(currentUserId);
                                        AlertDialog sourceDialog= (AlertDialog) dialog;
                                        EditText idText=sourceDialog.findViewById(R.id.dialog_id);
                                        EditText messageText=sourceDialog.findViewById(R.id.dialog_message);
                                        messageBuilder.setMToId(Integer.parseInt(idText.getText().toString()));
                                        messageBuilder.setMContent(messageText.getText().toString());
                                        messageBuilder.setMTime(System.currentTimeMillis());
                                        TTMessage joinMessage=messageBuilder.build();
                                        TTIMPacket joinPacket=new TTIMPacket();
                                        byte[] body=new byte[joinMessage.toByteArray().length+1];
                                        body[0]=OprateOptions.ASK;
                                        System.arraycopy(joinMessage.toByteArray(),0,body,1,body.length-1);
                                        joinPacket.setTYPE(TYPE.JOIN_REQ);
                                        joinPacket.setBodylength(body.length);
                                        joinPacket.setBody(body);
                                        TtApplication.send(joinPacket);
                                    }
                                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }).setCancelable(false).create();
                        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                        @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.dialog_add_friend, null);
                        EditText idEditText=dialogView.findViewById(R.id.dialog_id);
                        idEditText.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {

                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                if(s.toString().trim().equals("")){
                                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                                }else{
                                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                                }
                            }
                        });
                        dialog.setView(dialogView);
                        dialog.show();
                        Button button=dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                        button.setEnabled(false);
                        break;
                    case R.id.menu_main_scan:
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.function_give_up), Toast.LENGTH_SHORT).show();
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
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer) {
        };
        drawerLayout.addDrawerListener(drawerToggle);
        //drawerLayout associate with toolbar
        drawerToggle.syncState();
        //-----NavigationView initialization start
        //set itemIcon color
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_menu_item_settings:
                        Intent settingIntent=new Intent(MainActivity.this,SettingActivity.class);
                        startActivity(settingIntent);
                        break;
                    case R.id.navigation_menu_item_theme:
                        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this,R.style.dialog_anim);
                        AlertDialog themeDialog=builder.create();
                        themeDialog.show();
                        themeDialog.setContentView(R.layout.dialog_theme);
                        ImageButton themeButton=themeDialog.findViewById(R.id.theme_dialog_blue);
                        themeButton.setColorFilter(0xFF03A9F4);
                        themeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ThemeUtil.setTheme(R.style.AppTheme);
                                recreate();
                            }
                        });

                        themeButton=themeDialog.findViewById(R.id.theme_dialog_pink);
                        themeButton.setColorFilter(0xFFFF4081);
                        themeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ThemeUtil.setTheme(R.style.AppTheme_pink);
                                recreate();
                            }
                        });

                        themeButton=themeDialog.findViewById(R.id.theme_dialog_purple);
                        themeButton.setColorFilter(0xFF800080);
                        themeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ThemeUtil.setTheme(R.style.AppTheme_purple);
                                recreate();
                            }
                        });

                        themeButton=themeDialog.findViewById(R.id.theme_dialog_violet);
                        themeButton.setColorFilter(0xFFEE82EE);
                        themeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ThemeUtil.setTheme(R.style.AppTheme_violet);
                                recreate();
                            }
                        });

                        themeButton=themeDialog.findViewById(R.id.theme_dialog_yellow);
                        themeButton.setColorFilter(0xFFFFFF00);
                        themeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ThemeUtil.setTheme(R.style.AppTheme_yellow);
                                recreate();
                            }
                        });

                        if(themeDialog.getWindow()!=null){
                            themeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        }
                        WindowManager.LayoutParams dialogParams=themeDialog.getWindow().getAttributes();
                        dialogParams.gravity=Gravity.END;
                        dialogParams.width= (int) (88*getResources().getDisplayMetrics().density+0.5f);
                        themeDialog.getWindow().setAttributes(dialogParams);
                        themeDialog.getWindow().setWindowAnimations(R.style.dialog_anim);
                        break;
                    case R.id.navigation_menu_item_copyright:
                        Snackbar.make(navigationView, R.string.copyRight, Snackbar.LENGTH_LONG).show();
                        break;
                    case R.id.navigation_menu_item_about:
                        Intent aboutIntent=new Intent(MainActivity.this,AboutActivity.class);
                        startActivity(aboutIntent);
                        break;
                    case R.id.navigation_menu_item_logout:
                        Intent logoutIntent = new Intent(MainActivity.this, LoginActivity.class);
                        SessionContext sessionContext = TtApplication.getSessionContext();
                        //remove currentUserId currentUserPassword and currentUserName from SharedPreferences
                        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                        editor.remove("currentUserId");
                        editor.remove("currentUserPassword");
                        editor.remove("currentUserName");
                        editor.apply();
                        sessionContext.setLogin(false);
                        try {
                            sessionContext.getSocketChannel().close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        sessionContext.setSocketChannel(null);
                        sessionContext.setuID(null);
                        sessionContext.setBindUser(null);
                        startActivity(logoutIntent);
                        finish();
                        break;
                }
                return false;
            }
        });
        headerView = navigationView.inflateHeaderView(R.layout.navigation_header_main);
        TextView nameText = headerView.findViewById(R.id.navigation_name_header);
        nameText.setText(currentUserName);
        ImageButton headerPhotoView = headerView.findViewById(R.id.navigation_photo_header);
        headerPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent personInfoIntent = new Intent(MainActivity.this, PersonInfoActivity.class);
                personInfoIntent.putExtra("uId", currentUserId);
                personInfoIntent.putExtra("uName", currentUserName);
                startActivity(personInfoIntent);
            }
        });
        //-----NavigationView initialization end

        //BottomNavigation initialization start
        bottomNavigationView = findViewById(R.id.main_bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.bottom_navigation_menu_message:
                        viewPager.setCurrentItem(0);
                        break;
                    case R.id.bottom_navigation_menu_friend:
                        viewPager.setCurrentItem(1);
                        break;
                    case R.id.bottom_navigation_menu_favorite:
                        viewPager.setCurrentItem(2);
                        break;
                }
                return true;
            }
        });
        //BottomNavigation initialization end
    }

}
