package com.islavstan.free_talker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import rx.Observable;


public class DBHelper extends SQLiteOpenHelper {

    private static DBHelper sInstance;
    private Context context;
    private SQLiteDatabase db;

    public static DBHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DBHelper(context.getApplicationContext());
        }
        return sInstance;

    }

    private DBHelper(Context context) {
        super(context, "TalkerDB", null, 1);
        this.context = context;
        db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table badUsers ("
                + "id integer primary key autoincrement,"
                + "user_id integer);");

        db.execSQL("create table likeUsers ("
                + "id integer primary key autoincrement,"
                + "user_id integer, name text);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    public Observable<Integer> blockUser(int id) {
        return Observable.fromCallable(() -> {
            ContentValues contentValues = new ContentValues();
            contentValues.put("user_id", id);
            db.insert("badUsers", null, contentValues);
            return 1;
        });


    }


    public Observable<Integer> getBlockUser(int id) {
        return Observable.fromCallable(() -> {
            Cursor c = db.rawQuery("SELECT * FROM badUsers where user_id = '" + id + "'", null);
            int i = 0;
            if (c.moveToFirst())
                i = 1;
            c.close();
            return i;
        });


    }


}
