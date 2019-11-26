package com.kailang.music.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Music.class},version = 1,exportSchema = false)
public abstract class MusicDatabase extends RoomDatabase {
    private static MusicDatabase INSTANCE;
    static synchronized MusicDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),MusicDatabase.class,"music")
                    .allowMainThreadQueries()
                    .build();
        }
        return INSTANCE;
    }
    public abstract MusicDao getMusicDao();
}
