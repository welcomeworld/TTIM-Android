package cn.dmandp.context;

import android.app.Application;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.SocketChannel;

import cn.dmandp.common.SessionContext;

/**
 * Created by 萌即正义 on 27/03/2018.
 */

public class TtApplication extends Application {
    SessionContext sessionContext;

    public TtApplication() {
        super();
        try {
            SocketChannel socketChannel = SocketChannel.open();
            sessionContext = new SessionContext(socketChannel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SessionContext getSessionContext() {
        return sessionContext;
    }
}
