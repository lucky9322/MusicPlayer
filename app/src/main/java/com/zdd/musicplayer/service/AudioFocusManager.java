package com.zdd.musicplayer.service;

import android.content.Context;
import android.media.AudioManager;
import android.os.RemoteException;

import com.zdd.musicplayer.aidl.IPlayControl;


/**
 * Project: MusicPlayer
 * Created by Zdd on 2018/2/2.
 * <p>
 * 音频焦点管理
 * <p>
 * 服务端调用
 * <p>
 * 参考文章 http://www.jianshu.com/p/bc2f779a5400;
 */

public class AudioFocusManager implements AudioManager.OnAudioFocusChangeListener {

    private final Context context;
    private final IPlayControl control;
    private final AudioManager audioManager;

    //    短暂 音频焦点丢失 打电话
    private boolean isPauseByFocusLossTransient;
    //    瞬间 音频焦点丢失 很快就回来 来通知或者短信
    private int mVolumeWhenFocusLossTransientCanDuck;

    public AudioFocusManager(Context context, IPlayControl control) {
        this.context = context;
        this.control = control;
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * 播放音乐前先请求音频焦点
     *
     * @return
     */
    public boolean requestAudioFocus() {
        return audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_REQUEST_GRANTED) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    /**
     * 退出播放器后 不再占用音频焦点
     */
    public void abandonAudioFocus() {
        audioManager.abandonAudioFocus(this);
    }

    /**
     * 音频焦点回调监听
     *
     * @param i
     */
    @Override
    public void onAudioFocusChange(int i) {

        int volume;

        switch (i) {
//            重新获得焦点
            case AudioManager.AUDIOFOCUS_GAIN:
                if (!willPlay() && isPauseByFocusLossTransient) {
//                    通话结束，恢复播放
                    play();
                }
                volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                if (mVolumeWhenFocusLossTransientCanDuck > 0 && volume == mVolumeWhenFocusLossTransientCanDuck / 2) {
//                    恢复音量
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mVolumeWhenFocusLossTransientCanDuck,
                            AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                }
                isPauseByFocusLossTransient = false;
                mVolumeWhenFocusLossTransientCanDuck = 0;
                break;
//                永久丢失焦点，如被其他播放器抢占
            case AudioManager.AUDIOFOCUS_LOSS:
                if (willPlay()) {
                    forceStop();
                }
                break;
//                短暂丢失焦点，如来电话
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (willPlay()) {
                    forceStop();
                    isPauseByFocusLossTransient = true;
                }
                break;
//                瞬间丢失焦点，如通知
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
//                音量减小为一半
                volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                if (willPlay() && volume > 0) {
                    mVolumeWhenFocusLossTransientCanDuck = volume;
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mVolumeWhenFocusLossTransientCanDuck,
                            AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                }
                break;
        }

    }

    private void play() {
        try {
            control.resume();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void forceStop() {
        try {
            control.pause();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private boolean willPlay() {
        try {
            return control.status() == PlayController.STATUS_PLAYING;
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }
}
