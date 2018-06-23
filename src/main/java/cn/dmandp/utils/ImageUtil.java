/*
 * Copyright (c) 2018. welcomeworld All rights reserved
 */

package cn.dmandp.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class ImageUtil {

    /**
     * scale bitmap by  samplingRate from bitmap
     *
     * @param srcPath
     * @param desPath
     * @return
     */
    public static Bitmap samplingRateCompressImage(String srcPath, String desPath) {
        FileOutputStream fos = null;
        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(srcPath, op);
        op.inJustDecodeBounds=false;
        float w = op.outWidth;
        float h = op.outHeight;
        float hh = 640f;
        float ww = 640f;
        float be = 1.0f;
        if (w > h && w > ww) {
            be = w / ww;
        } else if (w < h && h > hh) {
            be = h / hh;
        }
        if (be <= 0) {
            be = 1.0f;
        }
        op.inSampleSize = (int) be;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath,op);
        try {
            if (desPath != null) {
                fos = new FileOutputStream(desPath);
                if (bitmap != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                }
            }
            return bitmap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * compress image by change size
     * redraw bitmap
     */
    public static Bitmap sizeCompressImage(Bitmap bitmap, String desPath) {
        FileOutputStream fos = null;
        if (bitmap.getHeight() > 640 || bitmap.getWidth() > 640) {
            Bitmap result = Bitmap.createBitmap(640, 640, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            Rect rect = new Rect(0, 0, 640, 640);
            canvas.drawBitmap(bitmap, null, rect, null);
            bitmap = result;
        }
        try {
            if (desPath != null) {
                fos = new FileOutputStream(desPath);
                if (bitmap != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                }
            }
            return bitmap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    //quality scale
    public static Bitmap qualityCompressImage(Bitmap image, String destPath) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 90;

        while (baos.toByteArray().length / 1024 > 100 && options > 10) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset(); // 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.PNG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;// 每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
        if (destPath != null && bitmap != null) {
            try {
                FileOutputStream fos = new FileOutputStream(destPath);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    public static Bitmap drawToBitmap(Drawable drawable) {
        // 取 drawable 的长宽
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();

        // 取 drawable 的颜色格式
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565;
        // 建立对应 bitmap
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        // 建立对应 bitmap 的画布
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        // 把 drawable 内容画到画布中
        drawable.draw(canvas);
        return bitmap;
    }
}
