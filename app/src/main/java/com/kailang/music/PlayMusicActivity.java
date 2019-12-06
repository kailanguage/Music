package com.kailang.music;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.kailang.music.data.Music;
import com.kailang.music.services.MediaPlayerHelp;
import com.kailang.music.services.MusicService;
import com.kailang.music.utils.FileManager;
import com.kailang.music.utils.MediaDownloader;

import java.io.File;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.glide.transformations.BlurTransformation;

public class PlayMusicActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String MUSIC_ID = "musicId";
    public static final String MUSIC_AMOUNT = "musicAmount";

    private ImageView mIvBg;
    private TextView mTvName, mTvAuthor;
    private int mMusicId;
    private int musicAmount;
    private Music mMusic;
    private MusicViewModel musicViewModel;

    private SeekBar seekBar;
    private boolean isPlaying = false, isBindService;
    private CircleImageView mIvIcon;
    private FrameLayout mFlPlayMusic;
    private ImageView iv_next, iv_previous, iv_pausePlay;
    private Animation mPlayMusicAnim;
    private UpdateSeekBar updateProgress;

    private MusicService.MusicBind mMusicBinder;
    private Intent mServiceIntent;
    private MediaPlayerHelp mediaPlayerHelp;
    private MediaPlayer helpMediaPlayer;


    public final String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath();
    List<Music> localMusicList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_music);

        musicViewModel = ViewModelProviders.of(this).get(MusicViewModel.class);
        initData();
        initView();

        //检查权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            selectSource();
        }


    }

    //初始化
    private void initData() {
        mMusicId = getIntent().getIntExtra(MUSIC_ID, 1);
        musicAmount = getIntent().getIntExtra(MUSIC_AMOUNT, 1);
        mMusic = musicViewModel.getMusicById(mMusicId);
    }

    //初始控件
    private void initView() {
        mFlPlayMusic = findViewById(R.id.fl_play_music);
        mIvIcon = findViewById(R.id.iv_icon);
        mIvBg = findViewById(R.id.iv_bg);
        mTvName = findViewById(R.id.tv_name);
        mTvAuthor = findViewById(R.id.tv_author);

        iv_next = findViewById(R.id.imageView_next);
        iv_pausePlay = findViewById(R.id.imageView_pausePlay);
        iv_previous = findViewById(R.id.imageView_previous);
        seekBar = findViewById(R.id.seekBar);

        iv_pausePlay.setOnClickListener(this);
        iv_previous.setOnClickListener(this);
        iv_next.setOnClickListener(this);
        //设置音乐图标
        setMusicIcon();
        mPlayMusicAnim = AnimationUtils.loadAnimation(this, R.anim.play_music_anim);
    }

    @Override
    public void onRequestPermissionsResult(int requstCode, String[] permissions, int[] grantResults) {
        switch (requstCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "正在加载", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "拒绝权限将无法使用程序", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            default:
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.imageView_pausePlay:
                if (!isPlaying) {
                    selectSource();
                } else
                    stopMusic();
                break;
            case R.id.imageView_previous:
                previous();
                break;
            case R.id.imageView_next:
                next();
                break;
        }
    }

    //上一首
    private void previous() {
        if (mMusic.getMusicId() == 0)
            mMusic = musicViewModel.getMusicById(musicAmount - 1);
        else
            mMusic = musicViewModel.getMusicById((mMusic.getMusicId() - 1) % musicAmount);
        Log.e("previous", Math.abs((mMusic.getMusicId() - 1) - musicAmount) % musicAmount + "");
        setMusicIcon();
        selectSource();
    }

    //下一首
    private void next() {
        mMusic = musicViewModel.getMusicById((mMusic.getMusicId() + 1) % musicAmount);
        Log.e("next", (mMusic.getMusicId() + 1) % musicAmount + "");
        setMusicIcon();
        selectSource();
    }

    //更新进度栏
    private class UpdateSeekBar extends AsyncTask<Integer, Integer, String> {
        @Override
        protected void onPostExecute(String s) {
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            seekBar.setProgress(values[0]);
        }

        @Override
        protected String doInBackground(Integer... params) {
            while (isPlaying) {
                try {
                    Thread.sleep(params[0]);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                this.publishProgress(helpMediaPlayer.getCurrentPosition());
            }
            return null;
        }
    }

    private class MySeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mMusicBinder.getMediaPlayerHelp().getmMediaPlayer().seekTo(seekBar.getProgress());
        }
    }

    /**
     * 设置光盘中显示的音乐封面图片，歌曲名及作者
     */
    private void setMusicIcon() {
        Log.e("xxxx", "setMusicIcon");
        Glide.with(this)
                .load(mMusic.getPoster())
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(25, 10)))
                .into(mIvBg);

        mTvName.setText(mMusic.getName());
        mTvAuthor.setText(mMusic.getAuthor());
        Glide.with(this)
                .load(mMusic.getPoster())
                .into(mIvIcon);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroy();
    }

    /**
     * 启动音乐服务
     */
    private void startMusicService() {
//        Log.e("xxxx", "startMusicService");
        if (mServiceIntent == null) {
            mServiceIntent = new Intent(this, MusicService.class);
            startService(mServiceIntent);
            iv_pausePlay.setImageDrawable(this.getDrawable(R.drawable.ic_play_circle_outline_black_64dp));
        } else {
//            Log.e("xxxx", "mServiceIntent_else");
            helpMediaPlayer = mMusicBinder.getMediaPlayerHelp().getmMediaPlayer();
            isPlaying = true;
            mFlPlayMusic.startAnimation(mPlayMusicAnim);
            iv_pausePlay.setImageDrawable(getApplication().getDrawable(R.drawable.ic_pause_circle_outline_black_64dp));
            mMusicBinder.setMusic(mMusic);
            mMusicBinder.playMusic();

            //设置进度条
            seekBar.setMax(mMusicBinder.getMediaPlayerHelp().getmMediaPlayer().getDuration());
            updateProgress = new UpdateSeekBar();
            updateProgress.execute(1000);
            seekBar.setOnSeekBarChangeListener(new MySeekBarChangeListener());
        }

//        当前未绑定，绑定服务，同时修改绑定状态
        if (!isBindService) {
            isBindService = true;
            bindService(mServiceIntent, conn, Context.BIND_AUTO_CREATE);
        }
    }

    /**
     * 停止播放
     */
    public void stopMusic() {
        Log.e("xxxx", "stopMusic");
        isPlaying = false;
        iv_pausePlay.setImageDrawable(this.getDrawable(R.drawable.ic_play_circle_outline_black_64dp));
        mFlPlayMusic.clearAnimation();
//        UpdateSeekBar updateProgress = new UpdateSeekBar();
//        updateProgress.execute(1000);
//        seekBar.setOnSeekBarChangeListener(new MySeekBarChangeListener());
        mMusicBinder.stopMusic();

        //设置进度条
//        seekBar.setMax(mMusicBinder.getMediaPlayerHelp().getmMediaPlayer().getDuration());
        updateProgress = new UpdateSeekBar();
        updateProgress.execute(1000);
        seekBar.setOnSeekBarChangeListener(new MySeekBarChangeListener());
    }

    /**
     * 销毁方法，需要在 activity 被销毁的时候调用
     */
    public void destroy() {
//        如果已绑定服务，则解除绑定，同时修改绑定状态
        if (isBindService) {
            isBindService = false;
            unbindService(conn);
        }

    }

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMusicBinder = (MusicService.MusicBind) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    //下载
    class DownloadTack extends AsyncTask<String, Integer, Object> implements MediaDownloader.OnDownloadListener {
        int lenght; //记录总大小
        String savePath;

        @Override
        protected Object doInBackground(String... params) {
            new MediaDownloader(this).download(params[0], params[1]);
            return null;
        }

        @Override
        public void onStart(int size, String path) {
            lenght = size;
            this.savePath = path;
        }

        @Override
        public void onDownloading(int currentSize) {
            //更新进度条

            if (currentSize >= 1024 && !isPlaying) {
                //开始解析
                setDataAndPlay(savePath);
            }
            int currentRate = 100 * currentSize / lenght;
            publishProgress(currentRate);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            seekBar.setSecondaryProgress(values[0]);
        }
    }


    //开始解析文件
    private void setDataAndPlay(String path) {
        //设置播放的数据源
        try {
            mMusic.setPath(path);
            startMusicService();
        } catch (Exception e) {
            isPlaying = false;
        }
    }


    //选择本地合还是网络
    private void selectSource() {
        localMusicList = FileManager.getInstance(this).getMusics();
        boolean isLocalHave=false;
        for (Music m : localMusicList) {
            Log.e("Path",mMusic.getName()+" "+m.getPath().substring(m.getPath().lastIndexOf("/"))+" "+localMusicList.size());
            if (m.getPath().substring(m.getPath().lastIndexOf("/")).contains(mMusic.getName())) {
                mMusic.setPath(m.getPath());
                Toast.makeText(this, "本地已存在，即将播放", Toast.LENGTH_SHORT).show();
                startMusicService();
                isLocalHave = true;
                break;
            }
        }
        Log.e("isLocalHave",isLocalHave+"");
        if (!isLocalHave) {
            Toast.makeText(this, "本地不存在，正在下载", Toast.LENGTH_SHORT).show();

            String filename = mMusic.getName()+".mp3";
            Log.e("filename",directory+"/"+filename);
            new DownloadTack().execute(mMusic.getPath(), directory +"/"+ filename);
        }
    }
}
