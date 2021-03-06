package cn.dmandp.context;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Locale;
import cn.dmandp.common.Const;
import cn.dmandp.common.TYPE;
import cn.dmandp.entity.TTIMPacket;
import cn.dmandp.entity.TTUser;
import cn.dmandp.netio.SendThread;
import cn.dmandp.service.MessageService;

/**
 * Created by 萌即正义 on 27/03/2018.
 */

public class TtApplication extends Application {
    @SuppressWarnings("FieldCanBeLocal")
    private static String TAG = "TTIM-TtApplication";
    static SessionContext sessionContext = new SessionContext(null);

    @Override
    public void onCreate() {
        super.onCreate();
        Resources resources=getResources();
        Configuration configuration=resources.getConfiguration();
        String language=PreferenceManager.getDefaultSharedPreferences(this).getString("language","0");
        Locale locale;
        if(language.equalsIgnoreCase("0")){
            locale=Locale.CHINESE;
        }else{
            locale=Locale.US;
        }
        configuration.locale=locale;
        DisplayMetrics displayMetrics=resources.getDisplayMetrics();
        resources.updateConfiguration(configuration,displayMetrics);
        Intent serviceIntent=new Intent(TtApplication.this, MessageService.class);
        startService(serviceIntent);
        notificationChannelInit();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i("TTIM-TtApplication", "networkService has run");
                SocketChannel socketChannel = null;
                String error = null;
                //open socketChannel
                try {
                    socketChannel = SocketChannel.open(new InetSocketAddress(Const.HOST, Const.PORT));
                } catch (Exception e) {
                    Log.e("TTIM-TtApplication", e.getMessage());
                    error = e.getMessage();
                }
                sessionContext.setSocketChannel(socketChannel);
                sessionContext.socketChannelErrorMessage = error;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean heartfalg = true;
                        //noinspection InfiniteLoopStatement
                        while (true) {
                            SocketChannel socketChannel = sessionContext.getSocketChannel();
                            //send heart
                            ByteBuffer heart = ByteBuffer.allocate(20);
                            heart.put(TYPE.HEART);
                            heart.putInt(1);
                            heart.put((byte) 120);
                            try {
                                heart.flip();
                                while (heart.hasRemaining()) {
                                    socketChannel.write(heart);
                                }
                                if (heartfalg) {
                                    Log.d("TTIM-TtApplication", "heart have start");
                                }
                                heartfalg = false;
                            } catch (Exception e) {
                                if (!heartfalg) {
                                    Log.d("TTIM-TtApplication", "heart have stop");
                                    heartfalg = true;
                                }
                            }
                            try {
                                Thread.sleep(4000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
                //noinspection InfiniteLoopStatement
                while (true) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    socketChannel = sessionContext.getSocketChannel();
                    // socketChannel reconnect
                    if (socketChannel == null) {
                        boolean flag = true;
                        while (flag) {
                            try {
                                socketChannel = SocketChannel.open(new InetSocketAddress(Const.HOST, Const.PORT));
                                Log.d("TTIM-TtApplication", "reconnect" + socketChannel.hashCode());
                                sessionContext.setSocketChannel(socketChannel);
                                //if have been login,login directly after reconnect
                                if (sessionContext.isLogin()) {
                                    ByteBuffer byteBuffer = ByteBuffer.allocate(514);
                                    byteBuffer.put(TYPE.LOGIN_REQ);
                                    TTUser.Builder builder = TTUser.newBuilder();
                                    builder.setUId(sessionContext.getuID());
                                    builder.setUPassword(sessionContext.getBindUser().getUPassword());
                                    TTUser loginuser = builder.build();
                                    byte[] body = loginuser.toByteArray();
                                    byteBuffer.putInt(body.length);
                                    byteBuffer.put(body);
                                    byteBuffer.flip();
                                    while (byteBuffer.hasRemaining()) {
                                        socketChannel.write(byteBuffer);
                                    }
                                }
                                flag = false;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }).start();
    }

    static public SessionContext getSessionContext() {
        return sessionContext;
    }

    static public void send(TTIMPacket packet) {
        if (sessionContext.getSocketChannel() != null) {
            Log.i(TAG, sessionContext.getSocketChannel().hashCode() + "send Packet" + packet.getTYPE());
        }
        new SendThread(packet).start();
    }

    public void notificationChannelInit() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("highChannel", "notificationChannel", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.GREEN);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            notificationChannel.setShowBadge(true);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if(notificationManager!=null){
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
    }
}
