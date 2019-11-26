package com.kailang.music;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.kailang.music.utils.JsonFileUtils;
import com.kailang.music.adapters.MusicListAdapter;
import com.kailang.music.data.Music;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView  mRvList;
    private MusicListAdapter mListAdapter;
    private MusicViewModel musicViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        musicViewModel = ViewModelProviders.of(this).get(MusicViewModel.class);
        musicViewModel.getAllMusicsLive().observe(this, new Observer<List<Music>>() {
            @Override
            public void onChanged(List<Music> music) {
                if (music==null||music.isEmpty()){
                    initData();
                }else {
                    Log.e("notifyDataSetChanged",music.size()+"");
                    mListAdapter.setmDataSource(music);
                    mListAdapter.notifyDataSetChanged();
                }

            }
        });
        initView();
    }

    private void initData () {
        Gson gson = new Gson();
        String musicJsonData = JsonFileUtils.getJson(this, "Music.json");
        Music[] music = gson.fromJson(musicJsonData, Music[].class);
        musicViewModel.insertMusic(music);
    }

    private void initView () {

//        mRvGrid = findViewById(R.id.rv_grid);
//        mRvGrid.setLayoutManager(new GridLayoutManager(this, 3));
//        mRvGrid.addItemDecoration(new GridSpaceItemDecoration(getResources().getDimensionPixelSize(R.dimen.albumMarginSize), mRvGrid));
//        mRvGrid.setNestedScrollingEnabled(false);
//        mGridAdapter = new MusicGridAdapter(this, mMusicSourceModel.getAlbum());
//        mRvGrid.setAdapter(mGridAdapter);

        mRvList = findViewById(R.id.rv_list);
        mRvList.setLayoutManager(new LinearLayoutManager(this));
        mRvList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mRvList.setNestedScrollingEnabled(false);
        mListAdapter = new MusicListAdapter(this, mRvList);
        mRvList.setAdapter(mListAdapter);
    }


}
