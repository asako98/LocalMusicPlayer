package com.example.localmusicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.List;


public class MusicService extends Service {
    private MediaPlayer mediaPlayer;
    private MyBinder myBinder;
    private List<LocalMusicBean> mDatas;
    private int musicDataSize;
    private int pausePosition;
    private int music_Id,music_duration;
    private String path;
    private String music_singer,music_song;
    private boolean playState,playModule;

    public MusicService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if(mediaPlayer!=null){
                    myBinder.play_next();
                }
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                return true;
            }
        });
        music_Id=-1;
        music_singer="";
        music_song="";
        pausePosition=0;
        playState=false;
        playModule=false;
        myBinder=new MyBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }
    public class MyBinder extends Binder{

        public boolean setDatas(List<LocalMusicBean> musicBeans){
            Log.d("MusicService----0----", "setDatas");
            mDatas = musicBeans;
            musicDataSize = mDatas.size();
            return true;
        }
        public void setMusic(int Id){
            Log.d("MusicService----1----", "setMusic");
            mediaPlayer.reset();
            pausePosition=0;
            music_Id=Id;
            LocalMusicBean musicBean = mDatas.get(music_Id);
            try{
                path = musicBean.getPath();
                mediaPlayer.setDataSource(path);
                music_song=musicBean.getSong();
                music_singer=musicBean.getSinger();
                music_duration= (int) musicBean.getDuration();
                Intent intentInfo = new Intent("com.example.localmusicplayer.intentService");
                intentInfo.setPackage(getPackageName());
                intentInfo.putExtra("music_id",music_Id);
                intentInfo.putExtra("music_song",music_song);
                intentInfo.putExtra("music_singer",music_singer);
                intentInfo.putExtra("music_duration",music_duration);
                startService(intentInfo);
                myBinder.playMusic();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public void playMusic() {
            Log.d("MusicService----2----", "playMusic");
            if(mediaPlayer!=null&&!mediaPlayer.isPlaying()){
                if(pausePosition==0){
                    try{
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                        playState=false;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{
                    mediaPlayer.seekTo(pausePosition);
                    mediaPlayer.start();
                    playState=false;
                }
            }
        }
        public void pauseMusic(){
            Log.d("MusicService----3----", "pauseMusic");
            if(mediaPlayer!=null&&mediaPlayer.isPlaying()){
                pausePosition = mediaPlayer.getCurrentPosition();
                mediaPlayer.pause();
            }
        }
        public void play_next(){
            if(!playModule){
                if(music_Id>=musicDataSize-1){
                    music_Id=-1;
                }
                music_Id = music_Id+1;
            }
            setMusic(music_Id);
            playMusic();
            playState=false;
        }
       public void play_last(){
            music_Id =  music_Id-1;
            if(music_Id==-1) return;
            setMusic(music_Id);
            playMusic();
       }
       public void stopMusic(){
           Log.d("MusicService----4----", "stopMusic");
           if(mediaPlayer!=null){
               pausePosition=0;
               mediaPlayer.pause();
               mediaPlayer.seekTo(0);
               mediaPlayer.stop();
           }
       }
       public int getPlayPosition(){
//            返回播放进度
           Log.d("MusicService----5----", "getPlayPosition");
           if(mediaPlayer!=null){
               return mediaPlayer.getCurrentPosition();
           }
           return 0;
       }
       public void seekToPosition(int msec){
//            设置播放进度
           Log.d("MusicService----6----", "seekToPosition");
           if(mediaPlayer!=null){
               if(!mediaPlayer.isPlaying()) myBinder.playMusic();
               playState = false;
               mediaPlayer.seekTo(msec);
           }
       }
       public int getMediaPlayState(){
//            返回播放状态
           Log.d("MusicService----7----", "getMediaPlayState");
           if(mediaPlayer!=null){
               if(mediaPlayer.isPlaying()) return 1;
               else if(playState) return 2;
               else  return  0;
           }
           else return 0;
       }
       public String getMusicSinger(){
            Log.d("MusicService----8----", "getMusicSinger");
            if(mediaPlayer!=null) return music_singer;
            else return  "";
        }
        public String getMusicSong(){
            Log.d("MusicService----9----", "getMusicSong");
            if(mediaPlayer!=null) return music_song;
            else return  "";
        }
        public int getMusicId(){
            Log.d("MusicService----10----", "getMusicId");
            if(mediaPlayer!=null) return music_Id;
            else return -1;
        }
        public int getMusicDuration(){
            Log.d("MusicService----11----", "getMusicDuration");
            if(mediaPlayer!=null) return mediaPlayer.getDuration();
            else return -1;
        }
        public void setPlayModule(){
//            设置播放模式
            if(playModule) playModule=false;
            else playModule=true;
        }
        public boolean getPlayModule(){return playModule;}




    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mediaPlayer!=null){
            pausePosition=0;
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
            mediaPlayer.stop();
        }
    }
}
