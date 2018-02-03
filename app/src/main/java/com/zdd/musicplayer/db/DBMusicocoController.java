package com.zdd.musicplayer.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.zdd.musicplayer.aidl.Song;
import com.zdd.musicplayer.db.modle.DBSongInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Project: MusicPlayer
 * Created by Zdd on 2018/2/3.
 * <p>
 * 每个线程只能使用一个SQLiteOpenHelper（多线程操作数据库会报错）
 */

public class DBMusicocoController {

    private final Context context;
    private final SQLiteDatabase database;

    private static final String TAG = "DBMusicocoController";

    public static final String DATABASE = "musicoco.db";

    public static final String TABLE_SONG = "song";
    public static final String SONG_ID = "_id"; //主键
    public static final String SONG_PATH = "path"; //路径
    public static final String SONG_LASTPLAYTIME = "last_play"; //最后播放时间
    public static final String SONG_PLAYTIMES = "play_times"; //播放次数
    public static final String SONG_REMARK = "remarks"; //备注
    public static final String SONG_SHEETS = "sheets"; //所属歌单 歌单编号，空格隔开
    public static final String SONG_CREATE = "create_time"; //创建时间
    public static final String SONG_FAVORITE = "song_favorite"; //是否收藏 0 否， 1 是

    public static final String TABLE_SHEET = "sheet";
    public static final String SHEET_ID = "_id"; // 主键
    public static final String SHEET_NAME = "name"; //歌单名称
    public static final String SHEET_REMARK = "remarks"; //歌单备注
    public static final String SHEET_CREATE = "create_time"; //创建时间
    public static final String SHEET_PLAYTIMES = "sheet_playtimes"; //播放次数
    public static final String SHEET_COUNT = "sheet_count"; //歌曲数目

    static void createSongTable(SQLiteDatabase db) {
        String sql = "create table if not exists " + DBMusicocoController.TABLE_SONG + "(" +
                DBMusicocoController.SONG_ID + " integer primary key autoincrement," +
                DBMusicocoController.SONG_PATH + " text unique," +
                DBMusicocoController.SONG_LASTPLAYTIME + " char(20)," +
                DBMusicocoController.SONG_PLAYTIMES + " integer," +
                DBMusicocoController.SONG_REMARK + " text," +
                DBMusicocoController.SONG_SHEETS + " text," +
                DBMusicocoController.SONG_CREATE + " text," +
                DBMusicocoController.SONG_FAVORITE + " integer)";
        db.execSQL(sql);
    }

    static void createSheetTable(SQLiteDatabase db) {

        String sql = "create table if not exists " + DBMusicocoController.TABLE_SHEET + "(" +
                DBMusicocoController.SHEET_ID + " integer primary key autoincrement," +
                DBMusicocoController.SHEET_NAME + " text unique," +
                DBMusicocoController.SHEET_REMARK + " text," +
                DBMusicocoController.SHEET_CREATE + " text," +
                DBMusicocoController.SHEET_PLAYTIMES + " integer," +
                DBMusicocoController.SHEET_COUNT + " integer)";
        db.execSQL(sql);
    }

    /**
     * 在使用结束时应调用{@link #close()}关闭数据库连接
     */
    public DBMusicocoController(Context context, boolean writable) {
        DBHelper helper = new DBHelper(context, DATABASE);
        if (writable) {
            this.database = helper.getWritableDatabase();
        } else {
            this.database = helper.getReadableDatabase();
        }
        this.context = context;
    }

    public void close() {
        if (database.isOpen()) {
            database.close();
        }
    }



    private int[] songSheetsStringToIntArray(String sheets) {
        if (TextUtils.isEmpty(sheets)) {
            return new int[]{};
        }
        String[] strs = sheets.split(" ");
        int[] shs = new int[strs.length];
        for (int i = 0; i < shs.length; i++) {
            shs[i] = Integer.parseInt(strs[i]);
        }
        return shs;
    }


    public List<DBSongInfo> getSongInfos() {
        String sql = "select * from " + TABLE_SONG;
        Cursor cursor = database.rawQuery(sql, null);
        List<DBSongInfo> infos = new ArrayList<>();
        while (cursor.moveToNext()) {
            DBSongInfo info = new DBSongInfo();
            info.id = cursor.getInt(cursor.getColumnIndex(SONG_ID));
            info.path = cursor.getString(cursor.getColumnIndex(SONG_PATH));

            String str = cursor.getString(cursor.getColumnIndex(SONG_LASTPLAYTIME));
            info.lastPlayTime = Long.valueOf(str);

            info.playTimes = cursor.getInt(cursor.getColumnIndex(SONG_PLAYTIMES));
            info.remark = cursor.getString(cursor.getColumnIndex(SONG_REMARK));

            String str1 = cursor.getString(cursor.getColumnIndex(SONG_CREATE));
            info.create = Long.valueOf(str1);

            int far = cursor.getInt(cursor.getColumnIndex(SONG_FAVORITE));
            info.favorite = far == 1;

            String sh = cursor.getString(cursor.getColumnIndex(SONG_SHEETS));
            info.sheets = songSheetsStringToIntArray(sh);

            infos.add(info);
        }

        cursor.close();
        return infos;
    }

    public List<DBSongInfo> getSongInfos(int sheetID) {

        String sql = "select * from " + TABLE_SONG;
        Cursor cursor = database.rawQuery(sql, null);

        List<DBSongInfo> infos = new ArrayList<>();

        while (cursor.moveToNext()) {
            DBSongInfo info = new DBSongInfo();

            String sh = cursor.getString(cursor.getColumnIndex(SONG_SHEETS));
            int[] shs = songSheetsStringToIntArray(sh);

            boolean isContain = false;
            for (int i : shs) {
                if (i == sheetID) {
                    isContain = true;
                }
            }

            if (isContain) {
                info.sheets = shs;

                info.id = cursor.getInt(cursor.getColumnIndex(SONG_ID));
                info.path = cursor.getString(cursor.getColumnIndex(SONG_PATH));

                String str = cursor.getString(cursor.getColumnIndex(SONG_LASTPLAYTIME));
                info.lastPlayTime = Long.valueOf(str);

                info.playTimes = cursor.getInt(cursor.getColumnIndex(SONG_PLAYTIMES));
                info.remark = cursor.getString(cursor.getColumnIndex(SONG_REMARK));

                String str1 = cursor.getString(cursor.getColumnIndex(SONG_CREATE));
                info.create = Long.valueOf(str1);

                int far = cursor.getInt(cursor.getColumnIndex(SONG_FAVORITE));
                info.favorite = far == 1;

                infos.add(info);
            }
        }

        cursor.close();
        return infos;
    }

}
