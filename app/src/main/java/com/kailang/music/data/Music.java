package com.kailang.music.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Music {

    /**
     * {
     *           "musicId": "101",
     *           "name": "Nostalgic Piano",
     *           "poster": "http://res.lgdsunday.club/poster-1.png",
     *           "path": "http://res.lgdsunday.club/Nostalgic%20Piano.mp3",
     *           "author": "Rafael Krux"
     *         }
     */
    @NonNull
    @PrimaryKey
    private int musicId;
    private String name;
    private String poster;
    private String path;
    private String author;

    public Music(String name, String path, String author) {
        this.name = name;
        this.path = path;
        this.author = author;
    }



    public int getMusicId() {
        return musicId;
    }

    public void setMusicId(int musicId) {
        this.musicId = musicId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
