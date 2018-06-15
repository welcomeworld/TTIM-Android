package cn.dmandp.context;
import android.app.Application;

import android.util.Log;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;


import cn.dmandp.common.Const;
import cn.dmandp.common.TYPE;
import cn.dmandp.entity.TTIMPacket;
import cn.dmandp.entity.TTUser;
import cn.dmandp.netio.SendThread;

/**
 * Created by 萌即正义 on 27/03/2018.
 */

public class TtApplication extends Application {
    private static String TAG = "TTIM-TtApplication";
    static SessionContext sessionContext = new SessionContext(null);

    @Override
    public void onCreate() {
        super.onCreate();
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
                                    Log.e("TTIM-TtApplication", "heart have start");
                                }
                                heartfalg = false;
                            } catch (Exception e) {
                                if (!heartfalg) {
                                    Log.e("TTIM-TtApplication", "heart have stop");
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
                                Log.e("TTIM-TtApplication", "reconnect" + socketChannel.hashCode());
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
            Log.e(TAG, sessionContext.getSocketChannel().hashCode() + "send Packet" + packet.getTYPE());
        }
        new SendThread(packet).start();
    }

    ;
}
