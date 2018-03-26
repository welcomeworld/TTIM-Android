package cn.dmandp.tt;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
        EditText accountText = (EditText) findViewById(R.id.login_account_edittext);
        EditText passwordText = (EditText) findViewById(R.id.login_password_edittext);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_login_button:

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.login_new_user_button:

            default:
        }
    }
}
