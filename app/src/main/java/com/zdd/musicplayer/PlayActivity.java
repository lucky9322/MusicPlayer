package com.zdd.musicplayer;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.zdd.musicplayer.aidl.IPlayControl;
import com.zdd.musicplayer.aidl.Song;
import com.zdd.musicplayer.manager.MediaManager;
import com.zdd.musicplayer.modle.SongInfo;
import com.zdd.musicplayer.service.PlayService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class PlayActivity extends RootActivity {

    private static final String TAG = "PlayActivity";
    private final int BIND_SERVICE = 1;

    private IPlayControl mControl;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == BIND_SERVICE) {

                Intent intent = new Intent(PlayActivity.this, PlayService.class);
                intent.putParcelableArrayListExtra("songs", songs);
                bindService(intent, connection, Service.BIND_AUTO_CREATE);

            }
            return true;
        }
    });

    private IBinder.DeathRecipient mRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Toast.makeText(PlayActivity.this, "服务 death", Toast.LENGTH_SHORT).show();
        }
    };

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mControl = IPlayControl.Stub.asInterface(service);
            try {
                for (Song s :
                        mControl.getPlayList())
                    Log.i("Main", "onServiceConnected: " + s.path);
                service.linkToDeath(mRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private ArrayList<Song> songs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        songs = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                HashSet<SongInfo> infos = MediaManager.getInstance().refreshData(PlayActivity.this);
                for (SongInfo s :
                        infos) {
                    Song song = new Song(s.getData());
                    songs.add(song);
                    Log.d(TAG, "run : " + s.getData());
                }

            }
        }).start();
    }
}
