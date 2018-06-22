/*
 * Copyright (c) 2018. welcomeworld All rights reserved
 */

package cn.dmandp.netio;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import cn.dmandp.common.Const;
import cn.dmandp.common.RESP_CODE;
import cn.dmandp.common.TYPE;
import cn.dmandp.context.TtApplication;
import cn.dmandp.tt.MainActivity;
import cn.dmandp.utils.HashUtil;

public class FileThread extends Thread {
    int uid;
    Context context;
    Handler handler;
    byte type;

    public FileThread(Context context, Bundle bundle, Handler handler) {
        this.context = context;
        this.uid = bundle.getInt("uid", -1);
        this.handler = handler;
        this.type = bundle.getByte("type", TYPE.USERPHOTO_GET_REQ);
    }

    @Override
    public void run() {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(Const.HOST, Const.FILEPORT));
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
            Log.e("FileThread", "start to get File");
            os.write(type);
            os.write(uid);
            Log.e("FileThread", "send success");
            File dir = new File(context.getFilesDir(), "head_portrait");
            if (!dir.exists()) {
                dir.mkdir();
            }
            File photo = new File(dir, uid + ".png");

            if (type == TYPE.USERPHOTO_GET_REQ) {
                boolean existsFlag = true;
                if (!photo.exists()) {
                    photo.createNewFile();
                    existsFlag = false;
                }
                String hash = HashUtil.getHash(new FileInputStream(photo), "MD5");
                Log.e("FileThread", uid + "hash:" + hash);
                os.write(hash.getBytes());
                if (is.read() != RESP_CODE.SUCCESS) {
                    if (!existsFlag) {
                        photo.delete();
                    }
                    Log.e("FileThread", "get photo fail");
                    return;
                }
                Log.e("FileThread", "File created");
                FileOutputStream fos = new FileOutputStream(photo);
                byte[] fileBytes = new byte[1024];
                int length = 0;
                int count = 0;
                while ((length = is.read(fileBytes, 0, fileBytes.length)) != -1) {
                    count = count + length;
                    fos.write(fileBytes, 0, length);
                    fos.flush();
                    Log.e("FileThread", "File writed" + count + "byte");
                }
                if (count <= 0) {
                    photo.delete();
                } else {
                    Message msg = Message.obtain();
                    Bundle bundle = new Bundle();
                    bundle.putInt("uid", uid);
                    msg.setData(bundle);
                    msg.what = TYPE.USERPHOTO_GET_RESP;
                    handler.sendMessage(msg);
                }
                fos.close();
            } else if (type == TYPE.USERPHOTO_SET_REQ) {
                if (!photo.exists()) {
                    Log.e("FileThread", "file not exists");
                }
                Log.e("FileThread", "File created");
                FileInputStream fis = new FileInputStream(photo);
                byte[] fileBytes = new byte[1024];
                int length = 0;
                int count = 0;
                while ((length = fis.read(fileBytes, 0, fileBytes.length)) != -1) {
                    count = count + length;
                    os.write(fileBytes, 0, length);
                    os.flush();
                    Log.e("FileThread", "File writed" + count + "byte");
                }
                if (count <= 0) {
                    Log.e("FileThread", "send file fail");
                } else {
                    Message msg = Message.obtain();
                    Bundle bundle = new Bundle();
                    bundle.putInt("uid", uid);
                    msg.setData(bundle);
                    msg.what = TYPE.USERPHOTO_SET_RESP;
                    handler.sendMessage(msg);
                }
                fis.close();
            }
            os.close();
            is.close();
        } catch (IOException e) {
            Log.e("FileThread", e.getMessage());
        }
    }
}
