package com.zdd.musicplayer.aidl;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Project: MusicPlayer
 * Created by Zdd on 2018/2/1.
 */

public class Song implements Parcelable {

    //与客户端 DBSongInfo 中的 data 域对应，对于同一首歌曲（文件路径相同），两者应该相同
    public String path;

    public Song(String path) {
        this.path = path;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Song song = (Song) obj;
        return path.equals(song.path);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    protected Song(Parcel in) {
        this.path = in.readString();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.path);
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel source) {
            return new Song(source);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };
}
