package cn.dmandp.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import cn.dmandp.common.Const;
import cn.dmandp.common.RESP_CODE;
import cn.dmandp.common.TYPE;
import cn.dmandp.context.SessionContext;
import cn.dmandp.context.TtApplication;
import cn.dmandp.entity.TTUser;
import cn.dmandp.tt.LoginActivity;
import cn.dmandp.tt.MainActivity;

/**
 * try to get message from server and parse it then send to handler
 * handler is a innerclass belong MessageService
 */
public class MessageService extends Service {
    boolean start = false;
    TTIMHandler handler = new TTIMHandler();

    public MessageService() {
    }

    @Override
    public void onCreate() {
        Log.i("TTIM-MessageService", "MessageService onCreate");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.i("TTIM-MessageService", "MessageService onDestroy");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //first start
        if (!start) {
            start = true;
            Log.i("TTIM-MessageService", "MessageService onStartCommand");
            new Thread(new Runnable() {
                ByteBuffer byteBuffer = ByteBuffer.allocate(Const.BYTEBUFFER_MAX);

                @Override
                public void run() {
                    SessionContext sessionContext = ((TtApplication) getApplication()).getSessionContext();
                    SocketChannel socketChannel = sessionContext.getSocketChannel();
                    //Response type List
                    List<Byte> TYPES = new ArrayList<Byte>();
                    TYPES.add(TYPE.LOGIN_RESP);
                    TYPES.add(TYPE.FRIENDS_RESP);
                    TYPES.add(TYPE.JOIN_RESP);
                    TYPES.add(TYPE.RECEIVE_RESP);
                    TYPES.add(TYPE.REGISTER_RESP);
                    TYPES.add(TYPE.SEND_RESP);
                    TYPES.add(TYPE.HEART);
                    while (true) {
                        int readnum = 0;
                        try {
                            readnum = socketChannel.read(byteBuffer);
                        } catch (Exception e) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                            e.printStackTrace();
                            //retry
                            socketChannel = sessionContext.getSocketChannel();
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
            Bundle bundle = msg.getData();
            switch (msg.what) {
                //Login
                case TYPE.LOGIN_RESP:
                    byte[] body = bundle.getByteArray("body");
                    byte[] user = new byte[body.length - 1];
                    System.arraycopy(body, 1, user, 0, user.length);
                    SessionContext sessionContext = ((TtApplication) getApplication()).getSessionContext();
                    //have been login so do nothing
                    if (sessionContext.isLogin()) {
                        break;
                    }

                    Intent intent = new Intent("cn.dmandp.tt.action.MAINACTIVITY");
                    LoginActivity activity = (LoginActivity) SessionContext.activities.get("LoginActivity");
                    MainActivity mainActivity = (MainActivity) SessionContext.activities.get("MainActivity");
                    //LoginActivity have been destroyed
                    if (activity == null) {
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
                            editor.commit();
                            //save login status(id and user bean) in sessionContext
                            sessionContext.setLogin(true);
                            sessionContext.setuID(currentUser.getUId());
                            sessionContext.setBindUser(currentUser);
                        }
                        break;
                    }
                    if (body[0] != RESP_CODE.SUCCESS) {
                        //Login fail
                        if (activity.isForeground()) {
                            Toast.makeText(activity, new String(user), Toast.LENGTH_SHORT).show();
                            activity.loadView.setVisibility(View.GONE);
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
                        editor.commit();
                        //save login status(id and user bean) in sessionContext
                        sessionContext.setLogin(true);
                        sessionContext.setuID(currentUser.getUId());
                        sessionContext.setBindUser(currentUser);
                        //go to MainActivity
                        activity.startActivity(intent);
                        activity.loadView.setVisibility(View.GONE);
                        activity.finish();
                    }
                    break;
                //receive message
                case TYPE.RECEIVE_RESP:

                    break;
            }
        }
    }
}
