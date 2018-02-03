package com.zdd.musicplayer.aidl;

import android.os.IBinder;
import android.os.RemoteException;

/**
 * Project: MusicPlayer
 * Created by Zdd on 2018/2/1.
 * <p>
 * 用户主动切换歌曲时回调，包含如下几种情况
 * <p>
 * 1 播放相同播放列表中指定曲目
 * 2 切换到 前一首
 * 3 切换到 下一曲
 * 4 切换播放且表
 */

public abstract class OnSongChangedListener extends IOnSongChangedListener.Stub {
    @Override
    public IBinder asBinder() {
        return super.asBinder();
    }

    /**
     * 该方法在服务端线程的 Binder 线程池中运行，客户端调用时 不能操作UI控件
     *
     * @param which
     * @param index
     * @param isNext
     * @throws RemoteException
     */
    @Override
    public abstract void onSongChange(Song which, int index, boolean isNext) throws RemoteException;
}
