package cn.dmandp.context;
import android.app.Application;
import android.util.Log;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;

import cn.dmandp.common.Const;
import cn.dmandp.common.ErrorTips;
import cn.dmandp.common.TYPE;
import cn.dmandp.entity.TTUser;

/**
 * Created by 萌即正义 on 27/03/2018.
 */

public class TtApplication extends Application {
    SessionContext sessionContext;

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i("TTIM-TtApplication", "networkService has run");
                SocketChannel socketChannel = null;
                String error = null;
                try {
                    socketChannel = SocketChannel.open(new InetSocketAddress(Const.HOST, Const.PORT));
                } catch (SecurityException e) {
                    Log.e("TTIM-TtApplication", ErrorTips.securityExceptionMessage);
                    error = ErrorTips.securityExceptionMessage;
                } catch (IOException e) {
                    Log.e("TTIM-TtApplication", e.getMessage());
                    error = "连接服务器失败，请确保网络连通！";
                } catch (UnresolvedAddressException e) {
                    Log.e("TTIM-TtApplication", ErrorTips.unresolvedAddressExceptionMessage);
                    error = ErrorTips.unresolvedAddressExceptionMessage;
                }
                sessionContext = new SessionContext(socketChannel);
                sessionContext.socketChannelErrorMessage = error;
                while (true) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
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
                            } catch (IOException e) {
                                Log.e("TTIM-TtApplication", "connect server fail!");
                                e.printStackTrace();
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    }
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
                        Log.d("TTIM-TtApplication", "heart message socketChannelHashCode=" + socketChannel.hashCode());
                    } catch (IOException e) {
                        if (sessionContext.getSocketChannel() != null) {
                            try {
                                sessionContext.getSocketChannel().close();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                        sessionContext.setSocketChannel(null);
                        Log.e("TTIM-TtApplication", e.getMessage());
                        Log.i("TTIM-TtApplication", "Reconnecting!");
                    }
                }
            }
        }).start();
    }
    public SessionContext getSessionContext() {
        return sessionContext;
    }
}
