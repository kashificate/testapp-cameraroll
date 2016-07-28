package com.example.makoto.camerarolltest2;

import android.graphics.Bitmap;
import android.net.Uri;

/**
 * Created by Makoto on 2016/03/30.
 */
public class PhotoItem {

    private Uri rawUri;
    private Uri miniUri;
    private Bitmap squareImage;

    public PhotoItem(Uri thumbnailUri, Uri miniUri, Bitmap squareImage) {
        this.rawUri = thumbnailUri;
        this.miniUri = miniUri;
        this.squareImage = squareImage;
    }


    public Uri getRawUri() {
        return rawUri;
    }

    public void setRawUri(Uri rawUri) {
        this.rawUri = rawUri;
    }

    public Uri getMiniUri() {
        return miniUri;
    }

    public void setMiniUri(Uri miniUri) {
        this.miniUri = miniUri;
    }

    public Bitmap getSquareImage() {
        return squareImage;
    }

    public void setSquareImage(Bitmap squareImage) {
        this.squareImage = squareImage;
    }

}
