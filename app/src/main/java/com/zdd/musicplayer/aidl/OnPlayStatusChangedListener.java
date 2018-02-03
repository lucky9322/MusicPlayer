package com.zdd.musicplayer.aidl;

import android.os.IBinder;
import android.os.RemoteException;

/**
 * Project: MusicPlayer
 * Created by Zdd on 2018/2/1.
 * <p>
 * 用户手动暂停，播放歌曲时回调
 */

public abstract class OnPlayStatusChangedListener extends IOnPlayStatusChangedListener.Stub {

    @Override
    public IBinder asBinder() {
        return super.asBinder();
    }

    /**
     * 1 自动播放时播放曲目播放完成时回调，一般情况下该方法调用后{@link #playStart(Song, int, int)}将会被调用
     * 2 暂停，停止播放时回调
     *
     * @param which  当前播放曲目
     * @param index  播放列表下标
     * @param status 播放状态{@link }
     * @throws RemoteException
     */
    @Override
    public abstract void playStop(Song which, int index, int status) throws RemoteException;

    /**
     * 1 自动播放时开始播放曲目时回调
     * 2 继续播放，开始播放时回调
     *
     * @param which  当前开始播放曲目
     * @param index  播放列表下标
     * @param status 播放状态
     * @throws RemoteException
     */
    @Override
    public abstract void playStart(Song which, int index, int status) throws RemoteException;
}
