package cn.dmandp.tt;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import cn.dmandp.common.Const;
import cn.dmandp.common.RESP_CODE;
import cn.dmandp.common.TYPE;
import cn.dmandp.context.SessionContext;
import cn.dmandp.context.TtApplication;
import cn.dmandp.entity.TTUser;
import cn.dmandp.netio.Result;

public class LoginActivity extends BaseActivity implements View.OnClickListener {
    Button newButton;
    Button forgetButton;
    Button loginButton;
    EditText accountText;
    EditText passwordText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginButton = findViewById(R.id.login_login_button);
        loginButton.setOnClickListener(this);
        newButton = findViewById(R.id.login_new_user_button);
        newButton.setOnClickListener(this);
        forgetButton = findViewById(R.id.login_forget_password_button);
        forgetButton.setOnClickListener(this);
        accountText = findViewById(R.id.login_account_edittext);
        passwordText = findViewById(R.id.login_password_edittext);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_login_button:
                new LoginTask().execute(accountText.getText().toString(), passwordText.getText().toString());
                break;
            case R.id.login_new_user_button:
                Intent intent = new Intent(this, RegisterActivity.class);
                startActivity(intent);
                break;
            default:
        }
    }

    class LoginTask extends AsyncTask<String, String, Result> {
        @Override
        protected Result doInBackground(String... strings) {
            Result result = new Result();
            int uId;
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
                ByteBuffer byteBuffer = ByteBuffer.allocate(Const.BYTEBUFFER_MAX);
                ByteBuffer receiveBuffer = ByteBuffer.allocate(Const.BYTEBUFFER_MAX);
                byteBuffer.put(TYPE.LOGIN_REQ);
                TTUser.Builder builder = TTUser.newBuilder();
                builder.setUId(uId);
                builder.setUPassword(uPassword);
                TTUser loginuser = builder.build();
                byte[] body = loginuser.toByteArray();
                byteBuffer.putInt(body.length);
                byteBuffer.put(body);
                byteBuffer.flip();
                while (byteBuffer.hasRemaining()) {
                    socketChannel.write(byteBuffer);
                }
                Log.e("TTIM-LoginActivity", "socketChannelHashCode" + socketChannel.hashCode());
                socketChannel.read(receiveBuffer);
                receiveBuffer.flip();
                byte[] responsecontent = new byte[receiveBuffer.remaining()];
                if (receiveBuffer.get() == TYPE.LOGIN_RESP) {
                    int length = receiveBuffer.getInt();
                    byte code = receiveBuffer.get();
                    receiveBuffer.get(responsecontent, 0, length - 1);
                    if (code == RESP_CODE.SUCCESS) {
                        result.setResultStatus((byte) 1);
                        TTUser.Builder returnUserBuilder = TTUser.newBuilder(TTUser.parseFrom(responsecontent));
                        returnUserBuilder.setUPassword(loginuser.getUPassword());
                        result.setResultBody(returnUserBuilder.build());
                        return result;
                    }
                    result.setResultStatus((byte) 0);
                    result.setResultBody(new String(responsecontent, 2, responsecontent.length - 2));
                    return result;
                } else {
                    result.setResultStatus((byte) 0);
                    result.setResultBody("未知的服务器响应：" + new String(receiveBuffer.array()));
                    return result;
                }
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
                TTUser currentUser = (TTUser) result.getResultBody();
                SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                editor.putInt("currentUserId", currentUser.getUId());
                editor.putString("currentUserPassword", currentUser.getUPassword());
                editor.commit();
                SessionContext sessionContext = ((TtApplication) getApplication()).getSessionContext();
                sessionContext.setLogin(true);
                sessionContext.setuID(currentUser.getUId());
                sessionContext.setBindUser(currentUser);
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(LoginActivity.this, (String) result.getResultBody(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
