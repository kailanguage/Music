package com.kailang.music.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MusicDao {
    @Insert
    void insertMusic(Music... music);

    @Update
    void updateMusic(Music... music);

    @Delete
    void deleteMusic(Music... music);

    @Query("SELECT * FROM Music ORDER BY musicId")
    LiveData<List<Music>> getAllMusicLive();

    @Query("SELECT * FROM Music WHERE musicId =:id ")
    Music getMusic(int id);

    //搜索数据库，范围包括歌曲名称和作者
    @Query("SELECT * FROM Music WHERE name LIKE :pattern or author LIKE :pattern ORDER BY musicId DESC")
    LiveData<List<Music>>findWordsWithPattern(String pattern);
}
