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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_login_button:
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                //Toast.makeText(LoginActivity.this,"ldfjkjjd",Toast.LENGTH_SHORT).show();
                startActivity(intent);
                break;
            default:
        }
    }
}
