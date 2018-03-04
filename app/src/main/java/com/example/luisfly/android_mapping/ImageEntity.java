package com.example.luisfly.android_mapping;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by louis fly on 2018/2/27.
 */

public class ImageEntity implements Serializable {

    public String path;
    public String name;
    public long size;

    public static List<ImageEntity> getShowImage(Context contenx) {
        Cursor cursor = null;
        List<ImageEntity> showImageList = new ArrayList<>();
        String filePath;

        if ((filePath=initImageDir()) == null) {
            Log.d("ImageEntity", "Init Image file failed.");
            return null;
        }
        try{
            Uri uri = Uri.parse(filePath);
            cursor = contenx.getContentResolver().query(uri, null, "1=1", null, null);
            if (cursor == null) {
                Log.d("ImageEntity", "The get ImageList cursor is null.");
                return showImageList;
            }
            int count = cursor.getCount();
            if (count <= 0) {
                Log.d("ImageEntity", "The ImageList cursor count is 0.");
            }
            showImageList = new ArrayList<ImageEntity>();
            ImageEntity imageEntity = null;
            while (cursor.moveToNext()) {
                imageEntity = new ImageEntity();
                imageEntity.path = cursor.getString(cursor.getColumnIndex(MediaStore.
                        Images.Media.DATA));
                imageEntity.name = cursor.getString(cursor.getColumnIndex(MediaStore.
                        Images.Media.DISPLAY_NAME));
                imageEntity.size = cursor.getLong(cursor.getColumnIndex(MediaStore.
                        Images.Media.SIZE));

                showImageList.add(imageEntity);
            }
        } catch (Exception e) {
            Log.e("ImageEntity", e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return showImageList;
    }

    private static String initImageDir() {
        String mSDCardPath = getSdcardDir();
        String imageFile = "Android_Mapping_demo1/Image";
        if (mSDCardPath == null) {
            return null;
        }
        File f = new File(mSDCardPath, imageFile);
        if (!f.exists()) {
            try {
                f.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return f.getAbsolutePath();
    }

    private static String getSdcardDir() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }

    public static List<ImageEntity> GetImagePath() {
        String filePath;
        if ((filePath=initImageDir()) == null) {
            Log.d("ImageEntity", "Init Image file failed.");
            return null;
        }

        File file = new File(filePath);
        File[] subFile = file.listFiles();
        List<ImageEntity> showImageList = new ArrayList<ImageEntity>();

        Log.d("ImageEntity", "Your path is " + filePath);
        if (subFile != null) {
            if (subFile.length == 0) {
                Log.d("ImageEntity", "The ImageList count is 0.");
            }
            ImageEntity imageEntity = null;
            for (int i = 0; i < subFile.length; i++) {
                if (!subFile[i].isDirectory()) {
                    imageEntity = new ImageEntity();
                    imageEntity.name = subFile[i].getName();
                    imageEntity.path = subFile[i].getAbsolutePath();
                    imageEntity.size = subFile[i].getTotalSpace();
                    showImageList.add(imageEntity);
                }
            }
        } else {
            Log.d("ImageEntity", "The ImageList is null.");
            return null;
        }

        return showImageList;
    }

}
