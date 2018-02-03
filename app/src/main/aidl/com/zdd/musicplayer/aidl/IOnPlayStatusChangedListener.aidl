// IOnPlayStatusChangedListener.aidl
package com.zdd.musicplayer.aidl;
import com.zdd.musicplayer.aidl.Song;

// Declare any non-default types here with import statements


// 播放状态改变监听
interface IOnPlayStatusChangedListener {

//    自动播放时歌曲播放完成时回调
    void playStop(in Song which,int index,int status);
//    自动播放歌曲开始时回调
    void playStart(in Song which,int index,int status);

}
