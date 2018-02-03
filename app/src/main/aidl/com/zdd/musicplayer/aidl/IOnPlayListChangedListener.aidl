// IOnPlayListChangedListener.aidl
package com.zdd.musicplayer.aidl;
import com.zdd.musicplayer.aidl.Song;
// Declare any non-default types here with import statements

// 播放列表改变 监听
interface IOnPlayListChangedListener {

  void onPlayListChange(in Song current,int index,int id);
}
