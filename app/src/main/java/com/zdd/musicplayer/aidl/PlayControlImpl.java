package com.zdd.musicplayer.aidl;

import android.content.Context;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.zdd.musicplayer.service.AudioFocusManager;
import com.zdd.musicplayer.service.MediaSessionManager;
import com.zdd.musicplayer.service.PlayController;

import java.util.List;

/**
 * Project: MusicPlayer
 * Created by Zdd on 2018/2/1.
 * <p>
 * 1 该类中的方法运行在服务端 Binder 线程中，所以需要处理线程同步
 * 2 这些方法被客户端调用时客户端线程会被挂起 如果客户端的线程为UI 线程 ，注意处理耗时操作避免出现anr
 */

public class PlayControlImpl extends IPlayControl.Stub {

    protected final RemoteCallbackList<IOnSongChangedListener> mSongChangeListeners;
    protected final RemoteCallbackList<IOnPlayStatusChangedListener> mStatusChangeListeners;
    protected final RemoteCallbackList<IOnPlayListChangedListener> mPlayListChangeListeners;
    protected final RemoteCallbackList<IOnDataIsReadyListener> mDataIsReadyListeners;

    private final Context context;

    private final PlayController manager;
    private final AudioFocusManager focusManager;
    private final MediaSessionManager sessionManager;

    public PlayControlImpl(Context context) {
        this.context = context;
        this.mSongChangeListeners = new RemoteCallbackList<>();
        this.mStatusChangeListeners = new RemoteCallbackList<>();
        this.mPlayListChangeListeners = new RemoteCallbackList<>();
        this.mDataIsReadyListeners = new RemoteCallbackList<>();

        this.sessionManager = new MediaSessionManager(context, this);
        this.focusManager = new AudioFocusManager(context, this);
        this.manager = PlayController.getMediaController(context, focusManager, sessionManager,
                new NotifyStatusChange(),
                new NotifySongChange(),
                new NotifyPlayListChange());

    }

    //==========IPlayControl start =========================

    /**
     * 播放相同列表中，指定歌曲
     *
     * @param which 曲目
     * @return 播放是否成功
     * @throws RemoteException
     */
    @Override
    public synchronized int play(Song which) throws RemoteException {
        if (null == which) {
            return -1;
        }
        int re = PlayController.ERROR_UNKNOWN;
        if (manager.getCurrentSong() != which) {
            re = manager.play(which);
        }
        return re;
    }

    @Override
    public int playByIndex(int index) throws RemoteException {
        int re = PlayController.ERROR_UNKNOWN;
        if (index < manager.getSongList().size()
                && manager.getSongList().get(index) != null
                && manager.getCurrentSongIndex() != index) {
            re = manager.play(index);
        }
        return re;
    }

    @Override
    public int getAudioSessionId() throws RemoteException {
        return manager.getAudioSessionId();
    }

    @Override
    public int setCurrentSong(Song song) throws RemoteException {
        if (null == song) {
            return -1;
        }
        return manager.prepare(song);
    }


    /**
     * 该方法并没有 在aidl文件中声明，客户端不应该调用该方法
     *
     * @param index
     * @return
     */
    public int play(int index) {
        return manager.play(index);
    }

    @Override
    public Song pre() throws RemoteException {
        Song pre = manager.getCurrentSong();
        Song s = manager.preSong();
        return s;
    }

    @Override
    public Song next() throws RemoteException {
        Song pre = manager.getCurrentSong();
        Song next = manager.nextSong();
        return next;
    }

    @Override
    public int pause() throws RemoteException {
        return manager.pause();
    }

    @Override
    public int resume() throws RemoteException {
        return manager.resume();
    }

    @Override
    public Song currentSong() throws RemoteException {
        return manager.getCurrentSong();
    }

    @Override
    public int currentSongIndex() throws RemoteException {
        return manager.getCurrentSongIndex();
    }

    @Override
    public int status() throws RemoteException {
        return manager.getPlayState();
    }

    @Override
    public Song setPlayList(List<Song> songs, int current, int id) throws RemoteException {
        if (songs.size() <= 0) {
            return null;
        }
        int cu = 0;
        if (current >= 0 && current < songs.size()) {
            cu = current;
        }
        Song s = manager.setPlayList(songs, cu, id);
        return s;
    }

    @Override
    public Song setPlaySheet(int sheetID, int current) throws RemoteException {
        return manager.setPlaySheet(sheetID, current);
    }

    @Override
    public List<Song> getPlayList() throws RemoteException {
        return manager.getSongList();
    }

    @Override
    public int getPlayListId() throws RemoteException {
        return manager.getPlayListId();
    }

    @Override
    public void registerOnSongChangedListener(IOnSongChangedListener li) throws RemoteException {
        mSongChangeListeners.register(li);
    }

    @Override
    public void registerOnPlayStatusChangedListener(IOnPlayStatusChangedListener li) throws RemoteException {
        mStatusChangeListeners.register(li);
    }

    @Override
    public void registerOnPlayListChangedListener(IOnPlayListChangedListener li) throws RemoteException {
        mPlayListChangeListeners.register(li);
    }

    @Override
    public void registerOnDataIsReadyListener(IOnDataIsReadyListener li) throws RemoteException {
        mDataIsReadyListeners.register(li);
    }

    @Override
    public void unregisterOnSongChangedListener(IOnSongChangedListener li) throws RemoteException {
        mSongChangeListeners.unregister(li);
    }

    @Override
    public void unregisterOnPlayStatusChangedListener(IOnPlayStatusChangedListener li) throws RemoteException {
        mStatusChangeListeners.unregister(li);
    }

    @Override
    public void unregisterOnPlayListChangedListener(IOnPlayListChangedListener li) throws RemoteException {
        mPlayListChangeListeners.unregister(li);
    }

    @Override
    public void unregisterOnDataIsReadyListener(IOnDataIsReadyListener li) throws RemoteException {
        mDataIsReadyListeners.unregister(li);
    }

    @Override
    public void setPlayMode(int mode) throws RemoteException {
        if (mode >= PlayController.MODE_DEFAULT && mode <= PlayController.MODE_RANDOM)
            manager.setPlayMode(mode);
    }

    @Override
    public int getProgress() throws RemoteException {
        return manager.getProgress();
    }

    @Override
    public int seekTo(int pos) throws RemoteException {
        return manager.seekTo(pos);
    }

    @Override
    public void remove(Song song) throws RemoteException {
        manager.remove(song);
    }

    @Override
    public int getPlayMode() throws RemoteException {
        return manager.getPlayMode();
    }

    //==========IPlayControl end =========================


    //==========PlayController 内部接口 start===============
    private class NotifySongChange implements PlayController.NotifySongChanged {

        @Override
        public void notify(Song song, int index, boolean isNext) {

        }
    }

    private class NotifyStatusChange implements PlayController.NotifyStatusChanged {

        @Override
        public void notify(Song song, int index, int status) {

        }
    }

    private class NotifyPlayListChange implements PlayController.NotifyPlayListChanged {

        @Override
        public void notify(Song current, int index, int id) {

        }
    }

    //==========PlayController 内部接口 end===============
}
