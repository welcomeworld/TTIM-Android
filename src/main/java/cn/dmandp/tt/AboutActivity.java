package cn.dmandp.tt;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

public class AboutActivity extends BaseActivity {
    TextView versionTextView;
    String versionName="";
    Handler handler=new Handler();
    Toolbar toolbar;
    Button sourceButton;
    Button talkButton;
    LinearLayout updateLayout;
    Button updateTip;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        toolbar=findViewById(R.id.about_toolbar);
        sourceButton=findViewById(R.id.about_source);
        versionTextView=findViewById(R.id.about_version);
        talkButton=findViewById(R.id.about_talk);
        updateLayout=findViewById(R.id.about_update);
        updateTip=findViewById(R.id.about_update_tip);
        init();
        updateCheck();
    }

    private void init() {
        sourceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent updateIntent=new Intent(Intent.ACTION_VIEW);
                Uri uri=Uri.parse("https://github.com/welcomeworld/TTIM-Android");
                updateIntent.setData(uri);
                startActivity(Intent.createChooser(updateIntent,"选择浏览器"));
            }
        });
        talkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent updateIntent=new Intent(Intent.ACTION_VIEW);
                Uri uri=Uri.parse("mqqwpa://im/chat?chat_type=wpa&uin=1303854224");
                updateIntent.setData(uri);
                startActivity(Intent.createChooser(updateIntent,"打开QQ"));
            }
        });
        updateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCheck();
            }
        });
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        PackageManager packageManager=getPackageManager();
        try {
            PackageInfo packageInfo=packageManager.getPackageInfo(getPackageName(),0);
            versionName=packageInfo.versionName;
            versionTextView.setText(versionName);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG,e.getMessage());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
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
            private  byte[] read(InputStream inStream) throws Exception{
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while((len = inStream.read(buffer)) != -1)
                {
                    outStream.write(buffer,0,len);
                }
                inStream.close();
                return outStream.toByteArray();
            }
        }).start();
    }

    @SuppressLint("HandlerLeak")
    class Handler extends android.os.Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    updateTip.setText(msg.getData().getString("versionName",versionName));
                    updateTip.setTextColor(Color.BLUE);
                    updateLayout.setOnClickListener(new View.OnClickListener() {
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
