package com.zdd.musicplayer.aidl;

import android.os.IBinder;
import android.os.RemoteException;

/**
 * Project: MusicPlayer
 * Created by Zdd on 2018/2/1.
 * <p>
 * 服务端的播放列表改变时回调
 * 1 改变歌单时的回调
 * 2 从歌单中移除歌曲时回调
 */

public abstract class OnPlayListChangedListener extends IOnPlayListChangedListener.Stub {

    @Override
    public IBinder asBinder() {
        return super.asBinder();
    }

    /**
     * 服务端的播放列表改变时回调
     *
     * @param current 当前曲目
     * @param index   曲目下标
     * @param id      歌单id
     * @throws RemoteException
     */
    @Override
    public abstract void onPlayListChange(Song current, int index, int id) throws RemoteException;
}
