package com.example.my_music_player;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {
  Button btnplay, btnnext, btnprev, btnff, btnfr;
  TextView txtsname ,txtsstart, txtsstop;
  SeekBar seekmusic,seekaudio;
  BarVisualizer visualizer;

  String  sname;

  Thread updateseekbar;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if(visualizer!= null){
            visualizer.release();
        }
        super.onDestroy();
    }

    public static  final  String  EXTRA_NAME ="song_name";
    static MediaPlayer mediaPlayer;
    int postion;
    ArrayList<File> mySongs;
  ImageView imageView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        getSupportActionBar().setTitle("Now Playing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
       seekaudio=findViewById(R.id.seekaudio);
        btnff=findViewById(R.id.btnff);
        btnplay=findViewById(R.id.playbtn);
        btnnext=findViewById(R.id.btnnext);
        btnprev=findViewById(R.id.btnprev);
        btnfr=findViewById(R.id.btnfr);
        txtsname=findViewById(R.id.txtsn);
        txtsstart=findViewById(R.id.txtsstart);
        txtsstop=findViewById(R.id.txtsstop);
        seekmusic=findViewById(R.id.seekbar);
        visualizer=findViewById(R.id.blast);
     imageView=findViewById(R.id.imageview);
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        Intent intent = getIntent();
        Bundle bundle =intent.getExtras();

        mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
        String songName =intent.getStringExtra("songname");
        postion= bundle.getInt("pos",0);
        txtsname.setSelected(true);
        Uri uri =Uri.parse(mySongs.get(postion).toString());
        sname = mySongs.get(postion).getName();
        txtsname.setText(sname);

        mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
        mediaPlayer.start();

        updateseekbar =new Thread(){
            @Override
            public void run() {
                int totalDuration =mediaPlayer.getDuration();
                int currentpostion = 0;
                while (currentpostion<totalDuration){
                    try {
                        sleep(500);
                        currentpostion=mediaPlayer.getCurrentPosition();
                        seekmusic.setProgress(currentpostion);
                    }catch (InterruptedException | IllegalStateException e){
                        e.printStackTrace();
                    }
                }

            }
        };


        seekaudio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekaudio.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekaudio.setVisibility(View.VISIBLE);
                int progress=seekBar.getProgress();
                int x = (int) Math.ceil(progress / 1000f);

                if (x != 0 && mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    clearMediaPlayer();
                    fab.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_play));
                    MainActivity.this.seekBar.setProgress(0);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekmusic.setMax(mediaPlayer.getDuration());
        updateseekbar.start();
        seekmusic.getProgressDrawable().setColorFilter(getResources().getColor(R.color.design_default_color_primary), PorterDuff.Mode.MULTIPLY);
        seekmusic.getThumb().setColorFilter(getResources().getColor(R.color.design_default_color_primary),PorterDuff.Mode.SRC_IN);

        seekmusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                    mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        String endTime = createTime(mediaPlayer.getDuration());
        txtsstop.setText(endTime);

        final Handler handler =new Handler();
        final int delay =1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime =createTime(mediaPlayer.getCurrentPosition());
                txtsstart.setText(currentTime);
                handler.postDelayed(this,delay);
            }
        },delay);

      btnplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 if(mediaPlayer.isPlaying()){
                     btnplay.setBackgroundResource(R.drawable.ic_play);
                     mediaPlayer.pause();
                 }else{
                     btnplay.setBackgroundResource(R.drawable.ic_pause);
                     mediaPlayer.start();
                 }
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                btnnext.performClick();
            }
        });

        int audiosessionId = mediaPlayer.getAudioSessionId();
        if(audiosessionId != -1){
            visualizer.setAudioSessionId(audiosessionId);
        }


        btnnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                postion=(postion+1)%mySongs.size();
                Uri u=Uri.parse(mySongs.get(postion).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(),u);
                sname = mySongs.get(postion).getName();
                txtsname.setText(sname);
                mediaPlayer.start();
                btnplay.setBackgroundResource(R.drawable.ic_pause);
                 startAnimation(imageView);
                int audiosessionId = mediaPlayer.getAudioSessionId();
                if(audiosessionId != -1){
                    visualizer.setAudioSessionId(audiosessionId);
                }
            }
        });

        btnprev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                postion=(postion-1<0)?mySongs.size()-1:postion-1;
                Uri u=Uri.parse(mySongs.get(postion).toString());
                mediaPlayer =mediaPlayer.create(getApplicationContext(),u);
                sname = mySongs.get(postion).getName();
                txtsname.setText(sname);
                mediaPlayer.start();
                btnplay.setBackgroundResource(R.drawable.ic_pause);
                startAnimation(imageView);
                int audiosessionId = mediaPlayer.getAudioSessionId();
                if(audiosessionId != -1){
                    visualizer.setAudioSessionId(audiosessionId);
                }
            }
        });

        btnff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10000);
                }
            }
        });

        btnfr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-10000);
                }
            }
        });
    }
    public void startAnimation(View view){
        ObjectAnimator animator =ObjectAnimator.ofFloat(imageView,"rotation",0f,360f);
        animator.setDuration(1000);
        AnimatorSet animatorSet=new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();
    }

    public String createTime(int duration){
        String time = "";
        int minut =duration/1000/60;
        int sec = (duration/1000)%60;

        time+=minut+":";

        if(sec<10){
            time+="0";
        }
        time+=sec;
        return time;
    }


}

    private void clearMediaPlayer() {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }
}
