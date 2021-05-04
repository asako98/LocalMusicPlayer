package com.example.localmusicplayer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class Frag_message extends Fragment implements View.OnTouchListener {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.menu_message,container,false);
        view.setOnTouchListener((View.OnTouchListener)this);
        return  view;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return true;
    }
}
