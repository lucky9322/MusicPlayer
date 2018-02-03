package com.zdd.musicplayer.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Project: MusicPlayer
 * Created by Zdd on 2018/2/3.
 */

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context context, String name) {
        super(context, name, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        initTableForDBMusicoco(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void initTableForDBMusicoco(SQLiteDatabase db) {
        DBMusicocoController.createSheetTable(db);
        DBMusicocoController.createSongTable(db);
    }
}
