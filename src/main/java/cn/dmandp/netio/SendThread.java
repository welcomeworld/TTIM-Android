/*
 * Copyright (c) 2018. welcomeworld All rights reserved
 */

package cn.dmandp.netio;

import android.util.Log;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import cn.dmandp.context.SessionContext;
import cn.dmandp.context.TtApplication;
import cn.dmandp.entity.TTIMPacket;
import cn.dmandp.tt.MainActivity;

/**
 * Created by 萌即正义 on 10/06/2018.
 */

public class SendThread extends Thread {
    String TAG="SendThread";
    private TTIMPacket packet;

    public SendThread(TTIMPacket packet) {
        this.packet = packet;
    }

    @Override
    public void run() {
        try {
            SessionContext sessionContext = TtApplication.getSessionContext();
            SocketChannel socketChannel = sessionContext.getSocketChannel();
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            byteBuffer.put(packet.getTYPE());
            byteBuffer.putInt(packet.getBodylength());
            int length = packet.getBodylength();
            int count = 0;
            while (length != 0) {
                int writeCount = 0;
                if (length > byteBuffer.limit()) {
                    writeCount = byteBuffer.limit();
                } else {
                    writeCount = length;
                    length = 0;
                }
                byteBuffer.put(packet.getBody(), count, writeCount);
                count += writeCount;
                byteBuffer.flip();
                //send packet to server
                while (byteBuffer.hasRemaining()) {
                    socketChannel.write(byteBuffer);
                }
                byteBuffer.compact();
                Log.i(TAG, packet.getTYPE() + "send success");
            }
        } catch (Exception e) {
            Log.e(TAG, packet.getTYPE() + "send fail because:"+e.getMessage());
        }
    }
}
