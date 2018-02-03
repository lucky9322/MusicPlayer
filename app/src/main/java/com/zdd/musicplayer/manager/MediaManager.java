package com.zdd.musicplayer.manager;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.zdd.musicplayer.aidl.Song;
import com.zdd.musicplayer.modle.SongInfo;
import com.zdd.musicplayer.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


/**
 * Project: MusicPlayer
 * Created by Zdd on 2018/2/2.
 */

public class MediaManager {

    private HashSet<SongInfo> songs;
    private static volatile MediaManager MEDIAMANAGER;

    private MediaManager() {

    }

    //传入 Application Context
    public static MediaManager getInstance() {
        if (MEDIAMANAGER == null) {
            synchronized (MediaManager.class) {
                if (MEDIAMANAGER == null)
                    MEDIAMANAGER = new MediaManager();
            }
        }
        return MEDIAMANAGER;
    }

    public HashSet<SongInfo> refreshData(Context context) {
        if (null == songs) {
            songs = new HashSet<>();
        } else {
            songs.clear();
        }
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null,
                null, null);
        if (cursor == null) {
            return songs;
        }
        while (cursor.moveToNext()) {
            SongInfo song = new SongInfo();
            song.setAlbum_id(cursor.getString(cursor.getColumnIndex(SongInfo.ALBUM_ID)));
            song.setAlbum_path(getAlbumArtPicPath(context, song.getAlbum_id()));
            song.setTitle_key(cursor.getString(cursor.getColumnIndex(SongInfo.TITLE_KEY)));
            song.setArtist_key(cursor.getString(cursor.getColumnIndex(SongInfo.ARTIST_KEY)));
            song.setAlbum_key(cursor.getString(cursor.getColumnIndex(SongInfo.ALBUM_KEY)));
            song.setArtist(cursor.getString(cursor.getColumnIndex(SongInfo.ARTIST)));
            song.setAlbum(cursor.getString(cursor.getColumnIndex(SongInfo.ALBUM)));
            song.setData(cursor.getString(cursor.getColumnIndex(SongInfo.DATA)));
            song.setDisplay_name(cursor.getString(cursor.getColumnIndex(SongInfo.DISPLAY_NAME)));
            song.setTitle(cursor.getString(cursor.getColumnIndex(SongInfo.TITLE)));
            song.setMime_type(cursor.getString(cursor.getColumnIndex(SongInfo.MIME_TYPE)));
            song.setYear(cursor.getLong(cursor.getColumnIndex(SongInfo.YEAR)));
            song.setDuration(cursor.getLong(cursor.getColumnIndex(SongInfo.DURATION)));
            song.setSize(cursor.getLong(cursor.getColumnIndex(SongInfo.SIZE)));
            song.setDate_added(cursor.getLong(cursor.getColumnIndex(SongInfo.DATE_ADDED)));
            song.setDate_modified(cursor.getLong(cursor.getColumnIndex(SongInfo.DATE_MODIFIED)));

            songs.add(song);
        }
        cursor.close();

        return songs;
    }

    //根据专辑 id 获得专辑图片保存路径
    private synchronized String getAlbumArtPicPath(Context context, String albumId) {

        // 小米应用商店检测crash ，错误信息：[31188,0,com.duan.musicoco,13155908,java.lang.IllegalStateException,Unknown URL: content://media/external/audio/albums/null,Parcel.java,1548]
        if (!StringUtils.isReal(albumId)) {
            return null;
        }

        String[] projection = {MediaStore.Audio.Albums.ALBUM_ART};
        String imagePath = null;
        Uri uri = Uri.parse("content://media" + MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI.getPath() + "/" + albumId);

        Cursor cur = context.getContentResolver().query(uri, projection, null, null, null);
        if (cur == null) {
            return null;
        }

        if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
            cur.moveToNext();
            imagePath = cur.getString(0);
        }
        cur.close();


        return imagePath;
    }

    /**
     * 获取歌曲信息
     *
     * @param context
     * @param song
     * @return
     */
    public SongInfo getSongInfo(Context context, Song song) {
        check(context);
        SongInfo info = null;
        for (SongInfo s :
                songs) {
            info = s;
            if (info.getData().equals(song.path)) {// 对于同一首歌的路径是相同的
                break;
            }
        }
        return info;
    }

    /**
     * 根据歌曲存放路径 获取歌曲信息
     *
     * @param context
     * @param path
     * @return
     */
    public SongInfo getSongInfo(Context context, String path) {
        return getSongInfo(context, new Song(path));
    }

    /**
     * 获取歌曲列表
     *
     * @param context
     * @return
     */
    public List<Song> getSongList(Context context) {
        check(context);
        List<Song> songInfos = new ArrayList<>();
        for (SongInfo s :
                songs) {
            songInfos.add(new Song(s.getData()));
        }
        return songInfos;
    }

    /**
     * 获取歌曲信息列表
     *
     * @param context
     * @return
     */
    public List<SongInfo> getSongInfoList(Context context) {
        check(context);
        List<SongInfo> songInfos = new ArrayList<>();
        for (SongInfo song : songs) {
            songInfos.add(song);
        }
        return songInfos;
    }

    private void check(Context context) {
        if (songs == null)
            refreshData(context);
    }

    /**
     * 扫描sd卡
     *
     * @param context
     * @param listener
     */
    public void scanSdCard(Context context, MediaScannerConnection.OnScanCompletedListener listener) {
        MediaScannerConnection.scanFile(context, new String[]{
                Environment.getExternalStorageDirectory().getAbsolutePath()
        }, null, listener);
    }

    /**
     * 检查媒体库是否为空
     *
     * @param context
     * @param refresh
     * @return
     */
    public boolean emptyMediaLibrary(Context context, boolean refresh) {

        if (refresh) {
            refreshData(context);
        } else {
            check(context);
        }
        if (songs.size() == 0) {
            return true;
        } else {
            return false;
        }
    }
}
