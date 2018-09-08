package cn.dmandp.tt;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import cn.dmandp.common.TYPE;
import cn.dmandp.context.TtApplication;
import cn.dmandp.entity.TTIMPacket;
import cn.dmandp.entity.TTUser;

/**
 * Created by 萌即正义 on 26/03/2018.
 */

public class RegisterActivity extends BaseActivity implements View.OnClickListener {
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
                int uId;
                try {
                    uId = Integer.parseInt(accountText.getText().toString());
                } catch (Exception e) {
                    return ;
                }
                TTUser.Builder builder = TTUser.newBuilder();
                builder.setUPassword(passwordText.getText().toString());
                builder.setUName(uNameText.getText().toString());
                builder.setUId(uId);
                TTUser registerUser = builder.build();
                TTIMPacket registerPacket=new TTIMPacket();
                registerPacket.setTYPE(TYPE.REGISTER_REQ);
                registerPacket.setBody(registerUser.toByteArray());
                registerPacket.setBodylength(registerUser.toByteArray().length);
                TtApplication.send(registerPacket);
                TtApplication.getSessionContext().setAttribute("registerUser", registerUser);
                break;
            case R.id.register_login_button:
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
