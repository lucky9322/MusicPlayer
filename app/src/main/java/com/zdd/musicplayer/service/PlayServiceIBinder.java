package com.zdd.musicplayer.service;

import android.content.Context;

import com.zdd.musicplayer.aidl.PlayControlImpl;
import com.zdd.musicplayer.aidl.Song;

import java.util.List;

/**
 * Project: MusicPlayer
 * Created by Zdd on 2018/2/3.
 */

public class PlayServiceIBinder extends PlayControlImpl {

    private Context context;

    public PlayServiceIBinder(Context context, List<Song> songs) {
        super(context);
        this.context = context;

    }
}
