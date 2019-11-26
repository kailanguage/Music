package com.kailang.music.data;

import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

public class MusicRepository {
    private LiveData<List<Music>> allMusicLive;
    private MusicDao musicDao;

    public MusicRepository(Context context){
        MusicDatabase musicDatabase = MusicDatabase.getDatabase(context.getApplicationContext());
        musicDao = musicDatabase.getMusicDao();
        allMusicLive = musicDao.getAllMusicLive();
    }

    public void insertMusic(Music... music){
        new InsertAsyncTask(musicDao).execute(music);
    }
    public void updateMusic(Music... music){
        new UpdateAsyncTask(musicDao).execute(music);
    }
    public void deleteMusic(Music... music){
        new DeleteAsyncTask(musicDao).execute(music);
    }
    public Music getMusicById(int musicId){
        return musicDao.getMusic(musicId);
    }
    public LiveData<List<Music>> getAllMusicsLive(){
        return allMusicLive;
    }
    public LiveData<List<Music>>findMusicWithPattern(String pattern){
        return musicDao.findWordsWithPattern("%" + pattern + "%");
    }

    static class InsertAsyncTask extends AsyncTask<Music,Void,Void> {
        private MusicDao musicDao;
        public InsertAsyncTask(MusicDao musicDao) {
            this.musicDao=musicDao;
        }

        @Override
        protected Void doInBackground(Music... music) {
            musicDao.insertMusic(music);
            return null;
        }
    }

    static class UpdateAsyncTask extends AsyncTask<Music,Void,Void>{
        private MusicDao musicDao;
        public UpdateAsyncTask(MusicDao musicDao) {
            this.musicDao=musicDao;
        }

        @Override
        protected Void doInBackground(Music... music) {
            musicDao.updateMusic(music);
            return null;
        }
    }

    static class DeleteAsyncTask extends AsyncTask<Music,Void,Void>{
        private MusicDao musicDao;
        public DeleteAsyncTask(MusicDao musicDao) {
            this.musicDao=musicDao;
        }

        @Override
        protected Void doInBackground(Music... music) {
            musicDao.deleteMusic(music);
            return null;
        }
    }
}
