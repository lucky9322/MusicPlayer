package com.zdd.musicplayer.util;

/**
 * Project: MusicPlayer
 * Created by Zdd on 2018/2/2.
 */

public class StringUtils {

    public static boolean isReal(String string) {
        return string != null && string.length() > 0 && !"null".equals(string);
    }
}
