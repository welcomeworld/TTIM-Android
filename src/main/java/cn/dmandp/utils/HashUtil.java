/*
 * Copyright (c) 2018. welcomeworld All rights reserved
 */

package cn.dmandp.utils;

import android.util.Log;

import java.io.FileInputStream;
import java.security.MessageDigest;

public class HashUtil {
    public static String getHash(String source, String type) {
        StringBuilder sb = new StringBuilder();
        MessageDigest md;
        try {
            md = MessageDigest.getInstance(type);
            md.update(source.getBytes());
            for (byte b : md.digest()) {
                sb.append(String.format("%02X", b)); // 10进制转16进制，X 表示以十六进制形式输出，02 表示不足两位前面补0输出
            }
            return sb.toString();
        } catch (Exception e) {
            Log.e("HashUtil", e.getMessage());
        }
        return null;
    }

    public static String getHash(FileInputStream inputStream, String type) {
        StringBuilder sb = new StringBuilder();
        MessageDigest md;
        try {
            md = MessageDigest.getInstance(type);
            byte[] buffer = new byte[1024];
            int length = -1;
            while ((length = inputStream.read(buffer, 0, 1024)) != -1) {
                md.update(buffer, 0, length);
            }
            inputStream.close();
            for (byte b : md.digest()) {
                sb.append(String.format("%02X", b)); // 10进制转16进制，X 表示以十六进制形式输出，02 表示不足两位前面补0输出
            }
            return sb.toString();
        } catch (Exception e) {
            Log.e("HashUtil", e.getMessage());
            ;
        }
        return null;
    }
}
