// IOnSongChangedListener.aidl
package com.zdd.musicplayer.aidl;
import com.zdd.musicplayer.aidl.Song;

// Declare any non-default types here with import statements
// 播放中
interface IOnSongChangedListener {

    //该方法运行在线程池中（非 UI 线程）
    void onSongChange(in Song which,int index,boolean isNext);

}
