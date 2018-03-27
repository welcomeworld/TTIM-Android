package cn.dmandp.context;

import android.app.Application;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import cn.dmandp.common.SessionContext;

/**
 * Created by 萌即正义 on 27/03/2018.
 */

public class TtApplication extends Application {
    SessionContext sessionContext;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("TTIM-Application", "Application OnCreate!");
    }

    public SessionContext getSessionContext() {
        if (sessionContext == null) {
            try {
                SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("119.28.9.43", 52072));
                sessionContext = new SessionContext(socketChannel);
            } catch (IOException e) {
                Log.e("TTIM-Application", "create SocketChannel ERROR");
                Log.e("TTIM-Application", e.getMessage());
            }
        }
        return sessionContext;
    }
}
