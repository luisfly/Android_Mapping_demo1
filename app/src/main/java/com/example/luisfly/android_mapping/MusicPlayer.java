package com.example.luisfly.android_mapping;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MusicPlayer extends AppCompatActivity {

    private ListView music_list;
    private String path;
    private int playing_song = -1 ;
    private MediaPlayer mediaPlayer = new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        music_list = (ListView) findViewById(R.id.music_list);
        ArrayList<String> show_music = new ArrayList<String>();

        List<MediaEntity> music_search = MediaEntity.getAllMediaList(this,"1=1");
        if (music_search != null) {
            for (MediaEntity e : music_search) {
                show_music.add("music name: " + e.display_name);
            }
            Toast.makeText(this, "add success", Toast.LENGTH_SHORT).show();
        }
        else {
            show_music.add("Your return music list is empty.");
            Toast.makeText(this, "music list is empty", Toast.LENGTH_SHORT).show();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, show_music);
        music_list.setAdapter(adapter);

        music_list.setOnItemClickListener((AdapterView<?> parent, View v, int position, long id)->{
            MediaEntity play = music_search.get(position);
            path = play.path;

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {
                initMediaPlayer(play.path);
            }
        });
    }

    private void initMediaPlayer(String path) {
        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.reset();
            }
            mediaPlayer.setDataSource(path);
            //mediaPlayer.release();
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.getMessage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int res : grantResults) {
                        if (res != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "拒绝权限将无法使用程序", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                    initMediaPlayer(path);
                }
                break;
            default:
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}
