package com.kailang.music;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.kailang.music.data.Music;
import com.kailang.music.data.MusicRepository;

import java.util.List;

public class MusicViewModel extends AndroidViewModel {
    private MusicRepository musicRepository;
    public MusicViewModel(@NonNull Application application) {
        super(application);
        musicRepository=new MusicRepository(application);
    }
    public LiveData<List<Music>> getAllMusicsLive(){
        return musicRepository.getAllMusicsLive();
    }
    public LiveData<List<Music>> findMusicWithPattern(String pattern){
        return musicRepository.findMusicWithPattern(pattern);
    }
    public Music getMusicById(int musicId){
        return musicRepository.getMusicById(musicId);
    }
    public void insertMusic(Music... music){
        musicRepository.insertMusic(music);
    }

    public void updateMusic(Music... music){
        musicRepository.updateMusic(music);
    }
    public void deleteMusic(Music... music){
        musicRepository.deleteMusic(music);
    }
}
