package com.example.makoto.camerarolltest2;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.util.List;

/**
 * Created by Makoto on 2016/03/30.
 */
public class PhotoGalleryAsyncLoader extends AsyncTaskLoader<List<PhotoItem>> {
    private List<PhotoItem> mPhotoListItems;

    public PhotoGalleryAsyncLoader(Context context) {
        super(context);
    }


    public static String getBucketId(String path) {
        return String.valueOf(path.toLowerCase().hashCode());
    }

    @Override
    public List<PhotoItem> loadInBackground() {
        final Context context = getContext();
        List<PhotoItem> photos = PhotoGalleryImageProvider.getAlbumThumbnails(context);
        return photos;
    }

    @Override
    public void deliverResult(List<PhotoItem> newPhotoListItems) {
        if (isReset()) {
            if (newPhotoListItems != null) {
                onReleaseResources(newPhotoListItems);
            }
        }
        List<PhotoItem> oldPhotos = mPhotoListItems;
        mPhotoListItems = newPhotoListItems;

        if (isStarted()) {
            super.deliverResult(newPhotoListItems);
        }

        if (oldPhotos != null) {
            onReleaseResources(oldPhotos);
        }
    }

    @Override
    protected void onStartLoading() {

        if (mPhotoListItems != null) {
            deliverResult(mPhotoListItems);
        } else {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    public void onCanceled(List<PhotoItem> photoListItems) {
        super.onCanceled(photoListItems);

        onReleaseResources(photoListItems);
    }

    @Override
    protected void onReset() {
        super.onReset();

        onStopLoading();

        if (mPhotoListItems != null) {
            onReleaseResources(mPhotoListItems);
            mPhotoListItems = null;
        }
    }

    protected void onReleaseResources(List<PhotoItem> photoListItems) {

    }
}
