package com.example.luisfly.android_mapping;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MessageShow extends AppCompatActivity {

    private ListView image_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_show);

        image_list = (ListView) findViewById(R.id.test);
        //List<ImageEntity> showImageList = ImageEntity.getShowImage(this);
        List<ImageEntity> showImageList = ImageEntity.GetImagePath();
        ArrayList<String> imageList = new ArrayList<>();

        if (showImageList != null) {
            for (ImageEntity e : showImageList) {
                imageList.add("Image name: " + e.name);
            }
            Toast.makeText(this, "find the image", Toast.LENGTH_SHORT).show();
            Log.d("test", "find the image");
        } else {
            imageList.add("Error");
            Toast.makeText(this, "showImageList is null", Toast.LENGTH_SHORT).show();
            Log.d("test", "showImageList is null");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, imageList);
        image_list.setAdapter(adapter);

    }
}
