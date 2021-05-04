package com.example.localmusicplayer;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class Frag_about extends Fragment implements View.OnTouchListener{
    TextView qq,email,blog;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.menu_about,container,false);
        view.setOnTouchListener((View.OnTouchListener)this);
        qq=view.findViewById(R.id.link_qq);
        blog=view.findViewById(R.id.link_blog);
        email=view.findViewById(R.id.link_email);
        qq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(),"qq:14134443",Toast.LENGTH_SHORT).show();
            }
        });
        blog.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        blog.getPaint().setAntiAlias(true);
        blog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://github.com/asako98"));
                startActivity(Intent.createChooser(intent,"Choose a Broswer"));
            }
        });
        email.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        email.getPaint().setAntiAlias(true);
        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("message/rfc822");
                intent.putExtra(Intent.EXTRA_EMAIL,new String[]{"chengwangcheng@foxmail.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT,"this is a test mail");
                intent.putExtra(Intent.EXTRA_TEXT,"welcome!Contact me anytime~");
                startActivity(Intent.createChooser(intent,"Choose an email client"));
            }
        });
        return view;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return true;
    }
}
