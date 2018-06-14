package cn.dmandp.tt;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executors;

import cn.dmandp.common.Const;
import cn.dmandp.common.RESP_CODE;
import cn.dmandp.common.TYPE;
import cn.dmandp.context.SessionContext;
import cn.dmandp.context.TtApplication;
import cn.dmandp.entity.TTIMPacket;
import cn.dmandp.entity.TTUser;
import cn.dmandp.netio.Result;
import cn.dmandp.view.LoadView;

public class LoginActivity extends BaseActivity implements View.OnClickListener {
    Button newButton;
    Button forgetButton;
    Button loginButton;
    EditText accountText;
    EditText passwordText;
    public LoadView loadView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //View initialization
        loginButton = findViewById(R.id.login_login_button);
        loginButton.setOnClickListener(this);
        newButton = findViewById(R.id.login_new_user_button);
        newButton.setOnClickListener(this);
        forgetButton = findViewById(R.id.login_forget_password_button);
        forgetButton.setOnClickListener(this);
        accountText = findViewById(R.id.login_account_edittext);
        passwordText = findViewById(R.id.login_password_edittext);
        loadView = findViewById(R.id.login_loadview);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //start loginTask
            case R.id.login_login_button:
                int uId;
                try {
                    uId = Integer.parseInt(accountText.getText().toString());
                } catch (Exception e) {
                    Toast.makeText(LoginActivity.this, "账号不是数字！", Toast.LENGTH_SHORT).show();
                    return;
                }
                String uPassword = passwordText.getText().toString();
                TTIMPacket loginPacket = new TTIMPacket();
                loginPacket.setTYPE(TYPE.LOGIN_REQ);
                TTUser.Builder builder = TTUser.newBuilder();
                builder.setUId(uId);
                builder.setUPassword(uPassword);
                TTUser loginuser = builder.build();
                byte[] body = loginuser.toByteArray();
                loginPacket.setBody(body);
                loginPacket.setBodylength(body.length);
                TtApplication.send(loginPacket);
                //save password temporary
                TtApplication.getSessionContext().setAttribute("loginpassword", uPassword);
                //new LoginTask().executeOnExecutor(Executors.newCachedThreadPool(), accountText.getText().toString(), passwordText.getText().toString());
                break;
            //go to register
            case R.id.login_new_user_button:
                Intent intent = new Intent(this, RegisterActivity.class);
                startActivity(intent);
                break;
            default:
        }
    }

    class LoginTask extends AsyncTask<String, String, Result> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadView.setVisibility(View.VISIBLE);
        }

        @Override
        protected Result doInBackground(String... strings) {
            Result result = new Result();
            int uId;
            //parse uid to integer
            try {
                uId = Integer.parseInt(strings[0]);
            } catch (Exception e) {
                result.setResultStatus((byte) 0);
                result.setResultBody("账号不是数字！");
                return result;
            }
            String uPassword = strings[1];
            TtApplication application = (TtApplication) getApplication();
            SessionContext sessionContext = application.getSessionContext();
            SocketChannel socketChannel = sessionContext.getSocketChannel();
            //socketChannel is null so return error message
            if (socketChannel == null) {
                Log.i("LoginActivity", "socketChannel is null");
                result.setResultStatus((byte) 0);
                if (sessionContext.socketChannelErrorMessage != null) {
                    result.setResultBody(sessionContext.socketChannelErrorMessage);
                } else {
                    result.setResultBody("未知错误！");
                }
                return result;
            }
            try {
                ByteBuffer byteBuffer = ByteBuffer.allocate(514);
                byteBuffer.put(TYPE.LOGIN_REQ);
                TTUser.Builder builder = TTUser.newBuilder();
                builder.setUId(uId);
                builder.setUPassword(uPassword);
                TTUser loginuser = builder.build();
                byte[] body = loginuser.toByteArray();
                byteBuffer.putInt(body.length);
                byteBuffer.put(body);
                byteBuffer.flip();
                //send login request to server
                while (byteBuffer.hasRemaining()) {
                    socketChannel.write(byteBuffer);
                }
                //save password temporary
                sessionContext.setAttribute("loginpassword", uPassword);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                result.setResultStatus((byte) 1);
                result.setResultBody("服务器响应超时！");
                return result;
            } catch (IOException e) {
                result.setResultStatus((byte) 0);
                Log.e("LoginActivity", e.getMessage());
                result.setResultBody("服务器通讯错误！");
                return result;
            }
        }

        @Override
        protected void onPostExecute(Result result) {
            if (result.getResultStatus() == 1) {
                //if loadView is still Visibility,the login failed
                if (loadView.getVisibility() == View.VISIBLE) {
                    Toast.makeText(LoginActivity.this, (String) result.getResultBody(), Toast.LENGTH_SHORT).show();
                    loadView.setVisibility(View.GONE);
                }
            } else {
                Toast.makeText(LoginActivity.this, (String) result.getResultBody(), Toast.LENGTH_SHORT).show();
                loadView.setVisibility(View.GONE);
            }
        }
    }
}
