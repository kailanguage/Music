package com.kailang.music.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;


import com.kailang.music.data.Music;


/**
 * 1、通过Service 连接 PlayMusicView 和 MediaPlayHelper
 * 2、PlayMusicView -- Service：
 *      1、播放音乐、暂停音乐
 *      2、启动Service、绑定Service、解除绑定Service
 * 3、MediaPlayHelper -- Service：
 *      1、播放音乐、暂停音乐
 *      2、监听音乐播放完成，停止 Service
 */
public class MusicService extends Service {

    private MediaPlayerHelp mMediaPlayerHelp;
    private Music mMusicModel;

    public MusicService() {

    }

    public class MusicBind extends Binder {
        /**
         * 设置音乐（Music）
         */
        public void setMusic (Music musicModel) {
            mMusicModel = musicModel;
        }

        public MediaPlayerHelp getMediaPlayerHelp() {
            return mMediaPlayerHelp;
        }

        /**
         * 播放音乐
         */
        public void playMusic () {
            /**
             * 1、判断当前音乐是否是已经在播放的音乐
             * 2、如果当前的音乐是已经在播放的音乐的话，那么就直接执行start方法
             * 3、如果当前播放的音乐不是需要播放的音乐的话，那么就调用setPath的方法
             */
            if (mMediaPlayerHelp.getPath() != null
                    && mMediaPlayerHelp.getPath().equals(mMusicModel.getPath())) {
                mMediaPlayerHelp.start();
            } else {
                mMediaPlayerHelp.setPath(mMusicModel.getPath());
                mMediaPlayerHelp.setOnMediaPlayerHelperListener(new MediaPlayerHelp.OnMediaPlayerHelperListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mMediaPlayerHelp.start();
                    }

                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        stopSelf();
                    }
                });
            }
        }

        /**
         * 暂停播放
         */
        public void stopMusic () {
            mMediaPlayerHelp.pause();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {

        return new MusicBind();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayerHelp = MediaPlayerHelp.getInstance(this);
    }

}
