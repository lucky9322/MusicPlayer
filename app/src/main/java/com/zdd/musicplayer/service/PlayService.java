package com.zdd.musicplayer.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Project: MusicPlayer
 * Created by Zdd on 2018/2/3.
 * <p>
 * 该服务将在独立的进程中运行
 * 只负责对播放列表中的歌曲进行播放
 */

public class PlayService extends Service {

    private PlayServiceIBinder mIBinder;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
