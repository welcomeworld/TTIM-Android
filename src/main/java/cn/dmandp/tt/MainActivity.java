/*
 * Copyright (c) 2018. welcomeworld All rights reserved
 */

package cn.dmandp.tt;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
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
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.dmandp.adapter.FavoriteRecyclerViewItemAdapter;
import cn.dmandp.adapter.FriendRecyclerViewItemAdapter;
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
import cn.dmandp.view.LoadView;
import cn.dmandp.view.MyViewPager;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private SessionContext sessionContext;
    private TTIMDaoHelper daoHelper = new TTIMDaoHelper(this);
    private SQLiteDatabase database;
    private int currentUserId;
    private String currentUserName;
    ArrayList<View> viewContainter = new ArrayList<View>();

    public List<ConversationListItem> getConversationList() {
        return conversationList;
    }

    private List<ConversationListItem> conversationList = new ArrayList<ConversationListItem>();

    public ArrayList<FriendRecyclerViewItem> getFriendRecyclerViewData() {
        return friendRecyclerViewData;
    }

    private ArrayList<FriendRecyclerViewItem> friendRecyclerViewData = new ArrayList<FriendRecyclerViewItem>();
    public ConversationListItemAdapter getConversationListItemAdapter() {
        return conversationListItemAdapter;
    }

    private ConversationListItemAdapter conversationListItemAdapter;

    public FriendRecyclerViewItemAdapter getFriendRecyclerViewItemAdapter() {
        return friendRecyclerViewItemAdapter;
    }

    private FriendRecyclerViewItemAdapter friendRecyclerViewItemAdapter;
    private FavoriteRecyclerViewItemAdapter favoriteRecyclerViewItemAdapter;
    private ArrayList<FavoriteRecyclerViewItem> favoriteRecyclerViewData = new ArrayList<FavoriteRecyclerViewItem>();

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private AppBarLayout appBarLayout;
    private ActionBarDrawerToggle drawerToggle;
    private RecyclerView recyclerView;
    private RecyclerView friendListView;

    public FavoriteRecyclerViewItemAdapter getFavoriteRecyclerViewItemAdapter() {
        return favoriteRecyclerViewItemAdapter;
    }

    public ArrayList<FavoriteRecyclerViewItem> getFavoriteRecyclerViewData() {
        return favoriteRecyclerViewData;
    }

    private RecyclerView favoriteListView;
    private MyViewPager viewPager;
    private BottomNavigationView bottomNavigationView;
    LoadView loadView;

    public View getHeaderView() {
        return headerView;
    }

    private View headerView;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        database = daoHelper.getReadableDatabase();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //start service
        Intent intent = new Intent(this, MessageService.class);
        startService(intent);
        //Login verify
        sessionContext = ((TtApplication) getApplication()).getSessionContext();
        SharedPreferences data = getSharedPreferences("data", MODE_PRIVATE);
        currentUserId = data.getInt("currentUserId", -1);
        currentUserName = data.getString("currentUserName", "未登录");
        if (!sessionContext.isLogin()) {
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

        drawerLayout = findViewById(R.id.main_drawerLayout);
        //------toolbar initialization start
        toolbar = findViewById(R.id.main_toolbar);
        toolbar.inflateMenu(R.menu.action_menu_main);
        //set toolbar item clickListener
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_main_add:
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
        final NavigationView navigationView = findViewById(R.id.main_navigation);
        //set itemIcon color
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_menu_item_theme:
                        ThemeUtil.setTheme(R.style.AppTheme_pink);
                        recreate();
                        break;
                    case R.id.navigation_menu_item_copyright:
                        Snackbar.make(navigationView, R.string.copyRight, Snackbar.LENGTH_LONG).show();
                        break;
                    case R.id.navigation_menu_item_about:
                        break;
                    case R.id.navigation_menu_item_logout:
                        Intent logoutIntent = new Intent(MainActivity.this, LoginActivity.class);
                        TtApplication application = (TtApplication) getApplication();
                        SessionContext sessionContext = application.getSessionContext();
                        //remove currentUserId currentUserPassword and currentUserName from SharedPreferences
                        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                        editor.remove("currentUserId");
                        editor.remove("currentUserPassword");
                        editor.remove("currentUserName");
                        editor.commit();
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

        //-----viewpager initialization start
        viewPager = findViewById(R.id.main_viewpager);
        View messageView = LayoutInflater.from(this).inflate(R.layout.viewpager_main_message, null);
        View friendView = LayoutInflater.from(this).inflate(R.layout.viewpager_main_friend, null);
        View favoriteView = LayoutInflater.from(this).inflate(R.layout.viewpager_main_favorite, null);
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
                editor.commit();
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
        recyclerView.setAdapter(conversationListItemAdapter);
        recyclerView.addItemDecoration(new MyDividerItemDecoration());
        recyclerView.setItemAnimator(new DefaultItemAnimator());
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
                    editor.commit();
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
                                photo = BitmapFactory.decodeResource(getResources(), R.drawable.ty);
                                Log.e("MainActivity", "do not have file" + uId + ".png");
                            }
                            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(null, photo);
                            roundedBitmapDrawable.setCircular(true);
                            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                            conversationList.add(0, new ConversationListItem(uId, cursor.getString(cursor.getColumnIndex("Uname")), "", format.format(new Date(System.currentTimeMillis())), 0 + "", roundedBitmapDrawable));
                            conversationListItemAdapter.notifyItemInserted(0);
                        }
                    }
                }
            }
        });
        friendListView.setAdapter(friendRecyclerViewItemAdapter);
        friendListView.addItemDecoration(new MyDividerItemDecoration());
        friendListView.setItemAnimator(new DefaultItemAnimator());

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
                    Toast.makeText(MainActivity.this, "you onclick:delete", Toast.LENGTH_LONG).show();
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
                        database.execSQL("delete from favorite where saveuserid=? and mtime=? and fromid=? and toid=?;", new Object[]{currentUserId, favoritemessage.getMTime(), favoritemessage.getMFromId(), favoritemessage.getMToId()});
                        favoriteRecyclerViewData.remove(currentView);
                        favoriteRecyclerViewItemAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }

                } else if (view.getId() == R.id.recyclerview_favorite_share) {
                    Toast.makeText(MainActivity.this, "you onclick:share", Toast.LENGTH_LONG).show();
                } else if (view.getId() == R.id.recyclerview_favorite_card) {
                    Toast.makeText(MainActivity.this, "you onclick:card", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "you onclick:" + view.getId(), Toast.LENGTH_LONG).show();
                }
            }
        });
        favoriteListView.setAdapter(favoriteRecyclerViewItemAdapter);
        favoriteListView.addItemDecoration(new MyDividerItemDecoration());
        favoriteListView.setItemAnimator(new DefaultItemAnimator());
        dataInit();
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
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
        Log.e("MainActivity", "dataInit");
        SharedPreferences currentUserPreferences = getSharedPreferences("data", MODE_PRIVATE);
        int currentUserId = currentUserPreferences.getInt("currentUserId", -1);
        SharedPreferences messagePreferences = getSharedPreferences("message", MODE_PRIVATE);
        Cursor cursor = database.rawQuery("select * from friends where uid=?", new String[]{currentUserId + ""});
        while (cursor.moveToNext()) {
            Log.e("MainActivity", "have friends");
            int friendid = cursor.getInt(cursor.getColumnIndex("friendid"));
            int messagecount = messagePreferences.getInt(friendid + ":" + currentUserId, -1);
            String uname = cursor.getString(cursor.getColumnIndex("Uname"));
            Bitmap photo = BitmapFactory.decodeFile(getFilesDir() + "/head_portrait/" + friendid + ".png");
            if (photo == null) {
                photo = BitmapFactory.decodeResource(getResources(), R.drawable.ty);
                Log.e("MainActivity", "do not have file" + friendid + ".png");
            }
            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(null, photo);
            roundedBitmapDrawable.setCircular(true);
            friendRecyclerViewData.add(new FriendRecyclerViewItem(friendid, roundedBitmapDrawable, null, uname));
            Cursor message = database.rawQuery("select * from messages where (Fromid=? and Toid=?) or (Fromid=? and Toid=?) order by Mtime desc limit 1", new String[]{friendid + "", currentUserId + "", currentUserId + "", friendid + ""});
            if (messagecount != -1 && message.moveToNext()) {
                SimpleDateFormat format = new SimpleDateFormat("HH:mm");
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
            photo = BitmapFactory.decodeResource(getResources(), R.drawable.ty);
            Log.e("MainActivity", "do not have file" + currentUserId + ".png");
        }
        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(null, photo);
        roundedBitmapDrawable.setCircular(true);
        ImageButton headerPhotoView = headerView.findViewById(R.id.navigation_photo_header);
        headerPhotoView.setImageDrawable(roundedBitmapDrawable);
        Cursor favorites = database.rawQuery("select * from favorite where  saveuserid=? order by Mtime desc", new String[]{currentUserId + ""});
        while (favorites.moveToNext()) {
            Bitmap favoritephoto = BitmapFactory.decodeFile(getFilesDir() + "/head_portrait/" + favorites.getInt(favorites.getColumnIndex("fromid")) + ".png");
            if (favoritephoto == null) {
                favoritephoto = BitmapFactory.decodeResource(getResources(), R.drawable.ty);
                Log.e("MainActivity", "do not have file" + currentUserId + ".png");
            }
            RoundedBitmapDrawable roundedBitmapDrawable2 = RoundedBitmapDrawableFactory.create(null, favoritephoto);
            roundedBitmapDrawable2.setCircular(true);
            String uname = "未知";
            if (favorites.getInt(favorites.getColumnIndex("fromid")) == currentUserId) {
                uname = currentUserName;
            } else {
                Cursor favoriteuser = database.rawQuery("select * from friends where uid=? and friendid=?", new String[]{currentUserId + "", favorites.getInt(favorites.getColumnIndex("fromid")) + ""});
                if (favoriteuser.moveToNext()) {
                    uname = favoriteuser.getString(favoriteuser.getColumnIndex("uname"));
                }

            }
            favoriteRecyclerViewData.add(new FavoriteRecyclerViewItem(favorites.getInt(favorites.getColumnIndex("fromid")), uname, favorites.getString(favorites.getColumnIndex("mcontent")), favorites.getLong(favorites.getColumnIndex("mtime")), roundedBitmapDrawable2, favorites.getInt(favorites.getColumnIndex("toid"))));
        }
        favoriteRecyclerViewItemAdapter.notifyDataSetChanged();
    }

}
