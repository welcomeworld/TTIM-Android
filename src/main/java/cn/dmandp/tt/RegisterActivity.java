package cn.dmandp.tt;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import cn.dmandp.common.SessionContext;
import cn.dmandp.common.TTUser;
import cn.dmandp.common.TYPE;
import cn.dmandp.context.TtApplication;
import cn.dmandp.netio.Result;

/**
 * Created by 萌即正义 on 26/03/2018.
 */

public class RegisterActivity extends Activity implements View.OnClickListener {
    Button newButton;
    Button loginButton;
    EditText accountText;
    EditText passwordText;
    EditText uNameText;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        loginButton = findViewById(R.id.register_login_button);
        loginButton.setOnClickListener(this);
        newButton = findViewById(R.id.register_register_button);
        newButton.setOnClickListener(this);
        accountText = findViewById(R.id.register_account_edittext);
        passwordText = findViewById(R.id.register_password_edittext);
        uNameText = findViewById(R.id.register_uname_edittext);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register_register_button:
                new RegisterTask().execute(accountText.getText().toString(), passwordText.getText().toString(), uNameText.getText().toString());
                break;
            case R.id.register_login_button:
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    class RegisterTask extends AsyncTask<String, String, Result> {

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
            TTUser.Builder builder = TTUser.newBuilder();
            builder.setUPassword(strings[1]);
            builder.setUName(strings[2]);
            builder.setUId(uId);
            TTUser registerUser = builder.build();
            TtApplication application = (TtApplication) getApplication();
            SessionContext sessionContext = application.getSessionContext();
            if (sessionContext == null) {
                Log.i("RegisterActivity", "sessionContext is null");
                result.setResultStatus((byte) 0);
                result.setResultBody("sessionContext create fail please check your network!");
                return result;
            }
            SocketChannel socketChannel = sessionContext.getSocketChannel();
            ByteBuffer registerBuffer = ByteBuffer.allocate(Const.BYTEBUFFER_MAX);
            ByteBuffer receiveBuffer = ByteBuffer.allocate(Const.BYTEBUFFER_MAX);
            registerBuffer.put(TYPE.REGISTER_REQ);
            registerBuffer.put(registerUser.toByteArray());
            registerBuffer.flip();
            try {
                socketChannel.write(registerBuffer);
                socketChannel.read(receiveBuffer);
            } catch (IOException e) {
                Log.e("RegisterActivity", "Failure of server communication cause:" + e.getMessage());
                result.setResultStatus((byte) 0);
                result.setResultBody("Failure of server communication");
                return result;
            }
            receiveBuffer.flip();
            byte[] responsecontent = new byte[receiveBuffer.remaining()];
            receiveBuffer.get(responsecontent);
            if (responsecontent[0] == TYPE.REGISTER_RESP) {
                if (responsecontent[1] == RESP_CODE.SUCCESS) {
                    result.setResultStatus((byte) 1);
                    return result;
                }
                result.setResultStatus((byte) 0);
                result.setResultBody(new String(responsecontent, 2, responsecontent.length - 2));
                return result;
            }
            result.setResultStatus((byte) 0);
            result.setResultBody("unknown server response：" + new String(responsecontent));
            return result;
        }

        @Override
        protected void onPostExecute(Result result) {
            if (result.getResultStatus() == 1) {
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
                SessionContext sessionContext = ((TtApplication) getApplication()).getSessionContext();
                sessionContext.setLogin(true);
                //尚未完成
            } else {
                Toast.makeText(RegisterActivity.this, (String) result.getResultBody(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
