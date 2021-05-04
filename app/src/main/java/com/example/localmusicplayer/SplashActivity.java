package com.example.localmusicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DISPLAY_LENGTH = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions
                .request(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_NETWORK_STATE)
                .subscribe(granded->{
                    if(granded){
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(SplashActivity.this,MainActivity.class);
                                startActivity(intent);
                                SplashActivity.this.finish();
                            }
                        },SPLASH_DISPLAY_LENGTH);
                        Log.d("rxRermision","get");
                    }else{
                        Toast.makeText(this,"未获取到存储权限",Toast.LENGTH_LONG).show();
                        Log.d("rxRermision","denied");
                    }
                });
    }
}