package com.example.makoto.camerarolltest2;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Makoto on 2016/03/30.
 */
public class PhotoGalleryImageProvider {
    public static final int IMAGE_RESOLUTION = 15;
    public static final String CAMERA_IMAGE_BUCKET_NAME =
            Environment.getExternalStorageDirectory().toString()
                    + "/DCIM/Camera";
    public static final String CAMERA_IMAGE_BUCKET_ID =
            getBucketId(CAMERA_IMAGE_BUCKET_NAME);

    public static List<PhotoItem> getAlbumThumbnails(Context context){
        int count = 0;

        final String[] projection = {MediaStore.Images.Media._ID};

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,       // Which columns to return
                null,       // Return all rows
                null,
                MediaStore.Images.Media.DATE_TAKEN);

        //  IDカラムを指定
        int idIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        ArrayList<PhotoItem> result = new ArrayList<>(cursor.getCount());

        if (cursor.moveToLast()) {
            do {
                //  IDカラムからidを取ってくる
                Long imageID = cursor.getLong(idIndex);
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageID);
                Bitmap squareImage = MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(), imageID, MediaStore.Images.Thumbnails.MINI_KIND, null);

                //  MINI_KINDのUriを取ってくる
                //  ない場合を考慮して作っておく
                Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(), imageID, MediaStore.Images.Thumbnails.MINI_KIND, null);
                Cursor miniCursor = MediaStore.Images.Thumbnails.queryMiniThumbnail(context.getContentResolver(), imageID, MediaStore.Images.Thumbnails.MINI_KIND, null);
                if (miniCursor.moveToFirst()){
                    Uri miniUri = Uri.fromFile(new File(miniCursor.getString(miniCursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA))));
                    PhotoItem newItem = new PhotoItem(imageUri, miniUri, squareImage);
                    result.add(newItem);
                }
                count++;
            } while (cursor.moveToPrevious());
        }
        cursor.close();
        return result;
    }

    public static String getBucketId(String path) {
        return String.valueOf(path.toLowerCase().hashCode());
    }
}
