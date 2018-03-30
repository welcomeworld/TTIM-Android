package cn.dmandp.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by 萌即正义 on 30/03/2018.
 */

public class TTIMDaoHelper extends SQLiteOpenHelper {
    private final String CREATE_FRIENDS = "create table friends (uid int primary key not null,\n" +
            "Uname varchar(30) not null,\n" +
            "Uphoto varchar(30),Upassword varchar(30))";

    private Context mContext;

    public TTIMDaoHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, Context mContext) {
        super(context, name, factory, version);
        this.mContext = mContext;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_FRIENDS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
