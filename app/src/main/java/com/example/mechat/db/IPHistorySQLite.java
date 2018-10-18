package com.example.mechat.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class IPHistorySQLite extends SQLiteOpenHelper {
    public static final String SQL_NAME_HISTORY = "ip_port_history";
    public static final String TABLE_NAME_HISTORY = "ip_port_tb";
    private static IPHistorySQLite sqLite;

    private IPHistorySQLite(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public static IPHistorySQLite getSqLite(Context context) {
        if (sqLite == null)
            sqLite = new IPHistorySQLite(context, SQL_NAME_HISTORY, null, 1);
        return sqLite;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME_HISTORY + "(" +
                "_id integer primary key," +
                "ip varchar(11) not null," +
                "port integer not null," +
                "date timestamp not null default (datetime('now','localtime'))" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean add(String ip, int port) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("ip", ip);
        contentValues.put("port", port);
        return getWritableDatabase().insert(TABLE_NAME_HISTORY, null, contentValues) > 0;
    }

    public ArrayList<String> queryTop(int how) {
        ArrayList<String> strings = new ArrayList<>(how);
        Cursor cur = getReadableDatabase().rawQuery("select ip,port from " + TABLE_NAME_HISTORY + " order by date limit 0,?", new String[]{(how - 1) + ""});
        if (cur.moveToFirst()) {
            do {
                String v = cur.getString(0) + ":" + cur.getString(1);
                strings.add(v);
            } while (cur.moveToNext());
        }
        return strings;
    }

    @Override
    public synchronized void close() {
        super.close();
        sqLite = null;
    }
}
