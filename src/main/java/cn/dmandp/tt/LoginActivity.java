package cn.dmandp.tt;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import cn.dmandp.common.TYPE;
import cn.dmandp.context.TtApplication;
import cn.dmandp.entity.TTIMPacket;
import cn.dmandp.entity.TTUser;

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
        //View initialization
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
                break;
            //go to register
            case R.id.login_new_user_button:
                Intent intent = new Intent(this, RegisterActivity.class);
                startActivity(intent);
                break;
            case R.id.login_forget_password_button:
                Toast.makeText(this,getResources().getString(R.string.function_give_up),Toast.LENGTH_SHORT).show();
                break;
            default:
        }
    }
}
