package com.example.localmusicplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.PrimitiveIterator;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView nextIv,playIv,lastIv,albumIv,menuIv;
    private TextView singerTv,songTv;
    private RecyclerView musicRv;
    private SearchView musicSearch;
    private List<LocalMusicBean> MainData,SetData;
    private LocalMusicAdapter MusicAdapter;

    private int musicDataSize;
    private int currentId=-2;
    private MusicService.MyBinder mMyBinder;
    private ServiceConnection serviceConnection;
    private boolean isNetAvailable = false;
    private Handler handler;
    private SeekBar seekBar;
    private Runnable runnable;
    private BroadcastReceiver myReceiver;

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private Fragment fragment_about,fragment_introduction,fragment_message;
    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;
    private int Notification_height;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        setRecycleView();
        loadLocalMusicData();
        Intent serviceStart = new Intent(this,MusicService.class);
        startService(serviceStart);
        Intent mediaServiceIntent = new Intent(this,MusicService.class);
        serviceConn();
        bindService(mediaServiceIntent,serviceConnection,BIND_AUTO_CREATE);
        setReceiver();
        setSearchList();
        setDrawer();
        setEventListener();
        setFragment();


    }

    private void setFragment() {
        fragmentManager = getSupportFragmentManager();
        fragment_message = new Frag_message();
        fragment_introduction = new Frag_introduction();
        fragment_about = new Frag_about();
        transaction = fragmentManager.beginTransaction();

    }


    private void setEventListener() {
        //设置点击事件
        MusicAdapter.setOnItemClickListener(new LocalMusicAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position) {
                LocalMusicBean bean = SetData.get(position);
                playMusicOnService(Integer.parseInt(bean.getId())-1);
            }
        });
//        设置单曲循环/列表循环
        albumIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMyBinder.setPlayModule();
                if(mMyBinder.getPlayModule()){
                    Toast.makeText(MainActivity.this,"单曲循环",Toast.LENGTH_SHORT).show();
                    albumIv.setImageResource(R.mipmap.icon_song2_loop_blue);
                }else{
                    Toast.makeText(MainActivity.this,"列表循环",Toast.LENGTH_SHORT).show();
                    albumIv.setImageResource(R.mipmap.icon_song2);
                }

            }
        });
        menuIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    private void playMusicOnService(int Id) {
        if(Id<10 && isNetAvailable) Toast.makeText(MainActivity.this,"在线歌曲",Toast.LENGTH_SHORT).show();
        mMyBinder.setMusic(Id);
        playIv.setImageResource(R.drawable.ripple_pause);
    }

    private void setDrawer() {
        navigationView.setItemIconTintList(null);
        navigationView.getChildAt(0).setVerticalScrollBarEnabled(false);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.item_0:
                        Log.d("ItemSelectedListener", "item0");
                        transaction = fragmentManager.beginTransaction();
                        transaction.remove(fragment_about).commit();
                        transaction = fragmentManager.beginTransaction();
                        transaction.remove(fragment_introduction).commit();
                        transaction = fragmentManager.beginTransaction();
                        transaction.remove(fragment_message).commit();
                        break;
                    case R.id.item_1:
                        transaction = fragmentManager.beginTransaction();
                        transaction.replace(R.id.fragment,fragment_message).commit();
                        Log.d("ItemSelectedListener", "item1");
                        Toast.makeText(MainActivity.this,"没有新消息噢",Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.item_2:
                        transaction = fragmentManager.beginTransaction();
                        transaction.replace(R.id.fragment,fragment_introduction).commit();
                        Log.d("ItemSelectedListener", "item2");
                        break;
                    case R.id.item_3:
                        transaction = fragmentManager.beginTransaction();
                        transaction.replace(R.id.fragment,fragment_about).commit();
                        Log.d("ItemSelectedListener", "item3");
                        break;
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                transaction.addToBackStack(null);
                return true;
            }
        });
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                Display display = MainActivity.this.getWindowManager().getDefaultDisplay();
                display.getMetrics(metrics);
                RelativeLayout relativeLayout = findViewById(R.id.main_activity);
                relativeLayout.layout(drawerView.getRight(),Notification_height,drawerView.getRight()+metrics.widthPixels,metrics.heightPixels);

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }

    private void setSearchList() {
        musicSearch.setIconifiedByDefault(true);
        musicSearch.setFocusable(false);
        musicSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(TextUtils.isEmpty(s)){
                    SetData.clear();
                    SetData.addAll(MainData);
                    MusicAdapter.notifyDataSetChanged();
                }else {
                    SetData.clear();
                    for(LocalMusicBean bean:MainData){
                        if(bean.getSong().contains(s)||bean.getSinger().contains(s)){
                            SetData.add(bean);
                        }
                    }
                    MusicAdapter.notifyDataSetChanged();
                }

                return true;
            }
        });
    }

    private void setReceiver() {
        myReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                songTv.setText(intent.getStringExtra("music_song"));
                singerTv.setText(intent.getStringExtra("music_singer"));
                currentId = intent.getIntExtra("music_id",-1);
                seekBar.setMax(intent.getIntExtra("music_duration",0));
            }
        };
        IntentFilter intentFilter = new IntentFilter("UI_info");
        registerReceiver(myReceiver,intentFilter);
    }

    private void serviceConn() {
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                mMyBinder = (MusicService.MyBinder) iBinder;
                if(mMyBinder.getMediaPlayState()==1){
                    playIv.setImageResource(R.mipmap.icon_pause);
                    songTv.setText(mMyBinder.getMusicSong());
                    singerTv.setText(mMyBinder.getMusicSinger());
                    currentId = mMyBinder.getMusicId();
                    seekBar.setMax(mMyBinder.getMusicDuration());
                    Log.d("currentId",Integer.toString(currentId));
                }
                mMyBinder.setDatas(MainData);
                seekBar.setProgress(0);
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        if(b && currentId!=-1 && currentId!=-2){
                            mMyBinder.seekToPosition(seekBar.getProgress());
                            playIv.setImageResource(R.drawable.ripple_pause);
                        }else if(b){
                            Toast.makeText(MainActivity.this,"请选择播放音乐",Toast.LENGTH_SHORT).show();
                            seekBar.setProgress(0);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                handler = new Handler();
                runnable = new Runnable() {
                    private int pre=-1,pos;
                    @Override
                    public void run() {
                        pos=mMyBinder.getPlayPosition();
                        if(currentId!=-1 && currentId!=-2)
                            seekBar.setProgress(pos);
                        Log.d("RunnablePos",String.valueOf(pos));
                        if(pre!=pos)  handler.postDelayed(runnable,1000);
                        else handler.postDelayed(runnable,1000);
                        pre = pos;
                    }
                };
                handler.post(runnable);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
    }

    private void setRecycleView() {
        MainData = new ArrayList<>();
        SetData = new ArrayList<>();
        MusicAdapter = new LocalMusicAdapter(this,SetData);
        musicRv.setAdapter(MusicAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        musicRv.setLayoutManager(layoutManager);
    }

    private void loadLocalMusicData() {
        //加载本地存储当中的音乐文件到集合当中
        //获取content resolver对象
        ContentResolver resolver = getContentResolver();
        //获取本地音乐存储的uri地址
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        //开始查询地址
        Cursor cursor = resolver.query(uri, null, null, null, null);
        //遍历cursor
        int id = -1;
        LocalMusicBean bean;
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if(networkInfo!=null&&networkInfo.isConnected()){
            isNetAvailable=true;
            bean = new LocalMusicBean(String.valueOf((++id)+1),"Good guy","SF9","SENSATION",339000,"https://m10.music.126.net/20210502191326/fe76beee5edb7aaee611db0a9618b76e/yyaac/060c/5452/040e/1f98bf8fbfc8ac573ade3d468f445bc0.m4a");
            MainData.add(bean);
        }
        while(cursor.moveToNext()){
            long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            if(duration>30*1000) {
                String song = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String singer = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                if (album.equals("Sounds")) continue;
                ++id;
                String sid = String.valueOf(id + 1);
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                bean = new LocalMusicBean(sid, song, singer, album, duration, path);
                MainData.add(bean);
            }
        }
        if(id==0){
            bean = new LocalMusicBean("0","没有找到本地歌曲","","",0,"");
            MainData.add(bean);
            currentId=-2;
        }else {
            currentId=-1;
        }
        musicDataSize=MainData.size();
        SetData.addAll(MainData);
        MusicAdapter.notifyDataSetChanged();

    }

    private void initView() {
       nextIv = findViewById(R.id.bottom_iv_next);
       playIv = findViewById(R.id.bottom_iv_play);
       lastIv = findViewById(R.id.bottom_iv_last);
       albumIv = findViewById(R.id.bottom_iv_icon);
       singerTv = findViewById(R.id.bottom_tv_singer);
       songTv = findViewById(R.id.bottom_tv_song);
       musicRv = findViewById(R.id.music_rv);
       seekBar = findViewById(R.id.music_seekBar);
       musicSearch = findViewById(R.id.music_search);
       drawerLayout = findViewById(R.id.drawer);
       navigationView = findViewById(R.id.nav_view);
       menuIv = findViewById(R.id.menu_icon);
       int resourceId = getResources().getIdentifier("status_bar_height","dimen","android");
       Notification_height = getResources().getDimensionPixelSize(resourceId);
       nextIv.setOnClickListener(this);
       lastIv.setOnClickListener(this);
       playIv.setOnClickListener(this);
       seekBar.setProgress(0);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(myReceiver);
        super.onDestroy();
        handler.removeCallbacks(runnable);
        unbindService(serviceConnection);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.bottom_iv_last:
                if(currentId==-2){
                    Toast.makeText(this,"没有获取到音乐",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(currentId==-1){
                    Toast.makeText(this,"开始播放第一首",Toast.LENGTH_SHORT).show();
                    currentId=1;
                }
                if(currentId==0){
                    Toast.makeText(this,"已经是第一首了",Toast.LENGTH_SHORT).show();
                    return;
                }
                currentId = currentId-1;
                playMusicOnService(currentId);
                break;
            case R.id.bottom_iv_next:
                if(currentId==-2){
                    Toast.makeText(this,"没有获取到音乐",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(currentId==-1){
                    Toast.makeText(this,"开始播放最后一首",Toast.LENGTH_SHORT).show();
                    currentId=musicDataSize-2;
                }
                if(currentId==musicDataSize-1){
                    Toast.makeText(this,"没有下一首了",Toast.LENGTH_SHORT).show();
                    return;
                }
                currentId=currentId+1;
                playMusicOnService(currentId);
                break;
            case R.id.bottom_iv_play:
                if(currentId==-1){
                    Toast.makeText(this,"请选择播放音乐",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(currentId==-2){
                    Toast.makeText(this,"请打开软件存储权限",Toast.LENGTH_SHORT).show();
                    return;
                }
                int state = mMyBinder.getMediaPlayState();
                if(state==1){
                    mMyBinder.pauseMusic();
                    playIv.setImageResource(R.drawable.ripple_play);
                }else if(state==0){
                    mMyBinder.playMusic();
                    playIv.setImageResource(R.drawable.ripple_pause);
                }else if(state==2){
                    Toast.makeText(this,"播放结束了",Toast.LENGTH_SHORT).show();
                }
                break;

    }
}
}