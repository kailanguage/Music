package com.kailang.music;

import android.Manifest;
import android.content.ComponentName;
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

import java.io.File;

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

    private MediaPlayer mediaPlayer = new MediaPlayer();
    private SeekBar seekBar;
    private boolean isPlaying = false;
    private CircleImageView mIvIcon;
    private FrameLayout mFlPlayMusic;
    private ImageView iv_next,iv_previous,iv_pausePlay;
    private Animation mPlayMusicAnim;
    private UpdateSeekBar updateProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_music);

        musicViewModel= ViewModelProviders.of(this).get(MusicViewModel.class);
        initData();
        initView();

        //检查权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            initMediaPlayer();
        }


    }

    private void initData () {
        mMusicId = getIntent().getIntExtra(MUSIC_ID,1);
        musicAmount=getIntent().getIntExtra(MUSIC_AMOUNT,1);
        mMusic=musicViewModel.getMusicById(mMusicId);
    }

    private void initView () {
        mFlPlayMusic=findViewById(R.id.fl_play_music);
        mIvIcon = findViewById(R.id.iv_icon);
        mIvBg = findViewById(R.id.iv_bg);
        mTvName = findViewById(R.id.tv_name);
        mTvAuthor = findViewById(R.id.tv_author);

        iv_next=findViewById(R.id.imageView_next);
        iv_pausePlay=findViewById(R.id.imageView_pausePlay);
        iv_previous=findViewById(R.id.imageView_previous);
        seekBar=findViewById(R.id.seekBar);

        iv_pausePlay.setOnClickListener(this);
        iv_previous.setOnClickListener(this);
        iv_next.setOnClickListener(this);

        setMusicIcon();
        mPlayMusicAnim = AnimationUtils.loadAnimation(this, R.anim.play_music_anim);
    }




    private void initMediaPlayer() {
        try {
            if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    isPlaying=false;
                mediaPlayer.reset();
            }
            //本地测试
//            File file = new File(Environment.getExternalStorageDirectory(), "Music/my_music.mp3");
//            mediaPlayer.setDataSource(file.getPath());
//            mediaPlayer.prepare();
//            start();

           //在线测试
            mediaPlayer.setDataSource(this, Uri.parse(mMusic.getPath()));
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    start();
                }
            });

        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requstCode, String[] permissions, int[] grantResults) {
        switch (requstCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,"正在加载",Toast.LENGTH_SHORT).show();
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
        switch (v.getId())   {
            case R.id.imageView_pausePlay:
                if(isPlaying)
                    pause();
                else
                    start();
                break;
            case R.id.imageView_previous:
                previous();
                break;
            case R.id.imageView_next:
                next();
                break;
        }
    }

    private void previous() {
        if(mMusic.getMusicId()==0)
            mMusic=musicViewModel.getMusicById(musicAmount-1);
        else
            mMusic=musicViewModel.getMusicById((mMusic.getMusicId()-1)%musicAmount);
        Log.e("previous",Math.abs((mMusic.getMusicId()-1)-musicAmount)%musicAmount+"");
        setMusicIcon();
        initMediaPlayer();

    }
    private void next() {
        mMusic=musicViewModel.getMusicById((mMusic.getMusicId()+1)%musicAmount);
        Log.e("next",(mMusic.getMusicId()+1)%musicAmount+"");
        setMusicIcon();
        initMediaPlayer();
    }

    private void start() {
        Log.e("start","isPlaying "+isPlaying);
        if(isPlaying)return;
        else {
            try {
                mediaPlayer.start();
                isPlaying = true;
                seekBar.setMax(mediaPlayer.getDuration());
                updateProgress = new UpdateSeekBar();
                updateProgress.execute(1000);
                seekBar.setOnSeekBarChangeListener(new MySeekBarChangeListener());
            } catch (Exception e) {
                isPlaying = false;
                Toast.makeText(this, "播放错误", Toast.LENGTH_SHORT).show();
            }
            mFlPlayMusic.startAnimation(mPlayMusicAnim);
            iv_pausePlay.setImageDrawable(this.getDrawable(R.drawable.ic_pause_circle_outline_black_64dp));
        }
    }

    private void end() {
        if (isPlaying) {
            isPlaying = false;
            iv_pausePlay.setImageDrawable(this.getDrawable(R.drawable.ic_play_circle_outline_black_64dp));
            mediaPlayer.stop();
            mFlPlayMusic.clearAnimation();
        }
    }

    private void pause() {
        if (isPlaying) {
            isPlaying = false;
            iv_pausePlay.setImageDrawable(this.getDrawable(R.drawable.ic_play_circle_outline_black_64dp));
            mediaPlayer.pause();
           mFlPlayMusic.clearAnimation();
        }
    }

    private void restart() {
        if (mediaPlayer.isPlaying()) {
            isPlaying = true;
            mediaPlayer.seekTo(0);
        }
    }

    private class UpdateSeekBar extends AsyncTask<Integer,Integer,String> {
        @Override
        protected void onPostExecute(String s) {}
        @Override
        protected void onProgressUpdate(Integer... values) {
            seekBar.setProgress(values[0]);
        }
        @Override
        protected String doInBackground(Integer... params) {
            while(isPlaying){
                try {
                    Thread.sleep(params[0]);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                this.publishProgress(mediaPlayer.getCurrentPosition());
            }
            return null;
        }
    }
    private class MySeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){  }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {   }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mediaPlayer.seekTo(seekBar.getProgress());
        }
    }

    /**
     * 设置光盘中显示的音乐封面图片，歌曲名及作者
     */
    private void setMusicIcon () {
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
    }

//    private void loadingCover(String mediaUri) {
//        MediaMetadataRetriever mediaMetadataRetriever=new MediaMetadataRetriever();
//        mediaMetadataRetriever.setDataSource(mediaUri);
//        byte[] picture = mediaMetadataRetriever.getEmbeddedPicture();
//        Bitmap bitmap= BitmapFactory.decodeByteArray(picture,0,picture.length);
//        mMusic.setPoster();
//    }

}
