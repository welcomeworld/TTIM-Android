package cn.dmandp.tt;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class AboutActivity extends BaseActivity {
    TextView versionTextView;
    String versionName="";
    Handler handler=new Handler();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        updateCheck();
        android.support.v7.widget.Toolbar toolbar=findViewById(R.id.about_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        versionTextView=findViewById(R.id.about_version);
        PackageManager packageManager=getPackageManager();
        try {
            PackageInfo packageInfo=packageManager.getPackageInfo(getPackageName(),0);
            versionName=packageInfo.versionName;
            versionTextView.setText(versionName);
            Button button=findViewById(R.id.about_source);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent updateIntent=new Intent(Intent.ACTION_VIEW);
                    Uri uri=Uri.parse("https://github.com/welcomeworld/TTIM-Android");
                    updateIntent.setData(uri);
                    startActivity(Intent.createChooser(updateIntent,"选择浏览器"));
                }
            });
            button=findViewById(R.id.about_talk);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent updateIntent=new Intent(Intent.ACTION_VIEW);
                    Uri uri=Uri.parse("mqqwpa://im/chat?chat_type=wpa&uin=1303854224");
                    updateIntent.setData(uri);
                    startActivity(Intent.createChooser(updateIntent,"打开QQ"));
                }
            });
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG,e.getMessage());
        }
    }

    private void updateCheck() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url=new URL("https://dmandp.cn/TTIM/VersionCheck");
                    HttpsURLConnection conn= (HttpsURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(2000);
                    if(conn.getResponseCode()==200){
                        InputStream in = conn.getInputStream();
                        byte[] data = read(in);
                        String latest_version = new String(data, "UTF-8");
                        if(!latest_version.equalsIgnoreCase(versionName)){
                            Message message=handler.obtainMessage();
                            message.what=0;
                            Bundle bundle=new Bundle();
                            bundle.putString("versionName",latest_version);
                            message.setData(bundle);
                            handler.sendMessage(message);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG,e.getMessage());
                }
            }
            public  byte[] read(InputStream inStream) throws Exception{
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = 0;
                while((len = inStream.read(buffer)) != -1)
                {
                    outStream.write(buffer,0,len);
                }
                inStream.close();
                return outStream.toByteArray();
            }
        }).start();
    }

    class Handler extends android.os.Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    Button button=findViewById(R.id.about_update_01);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent updateIntent=new Intent(Intent.ACTION_VIEW);
                    Uri uri=Uri.parse("https://dmandp.cn/TTIM/TTIM.apk");
                    updateIntent.setData(uri);
                    startActivity(Intent.createChooser(updateIntent,"选择浏览器"));
                }
            });
                    button=findViewById(R.id.about_update_02);
                    button.setText(msg.getData().getString("versionName",versionName));
                    button.setTextColor(Color.BLUE);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent updateIntent=new Intent(Intent.ACTION_VIEW);
                            Uri uri=Uri.parse("https://dmandp.cn/TTIM/TTIM.apk");
                            updateIntent.setData(uri);
                            startActivity(Intent.createChooser(updateIntent,"选择浏览器"));
                        }
                    });
                    break;
            }
        }
    }
}
