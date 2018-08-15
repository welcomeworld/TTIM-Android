package cn.dmandp.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.amitshekhar.DebugDB;
import com.google.protobuf.InvalidProtocolBufferException;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.dmandp.adapter.ConversationListItemAdapter;
import cn.dmandp.adapter.FavoriteRecyclerViewItemAdapter;
import cn.dmandp.adapter.FriendRecyclerViewItemAdapter;
import cn.dmandp.common.Const;
import cn.dmandp.common.OprateOptions;
import cn.dmandp.common.RESP_CODE;
import cn.dmandp.common.TYPE;
import cn.dmandp.context.SessionContext;
import cn.dmandp.context.TtApplication;
import cn.dmandp.dao.TTIMDaoHelper;
import cn.dmandp.entity.ChatMessage;
import cn.dmandp.entity.ConversationListItem;
import cn.dmandp.entity.FavoriteRecyclerViewItem;
import cn.dmandp.entity.FriendRecyclerViewItem;
import cn.dmandp.entity.TTIMPacket;
import cn.dmandp.entity.TTMessage;
import cn.dmandp.entity.TTUser;
import cn.dmandp.netio.FileThread;
import cn.dmandp.tt.ActivityCollector;
import cn.dmandp.tt.ConversationActivity;
import cn.dmandp.tt.LoginActivity;
import cn.dmandp.tt.MainActivity;
import cn.dmandp.tt.R;
import cn.dmandp.utils.ImageUtil;

/**
 * try to get message from server and parse it then send to handler
 * handler is a innerclass belong MessageService
 */
public class MessageService extends Service {
    TTIMDaoHelper daoHelper = new TTIMDaoHelper(this);
    SQLiteDatabase database;
    String TAG = "TTIM-MessageService";
    boolean start = false;
    static MessageService instance;

    public TTIMHandler getHandler() {
        return handler;
    }

    public static MessageService getInstance() {
        return instance;
    }

    TTIMHandler handler;

    public MessageService() {

    }

    @Override
    public void onCreate() {
        instance = this;
        Log.i("TTIM-MessageService", "MessageService onCreate");
        database = daoHelper.getReadableDatabase();
        handler = new TTIMHandler();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.i("TTIM-MessageService", "MessageService onDestroy");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences databaseaddress = getSharedPreferences("databaseaddress", MODE_PRIVATE);
        SharedPreferences.Editor databaseaddressedit = databaseaddress.edit();
        databaseaddressedit.putString("address", DebugDB.getAddressLog());
        databaseaddressedit.commit();
        Log.e(TAG, DebugDB.getAddressLog());
        //first start
        if (!start) {
            start = true;
            Log.i("TTIM-MessageService", "MessageService onStartCommand");
            new Thread(new Runnable() {
                ByteBuffer byteBuffer = ByteBuffer.allocate(Const.BYTEBUFFER_MAX);

                @Override
                public void run() {
                    SessionContext sessionContext = ((TtApplication) getApplication()).getSessionContext();
                    //Response type List
                    List<Byte> TYPES = new ArrayList<Byte>();
                    TYPES.add(TYPE.LOGIN_RESP);
                    TYPES.add(TYPE.FRIENDS_RESP);
                    TYPES.add(TYPE.JOIN_RESP);
                    TYPES.add(TYPE.RECEIVE_RESP);
                    TYPES.add(TYPE.REGISTER_RESP);
                    TYPES.add(TYPE.SEND_RESP);
                    TYPES.add(TYPE.HEART);
                    TYPES.add(TYPE.USERINFO_RESP);
                    TYPES.add(TYPE.FAVORITE_RESP);
                    // reading  data from server
                    while (true) {
                        int readnum = 0;
                        SocketChannel socketChannel = sessionContext.getSocketChannel();
                        try {
                            readnum = socketChannel.read(byteBuffer);
                            Log.e("TTIM-MessageService", "after read");
                        } catch (Exception e) {
                            e.printStackTrace();
                            try {
                                socketChannel.close();
                                sessionContext.setSocketChannel(null);
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                            continue;
                        }
                        if (readnum == -1) {
                            try {
                                socketChannel.close();
                                sessionContext.setSocketChannel(null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                            continue;
                        }
                        if (readnum > 0) {
                            byteBuffer.flip();
                            while (byteBuffer.hasRemaining()) {
                                byte type = byteBuffer.get();
                                //the message type is not what we want
                                while (byteBuffer.hasRemaining() && !TYPES.contains(type)) {
                                    Log.e(TAG, "舍弃" + type);
                                    type = byteBuffer.get();
                                }
                                if (byteBuffer.remaining() < 4) {
                                    if (TYPES.contains(type)) {
                                        byteBuffer.position(byteBuffer.position() - 1);
                                        byteBuffer.compact();
                                    }
                                    break;
                                } else {
                                    int length = byteBuffer.getInt();
                                    if (length <= byteBuffer.remaining() && length > 0) {
                                        //all fit then send to handle
                                        Log.e(TAG, "package start to handle" + type);
                                        byte[] dst = new byte[length];
                                        byteBuffer.get(dst);
                                        Message msg = Message.obtain();
                                        Bundle bundle = new Bundle();
                                        bundle.putByteArray("body", dst);
                                        msg.setData(bundle);
                                        msg.what = type;
                                        handler.sendMessage(msg);
                                        continue;
                                    } else if (length > byteBuffer.remaining() && length < Const.BYTEBUFFER_MAX - 5) {
                                        //length over remaining but is ok
                                        byteBuffer.position(byteBuffer.position() - 5);
                                        byteBuffer.compact();
                                        break;
                                    }
                                }
                            }
                            if (!byteBuffer.hasRemaining()) {
                                byteBuffer.clear();
                            }
                        }
                        Log.e(TAG, "num=" + readnum);
                    }
                }
            }).start();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MessageServiceBinder();
    }

    public class MessageServiceBinder extends Binder {
        Handler getHandler() {
            return handler;
        }
    }

    //handle message and update UI
    public class TTIMHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            SessionContext sessionContext = TtApplication.getSessionContext();
            LoginActivity loginActivity = (LoginActivity) SessionContext.activities.get("LoginActivity");
            MainActivity mainActivity = (MainActivity) SessionContext.activities.get("MainActivity");
            ConversationActivity conversationActivity = (ConversationActivity) SessionContext.activities.get("ConversationActivity");
            Bundle bundle = msg.getData();
            SharedPreferences userSharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
            int currentUserId = userSharedPreferences.getInt("currentUserId", -1);
            byte[] body = bundle.getByteArray("body");

            switch (msg.what) {
                //Login -----start
                case TYPE.LOGIN_RESP:
                    if (body == null) {
                        return;
                    }
                    byte[] user = new byte[body.length - 1];
                    System.arraycopy(body, 1, user, 0, user.length);
                    if (body[0] != RESP_CODE.SUCCESS) {
                        //Login fail
                        if (loginActivity == null) {
                            ActivityCollector.finishAll();
                            Intent loginIntent = new Intent(MessageService.this, LoginActivity.class);
                            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(loginIntent);
                            Toast.makeText(MessageService.this, new String(user), Toast.LENGTH_SHORT).show();
                            break;
                        }
                        if (loginActivity.isForeground()) {
                            Toast.makeText(loginActivity, new String(user), Toast.LENGTH_SHORT).show();
                            loginActivity.loadView.setVisibility(View.GONE);
                        }
                        break;
                    } else {
                        TTIMPacket favoritePacket = new TTIMPacket();
                        favoritePacket.setTYPE(TYPE.FAVORITE_REQ);
                        favoritePacket.setBodylength(1);
                        byte[] favoritebody = new byte[1];
                        favoritebody[0] = OprateOptions.GET;
                        favoritePacket.setBody(favoritebody);
                        TtApplication.send(favoritePacket);
                        //Login is successful
                        Intent intent = new Intent("cn.dmandp.tt.action.MAINACTIVITY");
                        //LoginActivity have been destroyed
                        if (loginActivity == null) {
                            if (mainActivity != null) {
                                TTUser currentUser = null;
                                try {
                                    TTUser responseuser = TTUser.parseFrom(user);
                                    TTUser.Builder builder = TTUser.newBuilder(responseuser);
                                    builder.setUPassword((String) sessionContext.getAttribute("loginpassword"));
                                    currentUser = builder.build();
                                } catch (InvalidProtocolBufferException e) {
                                    e.printStackTrace();
                                    break;
                                }
                                //save currentUserId and currentUserPassword in SharedPreferences
                                SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                                editor.putInt("currentUserId", currentUser.getUId());
                                editor.putString("currentUserPassword", currentUser.getUPassword());
                                editor.putString("currentUserName", currentUser.getUName());
                                editor.commit();
                                //get friends info from server
                                    TTIMPacket friendpacket = new TTIMPacket();
                                    friendpacket.setTYPE(TYPE.FRIENDS_REQ);
                                    ByteBuffer friendByteBuffer = ByteBuffer.allocate(20);
                                    friendByteBuffer.put(OprateOptions.GET);
                                    friendByteBuffer.flip();
                                    friendpacket.setBodylength(friendByteBuffer.remaining());
                                    friendpacket.setBody(friendByteBuffer.array());
                                    TtApplication.send(friendpacket);
                                    //Bundle fileBundle = new Bundle();
                                    //fileBundle.putInt("uid", friendid);
                                    //fileBundle.putByte("type", TYPE.USERPHOTO_GET_REQ);
                                    //new FileThread(MessageService.this, fileBundle, handler).start();

                                //save login status(id and user bean) in sessionContext
                                sessionContext.setLogin(true);
                                sessionContext.setuID(currentUser.getUId());
                                sessionContext.setBindUser(currentUser);
                                TTIMPacket packet = new TTIMPacket();
                                packet.setTYPE(TYPE.RECEIVE_REQ);
                                byte[] receivebody = {1};
                                packet.setBodylength(receivebody.length);
                                packet.setBody(receivebody);
                                TtApplication.send(packet);
                                Bundle fileBundle = new Bundle();
                                fileBundle.putInt("uid", currentUser.getUId());
                                fileBundle.putByte("type", TYPE.USERPHOTO_GET_REQ);
                                new FileThread(MessageService.this, fileBundle, handler).start();
                            }
                            break;
                        }
                        TTUser currentUser = null;
                        try {
                            TTUser responseuser = TTUser.parseFrom(user);
                            TTUser.Builder builder = TTUser.newBuilder(responseuser);
                            builder.setUPassword((String) sessionContext.getAttribute("loginpassword"));
                            currentUser = builder.build();
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                            break;
                        }
                        //save currentUserId and currentUserPassword in SharedPreferences
                        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                        editor.putInt("currentUserId", currentUser.getUId());
                        editor.putString("currentUserPassword", currentUser.getUPassword());
                        editor.putString("currentUserName", currentUser.getUName());
                        editor.commit();
                        //get friends info from server
                                TTIMPacket friendpacket = new TTIMPacket();
                                friendpacket.setTYPE(TYPE.FRIENDS_REQ);
                                ByteBuffer friendByteBuffer = ByteBuffer.allocate(50);
                                friendByteBuffer.put(OprateOptions.GET);
                                friendByteBuffer.flip();
                                friendpacket.setBodylength(friendByteBuffer.remaining());
                                friendpacket.setBody(friendByteBuffer.array());
                                TtApplication.send(friendpacket);
                        //save login status(id and user bean) in sessionContext
                        sessionContext.setLogin(true);
                        sessionContext.setuID(currentUser.getUId());
                        sessionContext.setBindUser(currentUser);
                        //go to MainActivity
                        loginActivity.startActivity(intent);
                        loginActivity.loadView.setVisibility(View.GONE);
                        loginActivity.finish();
                        TTIMPacket packet = new TTIMPacket();
                        packet.setTYPE(TYPE.RECEIVE_REQ);
                        byte[] receivebody = {1};
                        packet.setBodylength(receivebody.length);
                        packet.setBody(receivebody);
                        TtApplication.send(packet);
                        Bundle fileBundle = new Bundle();
                        fileBundle.putInt("uid", currentUser.getUId());
                        fileBundle.putByte("type", TYPE.USERPHOTO_GET_REQ);
                        new FileThread(MessageService.this, fileBundle, handler).start();
                    }
                    break;
                //Login ------end

                //receive message-----start
                case TYPE.RECEIVE_RESP:
                    Log.e(TAG, "RECEIVEing");
                    if (!sessionContext.isLogin()) {
                        Log.e(TAG, "not Login");
                        break;
                    }
                    if (mainActivity != null && mainActivity.getRecyclerView().getmCurrentRefreshStatus() == mainActivity.getRecyclerView().REFRESH_STATUS_REFRESHING) {
                        mainActivity.getRecyclerView().onStopRefresh();
                    }
                    if (body[0] == RESP_CODE.SUCCESS) {
                        try {
                            boolean foregroundFlage = false;
                            byte[] messagebody = new byte[body.length - 1];
                            System.arraycopy(body, 1, messagebody, 0, messagebody.length);
                            TTMessage message = TTMessage.parseFrom(messagebody);
                            try {
                                //save to database
                                database.execSQL("insert into messages values(?,?,?,?);", new Object[]{message.getMContent(), message.getMTime(), message.getMFromId(), message.getMToId()});
                            } catch (SQLiteConstraintException e) {
                                Log.e(TAG, e.getMessage());
                                return;
                            }
                            SharedPreferences messagePreferences = getSharedPreferences("message", MODE_PRIVATE);
                            SharedPreferences.Editor messageEditor = messagePreferences.edit();
                            int messageCount = messagePreferences.getInt(message.getMFromId() + ":" + message.getMToId(), -1);
                            //update mainActivity UI
                            if (mainActivity != null) {
                                ConversationListItemAdapter conversationListItemAdapter = mainActivity.getConversationListItemAdapter();
                                List<ConversationListItem> conversationList = mainActivity.getConversationList();
                                //not in message list add it to
                                if (messageCount == -1) {
                                    Bitmap photo = BitmapFactory.decodeFile(getFilesDir() + "/head_portrait/" + message.getMFromId() + ".png");
                                    Cursor cursor = database.rawQuery("select * from friends where uid=? and friendid=?", new String[]{sessionContext.getuID() + "", message.getMFromId() + ""});
                                    String username = "未知用户";
                                    if (photo == null) {
                                        photo = BitmapFactory.decodeResource(getResources(), R.drawable.ty);
                                        Bundle fileBundle = new Bundle();
                                        fileBundle.putInt("uid",message.getMFromId() );
                                        fileBundle.putByte("type", TYPE.USERPHOTO_GET_REQ);
                                        new FileThread(MessageService.this, fileBundle, handler).start();

                                    }
                                    if (cursor.moveToNext()) {
                                        username = cursor.getString(cursor.getColumnIndex("Uname"));
                                    }
                                    RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(null, photo);
                                    roundedBitmapDrawable.setCircular(true);
                                    SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                                    ConversationListItem friend = new ConversationListItem(message.getMFromId(), username, message.getMContent(), format.format(new Date(message.getMTime())), 1 + "", roundedBitmapDrawable);
                                    conversationList.add(0, friend);
                                    messageEditor.putInt(message.getMFromId() + ":" + message.getMToId(), 1);
                                    conversationListItemAdapter.notifyItemInserted(0);
                                } else {
                                //in message list now
                                for (int i = 0; i < conversationList.size(); i++) {
                                    if (conversationList.get(i).getUId() == message.getMFromId()) {
                                        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                                        if (conversationActivity != null && conversationActivity.isForeground() && conversationActivity.getChatUserId() == message.getMFromId()) {
                                            conversationList.get(i).setNewMessage(0 + "");
                                            messageEditor.putInt(message.getMFromId() + ":" + message.getMToId(), 0);
                                        } else {
                                            conversationList.get(i).setNewMessage(messageCount + 1 + "");
                                            messageEditor.putInt(message.getMFromId() + ":" + message.getMToId(), messageCount + 1);
                                        }
                                        conversationList.get(i).setMessage(message.getMContent());
                                        conversationList.get(i).setTime(format.format(new Date(message.getMTime())));
                                        conversationListItemAdapter.notifyItemChanged(i);
                                        break;
                                    }
                                }
                                }
                                if (mainActivity.isForeground()) {
                                    foregroundFlage = true;
                                }
                                messageEditor.commit();
                            }
                            ////update mainActivity UI
                            if (conversationActivity != null) {
                                if (conversationActivity.getAllMessages().get(message.getMFromId()) != null) {
                                    Bitmap photo = BitmapFactory.decodeFile(getFilesDir() + "/head_portrait/" + message.getMFromId() + ".png");
                                    if (photo == null) {
                                        photo = BitmapFactory.decodeResource(getResources(), R.drawable.ty);
                                        Bundle fileBundle = new Bundle();
                                        fileBundle.putInt("uid",message.getMFromId() );
                                        fileBundle.putByte("type", TYPE.USERPHOTO_GET_REQ);
                                        new FileThread(MessageService.this, fileBundle, handler).start();
                                    }
                                    RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(null, photo);
                                    roundedBitmapDrawable.setCircular(true);
                                    SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                                    Cursor friendCursor = database.rawQuery("select * from friends where uid=? and friendid=?", new String[]{message.getMToId() + "", message.getMFromId() + ""});
                                    String chatUserName;
                                    if (friendCursor.moveToNext()) {
                                        chatUserName = friendCursor.getString(friendCursor.getColumnIndex("Uname"));
                                    } else {
                                        chatUserName = "未知";
                                    }
                                    conversationActivity.getAllMessages().get(message.getMFromId()).add(new ChatMessage(roundedBitmapDrawable, chatUserName + "", message.getMContent(), message.getMTime(), 1));
                                }
                                List<ChatMessage> messages = conversationActivity.getMessages();
                                //chatting with message sender
                                if (messages.equals(conversationActivity.getAllMessages().get(message.getMFromId()))) {
                                    conversationActivity.getAdapter().notifyDataSetChanged();
                                }
                                if (conversationActivity.isForeground()) {
                                    foregroundFlage = true;
                                }
                            }
                            //app in background
                            if (!foregroundFlage) {
                                Bitmap photo = BitmapFactory.decodeFile(getFilesDir() + "/head_portrait/" + message.getMFromId() + ".png");
                                if (photo == null) {
                                    photo = BitmapFactory.decodeResource(getResources(), R.drawable.ty);
                                    Bundle fileBundle = new Bundle();
                                    fileBundle.putInt("uid",message.getMFromId() );
                                    fileBundle.putByte("type", TYPE.USERPHOTO_GET_REQ);
                                    new FileThread(MessageService.this, fileBundle, handler).start();
                                }
                                RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(null, photo);
                                roundedBitmapDrawable.setCircular(true);
                                Intent resultIntent = new Intent(MessageService.this, MainActivity.class);
                                PendingIntent resultPendingIntent = PendingIntent.getActivity(MessageService.this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                NotificationCompat.Builder builder = new NotificationCompat.Builder(MessageService.this, "highChannel");
                                builder.setSmallIcon(R.drawable.ty);
                                builder.setLargeIcon(ImageUtil.drawToBitmap(roundedBitmapDrawable));
                                builder.setContentText(message.getMContent());
                                builder.setContentTitle(message.getMFromId() + "");
                                builder.setContentIntent(resultPendingIntent);
                                builder.setNumber(messageCount == -1 ? 1 : messageCount + 1);
                                builder.setDefaults(Notification.DEFAULT_ALL);
                                Notification notification = builder.build();
                                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                notificationManager.notify(message.getMFromId(), notification);
                            }
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                //receive message-----end

                //userInfo start
                case TYPE.FRIENDS_RESP:
                    Log.e(TAG, "friends~~");
                    if (body[0] == RESP_CODE.SUCCESS) {
                            byte[] userbody = new byte[body.length - 1];
                            System.arraycopy(body, 1, userbody, 0, userbody.length);
                            try {
                                TTUser getUser = TTUser.parseFrom(userbody);
                                try {
                                    database.execSQL("insert into friends values(?,?,?)", new String[]{sessionContext.getuID() + "", getUser.getUId() + "", getUser.getUName()});
                                } catch (SQLiteConstraintException sc) {
                                    database.execSQL("update friends set uname=? where uid=? and friendid=?", new String[]{getUser.getUName(), sessionContext.getuID() + "", getUser.getUId() + ""});
                                }
                                //update mainActivity UI
                                if (mainActivity != null) {
                                    //update conversationList
                                    ConversationListItemAdapter conversationListItemAdapter = mainActivity.getConversationListItemAdapter();
                                    List<ConversationListItem> conversationList = mainActivity.getConversationList();
                                    for (int i = 0; i < conversationList.size(); i++) {
                                        if (conversationList.get(i).getUId() == getUser.getUId()) {
                                            if (conversationList.get(i).getUsername() != getUser.getUName()) {
                                                conversationList.get(i).setUsername(getUser.getUName());
                                                conversationListItemAdapter.notifyItemChanged(i);
                                            }
                                            break;
                                        }
                                    }
                                    //update friendList
                                    FriendRecyclerViewItemAdapter friendRecyclerViewItemAdapter = mainActivity.getFriendRecyclerViewItemAdapter();
                                    List<FriendRecyclerViewItem> friendRecyclerViewItemList = mainActivity.getFriendRecyclerViewData();
                                    boolean haveFriend=false;
                                    for (int i = 0; i < friendRecyclerViewItemList.size(); i++) {
                                        if (friendRecyclerViewItemList.get(i).getUId() == getUser.getUId()) {
                                            if (friendRecyclerViewItemList.get(i).getUsername() != getUser.getUName()) {
                                                friendRecyclerViewItemList.get(i).setUsername(getUser.getUName());
                                                friendRecyclerViewItemAdapter.notifyItemChanged(i);
                                            }
                                            haveFriend=true;
                                            break;
                                        }
                                    }
                                    if(!haveFriend){
                                        Bitmap photo = BitmapFactory.decodeFile(getFilesDir().getAbsolutePath() + "/head_portrait/" + getUser.getUId() + ".png");
                                        if(photo==null){
                                            photo = BitmapFactory.decodeResource(getResources(), R.drawable.ty);
                                            Bundle fileBundle = new Bundle();
                                            fileBundle.putInt("uid",getUser.getUId());
                                            fileBundle.putByte("type", TYPE.USERPHOTO_GET_REQ);
                                            new FileThread(MessageService.this, fileBundle, handler).start();
                                        }
                                        RoundedBitmapDrawable friendDrawable=RoundedBitmapDrawableFactory.create(null,photo);
                                        friendDrawable.setCircular(true);
                                        friendRecyclerViewItemList.add(new FriendRecyclerViewItem(getUser.getUId(),friendDrawable,null,getUser.getUName()));
                                        friendRecyclerViewItemAdapter.notifyDataSetChanged();
                                    }
                                    //update favoriteList UI
                                    FavoriteRecyclerViewItemAdapter favoriteRecyclerViewItemAdapter = mainActivity.getFavoriteRecyclerViewItemAdapter();
                                    List<FavoriteRecyclerViewItem> favoriteRecyclerViewItemList = mainActivity.getFavoriteRecyclerViewData();
                                    for (int i = 0; i < favoriteRecyclerViewItemList.size(); i++) {
                                        if (favoriteRecyclerViewItemList.get(i).getuId() == getUser.getUId()) {
                                            favoriteRecyclerViewItemList.get(i).setUsername(getUser.getUName());
                                        }
                                    }
                                    favoriteRecyclerViewItemAdapter.notifyDataSetChanged();
                                }
                                //update conversationActivity UI
                                if (conversationActivity != null) {
                                    if (conversationActivity.getChatUserId() == getUser.getUId()) {
                                        if (conversationActivity.getChatUserName() != getUser.getUName()) {
                                            for (int i = 0; i < conversationActivity.getMessages().size(); i++) {
                                                if (conversationActivity.getMessages().get(i).getType() != 0) {
                                                    conversationActivity.getMessages().get(i).setName(getUser.getUName());
                                                }
                                            }
                                            conversationActivity.setChatUserName(getUser.getUName());
                                            conversationActivity.getAdapter().notifyDataSetChanged();
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, e.getMessage() + e.getClass().getSimpleName());
                            }
                    }
                    break;
                //userInfo end
                case TYPE.USERPHOTO_GET_RESP:
                case TYPE.USERPHOTO_SET_RESP:
                    int uid = bundle.getInt("uid", -1);
                    if (uid == -1) {
                        return;
                    }
                    Bitmap photo = BitmapFactory.decodeFile(getFilesDir().getAbsolutePath() + "/head_portrait/" + uid + ".png");
                    if (photo == null) {
                        Log.e("MessageService", "File is not exist");
                        return;
                    }
                    RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(null, photo);
                    roundedBitmapDrawable.setCircular(true);
                    //update MainActivity's photo
                    if (mainActivity != null) {
                        if (uid == currentUserId) {
                            //update navigation header
                            ImageView headerPhotoView = mainActivity.getHeaderView().findViewById(R.id.navigation_photo_header);
                            headerPhotoView.setImageDrawable(roundedBitmapDrawable);
                        } else {
                            //update conversationList
                            ConversationListItemAdapter conversationListItemAdapter = mainActivity.getConversationListItemAdapter();
                            List<ConversationListItem> conversationList = mainActivity.getConversationList();
                            for (int i = 0; i < conversationList.size(); i++) {
                                if (conversationList.get(i).getUId() == uid) {
                                    conversationList.get(i).setImage(roundedBitmapDrawable);
                                    conversationListItemAdapter.notifyItemChanged(i);
                                    break;
                                }
                            }
                            //update friendList
                            FriendRecyclerViewItemAdapter friendRecyclerViewItemAdapter = mainActivity.getFriendRecyclerViewItemAdapter();
                            List<FriendRecyclerViewItem> friendRecyclerViewItemList = mainActivity.getFriendRecyclerViewData();
                            boolean haveFriend = false;
                            for (int i = 0; i < friendRecyclerViewItemList.size(); i++) {
                                if (friendRecyclerViewItemList.get(i).getUId() == uid) {
                                    haveFriend = true;
                                    friendRecyclerViewItemList.get(i).setPrimaryImage(roundedBitmapDrawable);
                                    friendRecyclerViewItemAdapter.notifyItemChanged(i);
                                    break;
                                }
                            }
                            if (!haveFriend) {
                                Cursor cursor = database.rawQuery("select * from friends where uid=? and friendid=?", new String[]{currentUserId + "", uid + ""});
                                if (cursor.moveToNext()) {
                                    friendRecyclerViewItemList.add(new FriendRecyclerViewItem(uid, roundedBitmapDrawable, null, cursor.getString(cursor.getColumnIndex("Uname"))));
                                } else {
                                    friendRecyclerViewItemList.add(new FriendRecyclerViewItem(uid, roundedBitmapDrawable, null, "未知"));
                                }
                                cursor.close();
                                friendRecyclerViewItemAdapter.notifyDataSetChanged();
                            }
                        }
                        //update favorite UI
                        RoundedBitmapDrawable favoriteroundedBitmapDrawable = RoundedBitmapDrawableFactory.create(null, photo);
                        favoriteroundedBitmapDrawable.setCircular(true);
                        FavoriteRecyclerViewItemAdapter favoriteRecyclerViewItemAdapter = mainActivity.getFavoriteRecyclerViewItemAdapter();
                        List<FavoriteRecyclerViewItem> favoriteRecyclerViewItemList = mainActivity.getFavoriteRecyclerViewData();
                        for (int i = 0; i < favoriteRecyclerViewItemList.size(); i++) {
                            if (favoriteRecyclerViewItemList.get(i).getuId() == uid) {
                                favoriteRecyclerViewItemList.get(i).setPrimaryImage(favoriteroundedBitmapDrawable);
                            }
                        }
                        favoriteRecyclerViewItemAdapter.notifyDataSetChanged();
                    }
                    //update ConversationActivity's photo
                    if (conversationActivity != null) {
                        if (conversationActivity.getChatUserId() == uid || currentUserId == uid) {
                            for (int i = 0; i < conversationActivity.getMessages().size(); i++) {
                                if (conversationActivity.getMessages().get(i).getType() == 0 && uid == conversationActivity.getChatUserId()) {
                                    conversationActivity.getMessages().get(i).setTouxiang(roundedBitmapDrawable);
                                }
                                if (conversationActivity.getMessages().get(i).getType() != 0 && uid == currentUserId) {
                                    conversationActivity.getMessages().get(i).setTouxiang(roundedBitmapDrawable);
                                }
                            }
                            conversationActivity.getAdapter().notifyDataSetChanged();
                        }
                    }
                    break;
                case TYPE.FAVORITE_RESP:
                    Log.e(TAG, "favorite...");
                    if (body[0] == RESP_CODE.SUCCESS) {
                        byte[] favoritebody = new byte[body.length - 1];
                        System.arraycopy(body, 1, favoritebody, 0, favoritebody.length);
                        try {
                            TTMessage message = TTMessage.parseFrom(favoritebody);
                            database.execSQL("insert into favorite values(?,?,?,?,?)", new Object[]{sessionContext.getuID(), message.getMContent(), message.getMTime(), message.getMFromId(), message.getMToId()});
                            Bitmap favoritephoto = BitmapFactory.decodeFile(getFilesDir() + "/head_portrait/" + message.getMFromId() + ".png");
                            String username = "未知用户";
                            if (message.getMFromId() == currentUserId) {
                                username = sessionContext.getBindUser().getUName();
                            } else {
                                Cursor cursor = database.rawQuery("select * from friends where uid=? and friendid=?", new String[]{sessionContext.getuID() + "", message.getMFromId() + ""});
                                if (cursor.moveToNext()) {
                                    username = cursor.getString(cursor.getColumnIndex("Uname"));
                                }
                            }
                            if (favoritephoto == null) {
                                favoritephoto = BitmapFactory.decodeResource(getResources(), R.drawable.ty);
                                Bundle fileBundle = new Bundle();
                                fileBundle.putInt("uid",message.getMFromId() );
                                fileBundle.putByte("type", TYPE.USERPHOTO_GET_REQ);
                                new FileThread(MessageService.this, fileBundle, handler).start();
                            }
                            RoundedBitmapDrawable favoriteroundedBitmapDrawable = RoundedBitmapDrawableFactory.create(null, favoritephoto);
                            favoriteroundedBitmapDrawable.setCircular(true);
                            if (mainActivity != null) {
                                mainActivity.getFavoriteRecyclerViewData().add(0, new FavoriteRecyclerViewItem(message.getMFromId(), username, message.getMContent(), message.getMTime(), favoriteroundedBitmapDrawable, message.getMToId()));
                                mainActivity.getFavoriteRecyclerViewItemAdapter().notifyItemInserted(0);
                            }
                        } catch (Exception e) {

                        }
                    }
                    break;
                case TYPE.JOIN_RESP:
                    Log.e(TAG, "join...");
                    if(body[0]==RESP_CODE.SUCCESS){
                        if(body[1]==OprateOptions.ASK){
                            byte[] messagebody = new byte[body.length - 2];
                            System.arraycopy(body, 2, messagebody, 0, messagebody.length);
                            try {
                                TTMessage joinMessage=TTMessage.parseFrom(messagebody);
                                database.execSQL("insert into requests(rcontent,rtime,fromid,toid,rtype) values(?,?,?,?,?)",new Object[]{joinMessage.getMContent(),joinMessage.getMTime(),joinMessage.getMFromId(),joinMessage.getMToId(),0});
                                if(mainActivity!=null){
                                    mainActivity.getNewFriendView().findViewById(R.id.friend_header_notification).setVisibility(View.VISIBLE);
                                }
                            } catch (InvalidProtocolBufferException e) {
                               Log.e(TAG,e.getMessage());
                            }
                        }
                    }
                    break;
            }
        }
    }
}
