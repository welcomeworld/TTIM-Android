package cn.dmandp.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.amitshekhar.DebugDB;
import com.google.protobuf.InvalidProtocolBufferException;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.dmandp.adapter.ConversationListItemAdapter;
import cn.dmandp.common.Const;
import cn.dmandp.common.OprateOptions;
import cn.dmandp.common.RESP_CODE;
import cn.dmandp.common.TYPE;
import cn.dmandp.context.SessionContext;
import cn.dmandp.context.TtApplication;
import cn.dmandp.dao.TTIMDaoHelper;
import cn.dmandp.entity.ConversationListItem;
import cn.dmandp.entity.TTIMPacket;
import cn.dmandp.entity.TTMessage;
import cn.dmandp.entity.TTUser;
import cn.dmandp.tt.ConversationActivity;
import cn.dmandp.tt.LoginActivity;
import cn.dmandp.tt.MainActivity;
import cn.dmandp.tt.R;

/**
 * try to get message from server and parse it then send to handler
 * handler is a innerclass belong MessageService
 */
public class MessageService extends Service {
    TTIMDaoHelper daoHelper = new TTIMDaoHelper(this);
    SQLiteDatabase database;
    String TAG = "TTIM-MessageService";
    boolean start = false;
    TTIMHandler handler = new TTIMHandler();

    public MessageService() {
    }

    @Override
    public void onCreate() {
        Log.i("TTIM-MessageService", "MessageService onCreate");
        database = daoHelper.getReadableDatabase();
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
                    while (true) {
                        int readnum = 0;
                        try {
                            Log.e("TTIM-MessageService", "before read");
                            SocketChannel socketChannel = sessionContext.getSocketChannel();
                            readnum = socketChannel.read(byteBuffer);
                            Log.e("TTIM-MessageService", "after read");
                        } catch (Exception e) {
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                            e.printStackTrace();
                            continue;
                        }
                        if (readnum > 0) {
                            byteBuffer.flip();
                            byte type = byteBuffer.get();
                            //the message type is not what we want
                            while (byteBuffer.hasRemaining() && !TYPES.contains(type)) {
                                type = byteBuffer.get();
                            }

                            if (byteBuffer.remaining() < 4) {
                                if (TYPES.contains(type)) {
                                    byteBuffer.position(byteBuffer.position() - 1);
                                    byteBuffer.compact();
                                } else {
                                    byteBuffer.clear();
                                }
                                continue;
                            } else {
                                int length = byteBuffer.getInt();

                                if (length <= byteBuffer.remaining() && length > 0) {
                                    //all fit then send to handle
                                    Log.i("TTIM-MessageService", "package success to handle");
                                    byte[] dst = new byte[length];
                                    byteBuffer.get(dst);
                                    byteBuffer.compact();
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
                                    continue;
                                } else {
                                    //length illegal
                                    byteBuffer.compact();
                                    continue;
                                }
                            }
                        } else {
                            //no new message so thread sleep;
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }).start();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //handle message and update UI
    public class TTIMHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            SessionContext sessionContext = ((TtApplication) getApplication()).getSessionContext();
            LoginActivity loginActivity = (LoginActivity) SessionContext.activities.get("LoginActivity");
            MainActivity mainActivity = (MainActivity) SessionContext.activities.get("MainActivity");
            ConversationActivity conversationActivity = (ConversationActivity) SessionContext.activities.get("ConversationActivity");
            Bundle bundle = msg.getData();
            byte[] body = bundle.getByteArray("body");
            switch (msg.what) {
                //Login
                case TYPE.LOGIN_RESP:
                    byte[] user = new byte[body.length - 1];
                    System.arraycopy(body, 1, user, 0, user.length);
                    //have been login so do nothing
                    if (sessionContext.isLogin()) {
                        Log.e("TTIM-MessageService", "User have been login");
                        break;
                    }
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
                            List<Integer> friendlist = currentUser.getUFriendsList();
                            for (int friendid : friendlist) {
                                Cursor friendCursor = database.rawQuery("select * from friends where uid=? and friendid=?", new String[]{currentUser.getUId() + "", friendid + ""});
                                if (friendCursor.moveToNext()) {
                                    continue;
                                } else {
                                    TTIMPacket friendpacket = new TTIMPacket();
                                    friendpacket.setTYPE(TYPE.USERINFO_REQ);
                                    ByteBuffer friendByteBuffer = ByteBuffer.allocate(50);
                                    friendByteBuffer.put(OprateOptions.GET);
                                    friendByteBuffer.putInt(friendid);
                                    friendByteBuffer.flip();
                                    friendpacket.setBodylength(friendByteBuffer.remaining());
                                    friendpacket.setBody(friendByteBuffer.array());
                                    TtApplication.send(friendpacket);
                                }
                            }
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
                        }
                        break;
                    }
                    if (body[0] != RESP_CODE.SUCCESS) {
                        //Login fail
                        if (loginActivity.isForeground()) {
                            Toast.makeText(loginActivity, new String(user), Toast.LENGTH_SHORT).show();
                            loginActivity.loadView.setVisibility(View.GONE);
                        }
                        break;
                    } else {
                        //Login is successful
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
                        List<Integer> friendlist = currentUser.getUFriendsList();
                        for (int friendid : friendlist) {
                            Cursor friendCursor = database.rawQuery("select * from friends where uid=? and friendid=?", new String[]{currentUser.getUId() + "", friendid + ""});
                            if (friendCursor.moveToNext()) {
                                continue;
                            } else {
                                TTIMPacket friendpacket = new TTIMPacket();
                                friendpacket.setTYPE(TYPE.USERINFO_REQ);
                                ByteBuffer friendByteBuffer = ByteBuffer.allocate(50);
                                friendByteBuffer.put(OprateOptions.GET);
                                friendByteBuffer.putInt(friendid);
                                friendByteBuffer.flip();
                                friendpacket.setBodylength(friendByteBuffer.remaining());
                                friendpacket.setBody(friendByteBuffer.array());
                                TtApplication.send(friendpacket);
                            }
                        }
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
                        //go to MainActivity
                        loginActivity.startActivity(intent);
                        loginActivity.loadView.setVisibility(View.GONE);
                        loginActivity.finish();
                    }
                    break;
                //receive message
                case TYPE.RECEIVE_RESP:
                    if (!sessionContext.isLogin()) {
                        break;
                    }
                    if (body[0] == RESP_CODE.SUCCESS) {
                        try {
                            byte[] messagebody = new byte[body.length - 1];
                            System.arraycopy(body, 1, messagebody, 0, messagebody.length);
                            TTMessage message = TTMessage.parseFrom(messagebody);
                            Log.e(TAG, message.getMContent());
                            try {
                                database.execSQL("insert into messages values(?,?,?,?);", new Object[]{message.getMContent(), message.getMTime(), message.getMFromId(), message.getMToId()});
                            } catch (SQLiteConstraintException e) {
                                return;
                            }
                            if (mainActivity != null && mainActivity.isForeground()) {
                                ConversationListItemAdapter conversationListItemAdapter = mainActivity.getConversationListItemAdapter();
                                SharedPreferences messagePreferences = getSharedPreferences("message", MODE_PRIVATE);
                                SharedPreferences.Editor messageEditor = messagePreferences.edit();
                                int messageCount = messagePreferences.getInt(message.getMFromId() + ":" + message.getMToId(), -1);
                                List<ConversationListItem> conversationList = mainActivity.getConversationList();
                                //not in message list add it to
                                if (messageCount == -1) {
                                    Bitmap photo = BitmapFactory.decodeFile(getFilesDir() + "/head_portrait/" + message.getMFromId() + ".png");
                                    Cursor cursor = database.rawQuery("select * from friends where uid=? and friendid=?", new String[]{sessionContext.getuID() + "", message.getMFromId() + ""});
                                    String username = "未知用户";
                                    if (photo == null) {
                                        photo = BitmapFactory.decodeResource(getResources(), R.drawable.ty);
                                        Log.e("MainActivity", "do not have file");
                                    }
                                    if (cursor.moveToNext()) {
                                        username = cursor.getString(cursor.getColumnIndex("Uname"));
                                    }
                                    SimpleDateFormat format = new SimpleDateFormat("HH:MM");
                                    ConversationListItem friend = new ConversationListItem(message.getMFromId(), username, message.getMContent(), format.format(new Date(message.getMTime())), 1 + "", photo);
                                    conversationList.add(0, friend);
                                    conversationListItemAdapter.notifyItemInserted(0);
                                    messageEditor.putInt(message.getMFromId() + ":" + message.getMToId(), 1);
                                    messageEditor.commit();
                                    return;
                                }
                                //in message list now
                                for (int i = 0; i < conversationList.size(); i++) {
                                    if (conversationList.get(i).getUId() == message.getMFromId()) {
                                        conversationList.get(i).getNewMessage();
                                        conversationList.get(i).setNewMessage(messageCount + 1 + "");
                                        conversationListItemAdapter.notifyItemChanged(i);
                                        break;
                                    }
                                }
                                return;
                            }
                            //is chatting
                            if (conversationActivity != null && conversationActivity.isForeground()) {
                                return;
                            }
                            //app in background
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case TYPE.USERINFO_RESP:
                    Log.e(TAG, "user_info");
                    if (body[0] == RESP_CODE.SUCCESS) {
                        if (body[1] == OprateOptions.GET) {
                            byte[] userbody = new byte[body.length - 2];
                            System.arraycopy(body, 2, userbody, 0, userbody.length);
                            try {
                                TTUser getUser = TTUser.parseFrom(userbody);
                                database.execSQL("insert into friends values(?,?,?)", new String[]{sessionContext.getuID() + "", getUser.getUId() + "", getUser.getUName()});
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
            }
        }
    }
}
