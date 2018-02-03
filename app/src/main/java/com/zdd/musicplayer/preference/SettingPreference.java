package com.zdd.musicplayer.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Project: MusicPlayer
 * Created by Zdd on 2018/1/30.
 * <p>
 * 应用偏好设置，打开应用自动播放，记忆播放，自动切换夜间模式，耳机线控
 */

public class SettingPreference {

    private final SharedPreferences preferences;
    private Context context;

    public SettingPreference(Context context) {
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * 自动播放
     *
     * @return
     */
    public boolean openAutoPlay() {
        return preferences.getBoolean("pre_auto_play", false);
    }

    /**
     * 记忆播放
     *
     * @return
     */
    public boolean memoryPlay() {
        return preferences.getBoolean("pre_memory_play", true);
    }

    /**
     * 自动切换夜间模式
     * @return
     */
    public boolean autoSwitchNightTheme() {
        return preferences.getBoolean("pre_auto_switch_night_theme", true);
    }

    /**
     * 耳机线控
     * @return
     */
    public boolean preHeadphoneWire() {
        return preferences.getBoolean("pre_headphone_wire", true);
    }
}
