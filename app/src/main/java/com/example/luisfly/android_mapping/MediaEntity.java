package com.example.luisfly.android_mapping;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by louis fly on 2018/2/26.
 */

public class MediaEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    public int id;
    public String title;
    public String display_name;
    public String path;
    public int duration;
    public String albums;
    public String artist;
    public String singer;
    public long size;

    public static List<MediaEntity> getAllMediaList(Context context, String selection) {
        Cursor cursor = null;
        List<MediaEntity> mediaList = null;
        try {
            cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
                            MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DURATION,
                            MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DATA,
                            MediaStore.Audio.Media.SIZE},
                    selection, null,null);
            if (cursor == null) {
                Log.d("MediaEntity", "The get MediaList cursor is null.");
                return mediaList;
            }
            int count = cursor.getCount();
            if (count <= 0) {
                Log.d("MediaEntity", "The getMediaList cursor count is 0.");
            }
            mediaList = new ArrayList<MediaEntity>();
            MediaEntity mediaEntity = null;
            while (cursor.moveToNext()) {
                mediaEntity = new MediaEntity();
                mediaEntity.id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                mediaEntity.title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                mediaEntity.display_name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                mediaEntity.duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                mediaEntity.size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));

                if (!checkIsMusic(mediaEntity.duration, mediaEntity.size)) {
                    continue;
                }
                mediaEntity.artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                mediaEntity.path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                mediaList.add(mediaEntity);
            }
        } catch (Exception e) {
            Log.e("MediaEntity", e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return mediaList;
    }

    private static boolean checkIsMusic(int time, long size) {
        if (time <=0 || size <=0) {
            return false;
        }

        time /= 1000;
        int minute = time / 60;
        int second = time % 60;
        if (minute <= 0 && second <= 30) {
            return false;
        }
        if (size <= 1024 * 1024) {
            return false;
        }
        return true;
    }
}
