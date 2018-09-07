/*
 * Copyright (c) 2018. welcomeworld All rights reserved
 */

package cn.dmandp.tt;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import java.io.File;
import java.io.IOException;
import cn.dmandp.common.TYPE;
import cn.dmandp.netio.FileThread;
import cn.dmandp.service.MessageService;
import cn.dmandp.utils.ImageUtil;

public class PersonInfoActivity extends BaseActivity {
    private int personInfoUserId;
    final private int CONTENT_PHOTO_REQUEST = 1;
    final private int PHOTO_CROP_REQUEST = 3;
    ImageButton personInfo_photo_button;
    TextView personInfo_username;
    TextView personInfo_userid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences data = getSharedPreferences("data", MODE_PRIVATE);
        int currentUserId = data.getInt("currentUserId", -1);
        Bundle bundle = getIntent().getExtras();
        if(bundle==null){
            finish();
            return;
        }
        personInfoUserId = bundle.getInt("uId");
        String personInfoUserName = bundle.getString("uName");
        setContentView(R.layout.activity_personinfo);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(getResources().getColor(R.color.transparent));
        }
        personInfo_username = findViewById(R.id.personinfo_username);
        personInfo_username.setText(personInfoUserName);
        personInfo_userid=findViewById(R.id.personinfo_userid);
        String useridText="uid:"+personInfoUserId;
        personInfo_userid.setText(useridText);
        personInfo_photo_button = findViewById(R.id.personinfo_photo);
        Bitmap photo = BitmapFactory.decodeFile(getFilesDir().getAbsolutePath() + "/head_portrait/" + personInfoUserId + ".png");
        if (photo == null) {
            Log.d("MessageService", "File is not exist");
            photo = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
            Bundle fileBundle = new Bundle();
            fileBundle.putInt("uid",personInfoUserId);
            fileBundle.putByte("type", TYPE.USERPHOTO_GET_REQ);
            new FileThread(PersonInfoActivity.this, fileBundle, MessageService.getInstance().getHandler()).start();
        }
        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), photo);
        roundedBitmapDrawable.setCircular(true);
        personInfo_photo_button.setImageDrawable(roundedBitmapDrawable);
        if(personInfoUserId== currentUserId){
            personInfo_photo_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/png");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(Intent.createChooser(intent, "请选择你的头像文件"), CONTENT_PHOTO_REQUEST);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == CONTENT_PHOTO_REQUEST) {
            imageCrop(data.getData());
        }
        if (resultCode == Activity.RESULT_OK && requestCode == PHOTO_CROP_REQUEST) {
            Bitmap photo = BitmapFactory.decodeFile(getExternalCacheDir() + "/temp.png");
            if (photo == null) {
                return;
            }
            ImageUtil.sizeCompressImage(photo, getFilesDir().getAbsolutePath() + "/head_portrait/" + personInfoUserId + ".png");
            photo = ImageUtil.samplingRateCompressImage(getFilesDir().getAbsolutePath() + "/head_portrait/" + personInfoUserId + ".png", getFilesDir().getAbsolutePath() + "/head_portrait/" + personInfoUserId + ".png");
            if(photo!=null){
                photo = ImageUtil.qualityCompressImage(photo, getFilesDir().getAbsolutePath() + "/head_portrait/" + personInfoUserId + ".png");
            }
            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), photo);
            roundedBitmapDrawable.setCircular(true);
            personInfo_photo_button.setImageDrawable(roundedBitmapDrawable);
            Bundle fileBundle = new Bundle();
            fileBundle.putInt("uid", personInfoUserId);
            fileBundle.putByte("type", TYPE.USERPHOTO_SET_REQ);
            new FileThread(PersonInfoActivity.this, fileBundle, MessageService.getInstance().getHandler()).start();
        }
    }

    private void imageCrop(Uri uri) {
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setDataAndType(uri, "image/*");
        cropIntent.putExtra("crop", "true");
        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1);
        cropIntent.putExtra("outputX", 300);
        cropIntent.putExtra("outputY", 300);
        File tempFile = new File(getExternalCacheDir(), "/temp.png");
        if (!tempFile.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                tempFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Uri fileUri = Uri.parse("file://" + getExternalCacheDir() + "/temp.png");
        cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        cropIntent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
        startActivityForResult(cropIntent, PHOTO_CROP_REQUEST);
    }
}
