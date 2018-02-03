package com.zdd.musicplayer.service;

import android.content.Context;
import android.drm.DrmStore;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.RemoteException;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.zdd.musicplayer.R;
import com.zdd.musicplayer.aidl.IPlayControl;
import com.zdd.musicplayer.manager.MediaManager;
import com.zdd.musicplayer.modle.SongInfo;
import com.zdd.musicplayer.util.StringUtils;

import java.util.zip.Inflater;

/**
 * Project: MusicPlayer
 * Created by Zdd on 2018/2/2.
 * <p>
 * 耳机线控
 * <p>
 * Android 5.0中新增了MediaSession类专门解决媒体播放时界面和服务通讯问题，官方说明是  允许与媒体控制器、音量键、媒体按钮和传输控件交互。
 * 包含了媒体控制和线控等功能
 * 这个框架可以让我们不再使用广播来控制播放器，而且也能适配耳机，蓝牙等一些其它设备，实现线控的功能。
 * <p>
 * 但是对于低版本上的仍需要自己控制
 * <p>
 * 参考文章 http://www.jianshu.com/p/bc2f779a5400;
 */

public class MediaSessionManager {

    private static final String TAG = "MediaSessionManager";

    //指定可以接收的来自锁屏页面的按键信息
    private static final long MEDIA_SESSION_ACTIONS =
            PlaybackStateCompat.ACTION_PLAY
                    | PlaybackStateCompat.ACTION_PAUSE
                    | PlaybackStateCompat.ACTION_PLAY_PAUSE
                    | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                    | PlaybackStateCompat.ACTION_STOP
                    | PlaybackStateCompat.ACTION_SEEK_TO;


    private final IPlayControl control;
    private final Context context;
    private MediaSessionCompat mMediaSession;
    private final MediaManager mediaManager;

    public MediaSessionManager(Context context, IPlayControl control) {
        this.control = control;
        this.context = context;
        this.mediaManager = MediaManager.getInstance();
        setupMediaSession();
    }

    /**
     * 初始化并激活 MediaSession
     */
    private void setupMediaSession() {
//        第二个参数 tag: 这个是用于调试用的,随便填写即可
        mMediaSession = new MediaSessionCompat(context, TAG);
        //指明支持的按键信息类型
        mMediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        );
        mMediaSession.setCallback(callback);
        mMediaSession.setActive(true);
    }

    /**
     * 更新播放状态， 播放／暂停／拖动进度条时调用
     */
    public void updatePlaybackState() {
        int state = isPlaying() ? PlaybackStateCompat.STATE_PLAYING :
                PlaybackStateCompat.STATE_PAUSED;

        mMediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setActions(MEDIA_SESSION_ACTIONS)
                .setState(state, getCurrentPosition(), 1)
                .build());
    }

    private long getCurrentPosition() {
        try {
            return control.getProgress();
        } catch (RemoteException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 是否在播放
     *
     * @return
     */
    protected boolean isPlaying() {
        try {
            return control.status() == PlayController.STATUS_PLAYING;
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 更新正在播放的音乐信息，切换歌曲时调用
     */
    public void updateMetaData(String path) {
        if (!StringUtils.isReal(path)) {
            mMediaSession.setMetadata(null);
            return;
        }
        SongInfo songInfo = mediaManager.getSongInfo(context, path);

        MediaMetadataCompat.Builder metaDta = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, songInfo.getTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, songInfo.getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, songInfo.getAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, songInfo.getArtist())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, songInfo.getDuration())
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, getCoverBitmap(songInfo));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            metaDta.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, getCount());
        }
        mMediaSession.setMetadata(metaDta.build());

    }

    private long getCount() {
        try {
            return control.getPlayList().size();
        } catch (RemoteException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private Bitmap getCoverBitmap(SongInfo info) {
        if (StringUtils.isReal(info.getAlbum_path())) {
            return BitmapFactory.decodeFile(info.getAlbum_path());
        } else {
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.default_song);
        }
    }


    /**
     * 释放MediaSession，退出播放器时调用
     */
    public void release() {
        mMediaSession.setCallback(null);
        mMediaSession.setActive(false);
        mMediaSession.release();
    }


    /**
     * API 21 以上 耳机多媒体按钮监听 MediaSessionCompat.Callback
     */
    private MediaSessionCompat.Callback callback = new MediaSessionCompat.Callback() {

//        接收到监听事件，可以有选择的进行重写相关方法

        @Override
        public void onPlay() {
            try {
                control.resume();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onPause() {
            try {
                control.pause();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSkipToNext() {
            try {
                control.next();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSkipToPrevious() {
            try {
                control.pre();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onStop() {
            try {
                control.pause();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSeekTo(long pos) {
            try {
                control.seekTo((int) pos);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

}
