package cn.dmandp.tt;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import cn.dmandp.common.Const;
import cn.dmandp.common.RESP_CODE;
import cn.dmandp.common.SessionContext;
import cn.dmandp.common.TTUser;
import cn.dmandp.common.TYPE;
import cn.dmandp.context.TtApplication;

public class LoginActivity extends Activity implements View.OnClickListener {
    Button newButton;
    Button forgetButton;
    Button loginButton;
    EditText accountText;
    EditText passwordText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginButton = (Button) findViewById(R.id.login_login_button);
        loginButton.setOnClickListener(this);
        newButton = (Button) findViewById(R.id.login_new_user_button);
        newButton.setOnClickListener(this);
        forgetButton = (Button) findViewById(R.id.login_forget_password_button);
        forgetButton.setOnClickListener(this);
        accountText = (EditText) findViewById(R.id.login_account_edittext);
        passwordText = (EditText) findViewById(R.id.login_password_edittext);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_login_button:
                new LoginTask().execute(accountText.getText().toString(), passwordText.getText().toString());
                break;
            case R.id.login_new_user_button:

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
                result.setResultcode((byte) 0);
                result.setResultMessage("账号不是数字！");
                return result;
            }
            String uPassword = strings[1];
            TtApplication application = (TtApplication) getApplication();
            SessionContext sessionContext = application.getSessionContext();
            if (sessionContext == null) {
                Log.i("LoginActivity", "sessionContext is null");
                result.setResultcode((byte) 0);
                result.setResultMessage("sessionContext create fail please check your network!");
                return result;
            }
            SocketChannel socketChannel = sessionContext.getSocketChannel();
            try {
                ByteBuffer byteBuffer = ByteBuffer.allocate(Const.BYTEBUFFER_MAX);
                ByteBuffer receiveBuffer = ByteBuffer.allocate(Const.BYTEBUFFER_MAX);
                byteBuffer.put(TYPE.LOGIN_REQ);
                TTUser.Builder builder = TTUser.newBuilder();
                builder.setUId(uId);
                builder.setUPassword(uPassword);
                TTUser loginuser = builder.build();
                byteBuffer.put(loginuser.toByteArray());
                byteBuffer.flip();
                socketChannel.write(byteBuffer);
                socketChannel.read(receiveBuffer);
                receiveBuffer.flip();
                byte[] responsecontent = new byte[receiveBuffer.remaining()];
                receiveBuffer.get(responsecontent);
                if (responsecontent[0] == TYPE.LOGIN_RESP) {
                    if (responsecontent[1] == RESP_CODE.SUCCESS) {
                        result.setResultcode((byte) 1);
                        return result;
                    }
                    result.setResultcode((byte) 0);
                    result.setResultMessage(new String(responsecontent, 2, responsecontent.length - 2));
                    return result;
                } else {
                    result.setResultcode((byte) 0);
                    result.setResultMessage("未知的服务器响应：" + new String(responsecontent));
                    return result;
                }
            } catch (IOException e) {
                result.setResultcode((byte) 0);
                Log.e("LoginActivity", e.getMessage());
                result.setResultMessage("服务器通讯错误！");
                return result;
            }
        }

        @Override
        protected void onPostExecute(Result result) {
            if (result.getResultcode() == 1) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(LoginActivity.this, result.getResultMessage(), Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(result);
        }
    }

    class Result {
        private byte resultcode;
        private String resultMessage;

        public byte getResultcode() {
            return resultcode;
        }

        public void setResultcode(byte resultcode) {
            this.resultcode = resultcode;
        }

        public String getResultMessage() {
            return resultMessage;
        }

        public void setResultMessage(String resultMessage) {
            this.resultMessage = resultMessage;
        }
    }
}
