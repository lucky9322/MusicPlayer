package com.zdd.musicplayer.service;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.zdd.musicplayer.aidl.Song;
import com.zdd.musicplayer.db.DBMusicocoController;
import com.zdd.musicplayer.db.MainSheetHelper;
import com.zdd.musicplayer.db.modle.DBSongInfo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Project: MusicPlayer
 * Created by Zdd on 2018/2/2.
 * <p>
 * MediaPlayer 媒体播放引擎实现
 */

public class PlayController {

    private final Context context;
    private static volatile PlayController MANAGER = null;
    private final AudioFocusManager focusManager;
    private final MediaSessionManager sessionManager;

    private int mCurrentSong = 0;
    private int mPlayState;

    private List<Song> mPlayList = Collections.synchronizedList(new ArrayList<Song>());
    private final MediaPlayer mPlayer;

    private boolean isNext = true;
    private int mPlayListId;

    // MediaPlayer 是否调用过 setDataSource，
    // 否则第一次调用 changeSong 里的 _.reset 方法时 MediaPlayer 会抛 IllegalStateException
    private boolean hasMediaPlayerInit = false;

    public interface NotifyStatusChanged {
        void notify(Song song, int index, int status);
    }

    public interface NotifySongChanged {
        void notify(Song song, int index, boolean isNext);
    }

    public interface NotifyPlayListChanged {
        void notify(Song current, int index, int id);
    }

    private final NotifySongChanged mNotifySongChanged;
    private final NotifyPlayListChanged mNotifyPlayListChanged;
    private final NotifyStatusChanged mNotifyStatusChanged;


    //未知错误
    public static final int ERROR_UNKNOWN = -1;

    public static final int ERROR_INVALID = -2;

    //歌曲文件解码错误
    public static final int ERROR_DECODE = -3;

    //没有指定歌曲
    public static final int ERROR_NO_RESOURCE = -4;

    //正在播放
    public static final int STATUS_PLAYING = 10;

    //播放结束
    public static final int STATUS_COMPLETE = 11;

    //开始播放
    public static final int STATUS_START = 12;

    //播放暂停
    public static final int STATUS_PAUSE = 13;

    //停止
    public static final int STATUS_STOP = 14;

    //默认播放模式，列表播放，播放至列表末端时停止播放
    public static final int MODE_DEFAULT = 20;

    //列表循环
    public static final int MODE_LIST_LOOP = 21;

    //单曲循环
    public static final int MODE_SINGLE_LOOP = 22;

    //随机播放
    public static final int MODE_RANDOM = 23;

    private int mPlayMode = MODE_DEFAULT;

    private PlayController(Context context, AudioFocusManager focusManager, MediaSessionManager sessionManager, NotifyStatusChanged sl, NotifySongChanged sc, NotifyPlayListChanged pl) {
        this.context = context;
        this.focusManager = focusManager;
        this.sessionManager = sessionManager;
        this.mNotifyStatusChanged = sl;
        this.mNotifySongChanged = sc;
        this.mNotifyPlayListChanged = pl;

        mPlayState = STATUS_STOP;
        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                nextSong();
            }
        });

    }

    public static PlayController getMediaController(Context context, AudioFocusManager focusManager, MediaSessionManager sessionManager, NotifyStatusChanged sl, NotifySongChanged sc, NotifyPlayListChanged pl) {
        if (MANAGER == null) {
            synchronized (PlayController.class) {
                if (MANAGER == null)
                    MANAGER = new PlayController(context, focusManager, sessionManager, sl, sc, pl);
            }
        }
        return MANAGER;
    }

    //    设置播放模式
    public void setPlayMode(int mode) {
        this.mPlayMode = mode;
    }

    //    获得播放模式
    public int getPlayMode() {
        return this.mPlayMode;
    }

    //    获得播放列表
    public List<Song> getSongList() {
        return this.mPlayList;
    }

    //    设置播放列表
    public Song setPlayList(List<Song> songs, int current, int id) {
        this.mPlayList = songs;
        this.mPlayListId = id;

        mCurrentSong = current;
        changeSong();
        Song currentS = songs.get(mCurrentSong);
        mNotifyPlayListChanged.notify(currentS, current, id);
        return currentS;
    }

    public int getPlayListId() {
        return mPlayListId;
    }

    public Song setPlaySheet(int sheetID, int current) {
        DBMusicocoController dbController = new DBMusicocoController(context, false);
        List<DBSongInfo> ds;
        if (sheetID < 0) {
            MainSheetHelper helper = new MainSheetHelper(context, dbController);
            ds = helper.getMainSheetSongInfo(sheetID);
        } else {
            ds = dbController.getSongInfos(sheetID);
        }
        dbController.close();

        if (ds == null || ds.size() == 0) {
            return null;
        }

        List<Song> list = new ArrayList<>();
        for (DBSongInfo d : ds) {
            Song song = new Song(d.path);
            list.add(song);
        }

        mPlayList = list;
        mPlayListId = sheetID;

        mCurrentSong = current;
        changeSong();

        Song currentS = mPlayList.get(mCurrentSong);
        mNotifyPlayListChanged.notify(currentS, current, sheetID);

        return currentS;
    }

    /**
     * 获取当前正在播放歌曲
     *
     * @return
     */
    public Song getCurrentSong() {
        return mPlayList.size() == 0 ? null : mPlayList.get(mCurrentSong);
    }

    public int getCurrentSongIndex() {
        return mCurrentSong;
    }

    public int play(@NonNull Song song) {
        return play(mPlayList.indexOf(song));
    }

    public int play(int index) {
        int result = ERROR_INVALID;
        if (index != -1) {//列表中存在该歌曲
            if (mCurrentSong != index) {//不是当前歌曲
                isNext = mCurrentSong < index;
                if (mPlayState != STATUS_PLAYING) {
                    mNotifyStatusChanged.notify(getCurrentSong(), mCurrentSong, STATUS_START);
//                    切换并播放
                    mPlayState = STATUS_PLAYING;
                }
                result = changeSong();
            } else if (mPlayState != STATUS_PLAYING) {//是当前歌曲，但是没有播放
                mPlayState = STATUS_PAUSE;
                resume();//播放
            } else {//是且已经播放
                return 1;
            }
        } else {
            return ERROR_NO_RESOURCE;
        }
        return result;
    }

    public int prepare(@NonNull Song song) {
        int result = ERROR_INVALID;
        int index = mPlayList.indexOf(song);
        if (index != -1) {//列表中有该歌曲
            if (mCurrentSong != index) {//不是当前歌曲
                mCurrentSong = index;
                if (mPlayState == STATUS_PLAYING) {
                    pause();
                }
                result = changeSong();
            }
        } else {
            return ERROR_NO_RESOURCE;
        }

        return result;
    }

    //获得播放状态
    public int getPlayState() {
        return mPlayState;
    }

    /**
     * 上一曲
     *
     * @return
     */
    public Song preSong() {
        isNext = false;
        switch (mPlayMode) {
            case MODE_SINGLE_LOOP: {
                changeSong();
                break;
            }
            case MODE_RANDOM: {
                int pre = new Random().nextInt(mPlayList.size());
                if (pre != mCurrentSong) {
                    mCurrentSong = pre;
                    changeSong();
                }
            }
            case MODE_LIST_LOOP:
            default: {
                if (mCurrentSong == 0) {
                    mCurrentSong = mPlayList.size() - 1;
                } else {
                    mCurrentSong--;
                }
                changeSong();
            }
        }
        return mPlayList.size() == 0 ? null : mPlayList.get(mCurrentSong);
    }

    /**
     * 下一曲
     *
     * @return
     */
    public Song nextSong() {
        isNext = true;
        switch (mPlayMode) {
            case MODE_SINGLE_LOOP: {
                changeSong();
                break;
            }
            case MODE_LIST_LOOP: {
                if (mCurrentSong == mPlayList.size() - 1) {
                    mCurrentSong = 0;
                } else {
                    mCurrentSong++;
                }
                changeSong();
                break;
            }
            case MODE_RANDOM: {
                int next = new Random().nextInt(mPlayList.size());
                if (next != mCurrentSong) {
                    mCurrentSong = next;
                    changeSong();
                }
                break;
            }
            default: {
                if (mCurrentSong == mPlayList.size() - 1) {//最后一首
                    mCurrentSong = 0;
                    changeSong();
//                    使暂停播放
                    pause();
                } else {
                    mCurrentSong++;
                    changeSong();
                }
            }
        }
        return mPlayList.size() == 0 ? null : mPlayList.get(mCurrentSong);
    }

    public int pause() {
        if (mPlayState == STATUS_PLAYING) {
            sessionManager.updatePlaybackState();
            mPlayer.pause();
            mPlayState = STATUS_PAUSE;

            mNotifyStatusChanged.notify(getCurrentSong(), mCurrentSong, STATUS_STOP);
        }
        return mPlayState;
    }

    //    继续播放
    public int resume() {

        if (mPlayState != STATUS_PLAYING) {
            focusManager.requestAudioFocus();
            sessionManager.updatePlaybackState();
            mPlayer.start();
            mPlayState = STATUS_PLAYING;

            mNotifyStatusChanged.notify(getCurrentSong(), mCurrentSong, STATUS_START);
        }
        return 1;
    }

    //定位到
    public int seekTo(int to) {
        sessionManager.updatePlaybackState();
        mPlayer.seekTo(to);
        return 1;
    }

    //获得播放进度
    public int getProgress() {
        return mPlayer.getCurrentPosition();
    }

    /**
     * 切换歌曲
     * <p>
     * 切换成功返回1
     *
     * @return
     */
    private synchronized int changeSong() {
        if (mPlayState == STATUS_PLAYING || mPlayState == STATUS_PAUSE) {
            mPlayer.stop();
        }
        if (hasMediaPlayerInit) {
            mPlayer.reset();
        }
        if (mPlayList.size() == 0) {
            mCurrentSong = 0;
            mNotifySongChanged.notify(null, -1, isNext);
            return ERROR_NO_RESOURCE;//没有歌曲
        } else {
            String next = mPlayList.get(mCurrentSong).path;
            try {
                sessionManager.updateMetaData(next);
                mPlayer.setDataSource(next);
                if (!hasMediaPlayerInit) {
                    hasMediaPlayerInit = true;
                }
                mPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
                return ERROR_DECODE; //歌曲解码错误
            }
            if (mPlayState == STATUS_PLAYING) {
                focusManager.requestAudioFocus();
                sessionManager.updatePlaybackState();
                mPlayer.start();
            }
            mNotifySongChanged.notify(getCurrentSong(), mCurrentSong, isNext);
            return 1;
        }
    }

    //用于提取频谱
    public int getAudioSessionId() {
        return mPlayer.getAudioSessionId();
    }

    public void remove(Song song) {
        if (null == song) {
            return;
        }
        int index = mPlayList.indexOf(song);
        if (index != -1) {
            if (mCurrentSong == index) {
                int tempS = mPlayMode;
                mPlayMode = MODE_LIST_LOOP;
                mPlayList.remove(index);
                mCurrentSong--;
                nextSong();
                mPlayMode = tempS;
            } else {
                mPlayList.remove(index);
                if (index < mCurrentSong) {
                    mCurrentSong--;
                }
            }

            if (mPlayList.size() == 0 || mCurrentSong < 0) {
                // 服务器的播放列表是空的，这可能是因为仅有一首歌曲的播放列表被清空
                // 此时重新设置为【全部歌曲】，该过程在服务端完成，若在客户端的 onPlayListChange
                // 回调中重置播放列表会得到异常：beginBroadcast() called while already in a broadcast
                setPlaySheet(MainSheetHelper.SHEET_ALL, 0);
            } else {
                mNotifyPlayListChanged.notify(mPlayList.get(mCurrentSong), mCurrentSong, mPlayListId);
            }
        }
    }
}
