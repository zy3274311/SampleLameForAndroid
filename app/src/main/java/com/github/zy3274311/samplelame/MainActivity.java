package com.github.zy3274311.samplelame;

import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.github.zy3274311.libmp3lame.MP3Recorder;

import java.io.File;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {
    private View start_btn;
    private View stop_btn;
    private View play_btn;
    private TextView path_tv;
    private MP3Recorder mMP3Recorder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions();
        path_tv = findViewById(R.id.path_tv);
        start_btn = findViewById(R.id.start_btn);
        stop_btn = findViewById(R.id.stop_btn);
        play_btn = findViewById(R.id.play_btn);

        File dir = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        String filename = System.currentTimeMillis() + ".mp3";
        File mp3 = new File(dir, filename);

        mMP3Recorder = new MP3Recorder();
        mMP3Recorder.setOutputPath(mp3.getPath());
        mMP3Recorder.prepare();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mMP3Recorder !=null){
            mMP3Recorder.release();
            mMP3Recorder = null;
        }
    }

    public void startRecord(View view) {
        if(mMP3Recorder !=null){
            try {
                mMP3Recorder.start();
                start_btn.setVisibility(View.GONE);
                stop_btn.setVisibility(View.VISIBLE);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopRecord(View view) {
        if(mMP3Recorder !=null){
            try {
                mMP3Recorder.stop();
                String path = mMP3Recorder.getOutputPath();
                path_tv.setText(path);

                start_btn.setVisibility(View.VISIBLE);
                stop_btn.setVisibility(View.GONE);
                play_btn.setVisibility(View.VISIBLE);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    }

    public void play(View view) {
        try {
            String path = path_tv.getText().toString();
            MediaPlayer player = new MediaPlayer();
            player.setDataSource(path);
            player.prepare();
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void requestPermissions(){
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO}, 0);
        }
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

}
