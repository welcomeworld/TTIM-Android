package cn.dmandp.tt;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import cn.dmandp.context.SessionContext;

/**
 * Created by 萌即正义 on 13/04/2018.
 */

public class BaseActivity extends AppCompatActivity {
    String TAG = "BaseActivity";
    private boolean foreground = false;

    public boolean isForeground() {
        return foreground;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = getClass().getSimpleName();
        Log.d("BaseActivity", getClass().getSimpleName());
        SessionContext.activities.put(getClass().getSimpleName(), this);
        ActivityCollector.addActivity(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStart() {
        super.onStart();
        foreground = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        foreground = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SessionContext.activities.remove(getClass().getSimpleName());
        ActivityCollector.removeActivity(this);
    }
}
