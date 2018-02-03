package com.zdd.musicplayer.app;

import android.app.Application;

import com.zdd.musicplayer.preference.SettingPreference;

/**
 * Project: MusicPlayer
 * Created by Zdd on 2018/1/30.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        checkAutoThemeSwitch();
    }

    /**
     * 检测是否需要切换主题
     */
    private void checkAutoThemeSwitch(){
        SettingPreference settingPreference=new SettingPreference(this);

    }
}
