package cn.dmandp.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by 萌即正义 on 30/03/2018.
 */

public class TTIMDaoHelper extends SQLiteOpenHelper {
    private final static String DATABASE_NAME = "ttimDatabase";
    private final static Integer DATABASE_VERSION = 2;
    private final String CREATE_FRIENDS = "create table friends (uid int not null,friendid int not null," +
            "Uname varchar(30) not null,Primary key (uid,friendid))";
    private final String CREATE_MESSAGES = "create table messages(mcontent varchar(400) not null," +
            "Mtime bigint not null," +
            "Fromid int not null," +
            "Toid int not null," +
            "Primary key (fromid,toid,mtime))";
    private final String CREATE_FAVORITE = "CREATE TABLE `favorite` (saveuserid int(11) NOT NULL ,  " +
            "`mcontent` varchar(400) NOT NULL,   " +
            "`mtime` bigint(20) NOT NULL,   " +
            "`fromid` int(11) NOT NULL,   " +
            "`toid` int(11) NOT NULL,   " +
            "PRIMARY KEY (saveuserid,`fromid`,`toid`,`mtime`) );";
    private final String CREATE_REQUESTS="CREATE TABLE `requests` (" +
            "  `rcontent` varchar(400) DEFAULT NULL," +
            "  `rtime` bigint(20) NOT NULL," +
            "  `fromid` int(11) NOT NULL," +
            "  `toid` int(11) NOT NULL," +
            "  `rtype` int(2) NOT NULL," +
            "  `rstatus` int(2) DEFAULT 0,"+
            "PRIMARY KEY (`fromid`,`toid`,`rtime`));";

    private Context mContext;

    public TTIMDaoHelper(Context mContext, SQLiteDatabase.CursorFactory factory) {
        super(mContext, DATABASE_NAME, factory, DATABASE_VERSION);
        this.mContext = mContext;
    }

    public TTIMDaoHelper(Context mContext) {
        super(mContext, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = mContext;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_FRIENDS);
        db.execSQL(CREATE_MESSAGES);
        db.execSQL(CREATE_FAVORITE);
        db.execSQL(CREATE_REQUESTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion){
            case 1:
                db.execSQL(CREATE_REQUESTS);
        }
    }
}
