package com.example.makoto.camerarolltest2;

import android.Manifest;
import android.app.ActionBar;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.picasso.transformations.BlurTransformation;
import uk.co.senab.photoview.PhotoViewAttacher;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener,
        android.support.v4.app.LoaderManager.LoaderCallbacks<List<PhotoItem>> {

    private EditText et;
    private TextView inputSizeView;
    private LinearLayout ll;
    private ImageView iv;
    private Bitmap bmp;
    private InputStream is;
    private LinearLayout photoLayout;
    private Bitmap bm;
    private List<PhotoItem> mPhotoListItem;
    private Uri capturedImageUri;
    private int activatedPhotoNum = -1;
    private List<Button> sendButtons;
    private List<View.OnClickListener> photoClickListeners;
    private List<View.OnClickListener> sendButtonClickListeners;
    private List<ImageView> photoViews;
    private List<ImageView> blurViews;
    private List<ShapeDrawable> buttonDrawables;
    private List<RelativeLayout> photoFrames;
    private int mode = -1;
    private final int TEXT_MODE = 1;
    private final int CAMERA_MODE = 2;
    private final int FILE_MODE = 3;
    private float density;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et = (EditText) findViewById(R.id.edit_text);
        et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });
        ll = (LinearLayout) findViewById(R.id.blank);
        iv = (ImageView) findViewById(R.id.picture);
        PhotoViewAttacher pva = new PhotoViewAttacher(iv);

        DisplayMetrics metrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        density = metrics.density;

        bm = BitmapFactory.decodeResource(getResources(), R.drawable.image);
        photoLayout = (LinearLayout) findViewById(R.id.photoLayout);
        inputSizeView = (TextView) findViewById(R.id.inputSize);
        photoFrames = new ArrayList<>();
        sendButtons = new ArrayList<>();
        photoClickListeners = new ArrayList<>();
        sendButtonClickListeners = new ArrayList<>();
        photoViews = new ArrayList<>();
        blurViews = new ArrayList<>();
        buttonDrawables = new ArrayList<>();
        mPhotoListItem = new ArrayList<>();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.READ_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( this, new String[] {  Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int size = 20 - s.length();
                inputSizeView.setText(""+size);
                if (size <  0) inputSizeView.setTextColor(Color.RED);
                if (size >= 0) inputSizeView.setTextColor(Color.WHITE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        Intent it = new Intent();
        switch (v.getId()) {

            case R.id.cameraModeButton:
                //  ファイルモードを初期化
                initCameraRoll();
                //  写真を撮るインテント
                it.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                //  撮ったら保存する
                File path;
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                } else {
                    path = Environment.getDataDirectory();
                    Toast.makeText(this, "SDカードがありません", Toast.LENGTH_LONG).show();
                }
                String filename = System.currentTimeMillis() + ".jpg";
                File capturedFile = new File(path, filename);
                capturedImageUri = Uri.fromFile(capturedFile);
                it.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
                startActivityForResult(it, 2);
                break;

            case R.id.fromFilesModeButton:
                et.setVisibility(View.GONE);

                if (mode == FILE_MODE) {
                    break;
                }

                mode = FILE_MODE;
                initCameraRoll();

                break;

            case R.id.textModeButton:
                //  ファイルモードを初期化
                initCameraRoll();

                et.setVisibility(View.VISIBLE);
                et.requestFocus();

                mode = TEXT_MODE;
                break;

            case R.id.blank:
                et.setVisibility(View.VISIBLE);
                initCameraRoll();
                ll.requestFocus();

                mode = -1;
                break;

            case R.id.sendButton:
                if (et.getText().length() <= 0){
                    Toast.makeText(this, "メッセージを入力してください", Toast.LENGTH_SHORT).show();
                    break;
                }

                if (et.getText().length() > 20){
                    Toast.makeText(this, "メッセージが長すぎます！", Toast.LENGTH_SHORT).show();
                    break;
                }

                Toast.makeText(this, et.getText().toString(),Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        ll.requestFocus();
        return super.onTouchEvent(event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent it) {
        super.onActivityResult(requestCode, resultCode, it);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Uri u = it.getData();
            try {
                is = getContentResolver().openInputStream(u);
                bmp = BitmapFactory.decodeStream(is);
                iv.setImageBitmap(bmp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (requestCode == 2 && resultCode == RESULT_OK) {
            Bitmap photo = null;
            try {
                photo = MediaStore.Images.Media.getBitmap(getContentResolver(), capturedImageUri);
                MediaScannerConnection.scanFile(
                        this,
                        new String[]{capturedImageUri.getPath()},
                        null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(String path, Uri uri) {

                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
            iv.setImageBitmap(photo);
        }
    }

    @Override
    public Loader<List<PhotoItem>> onCreateLoader(int id, Bundle args) {
        Log.d("onCreateLoader", "に入った");
        return new PhotoGalleryAsyncLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<List<PhotoItem>> loader, List<PhotoItem> data) {
        mPhotoListItem.clear(); //  delete all components
        Log.d("onLoaderFinished", "に入った");

        for (int i = 0; i < data.size(); i++) {
            PhotoItem item = data.get(i);
            mPhotoListItem.add(item);
        }

        for (int i = 0; i < mPhotoListItem.size(); i++) {
            final int photoNum = i;

            //  buttons
            Button button = new Button(getApplicationContext());
            button.setText("Send");
            button.setTextColor(Color.WHITE);
            button.setBackgroundDrawable(null);
            button.setVisibility(View.GONE);    //  buttons are invisible at first
            sendButtons.add(button);

            //  images
            ImageView photoView = new ImageView(getApplicationContext());
            photoViews.add(photoView);

            //  blurred images
            final ImageView blurView = new ImageView(getApplicationContext());
            blurViews.add(blurView);

            //  images' OnClickListener
            View.OnClickListener plistener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (activatedPhotoNum != photoNum) {
                        //  create button, if not already exist
                        if (sendButtons.get(photoNum).getBackground() == null) {
                            ShapeDrawable drawable = new ShapeDrawable(new OvalShape());
                            drawable.setIntrinsicWidth(300);
                            drawable.setIntrinsicHeight(300);
                            drawable.getPaint().setColor(Color.GRAY);
                            drawable.getPaint().setStyle(Paint.Style.FILL);
                            drawable.getPaint().setAntiAlias(true);
                            drawable.getPaint().setAlpha(120);
                            sendButtons.get(photoNum).setBackgroundDrawable(drawable);
                            sendButtons.get(photoNum).setVisibility(View.VISIBLE);
                        } else {
                            sendButtons.get(photoNum).setVisibility(View.VISIBLE);
                        }

                        //  just for debug
                        try {
                            iv.setImageBitmap(BitmapFactory.decodeStream(getContentResolver().openInputStream(mPhotoListItem.get(photoNum).getMiniUri())));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        Log.d("rawUrlは", "" + mPhotoListItem.get(photoNum).getRawUri().toString());
                        Log.d("miniUrlは", "" + mPhotoListItem.get(photoNum).getMiniUri().toString());

                        //  create blurred images, if not already exist
                        if (blurViews.get(photoNum).getDrawable() == null) {
                            Picasso.with(getApplicationContext())
                                    .load(mPhotoListItem.get(photoNum).getMiniUri())
                                    .fit()
                                    .centerCrop()
                                    .transform(new BlurTransformation(getApplicationContext())) //  Blur
                                    .into(blurViews.get(photoNum));
                        } else {
                            blurViews.get(photoNum).setVisibility(View.VISIBLE);
                        }

                        //  inactivate
                        if (activatedPhotoNum != -1) {
                            blurViews.get(activatedPhotoNum).setVisibility(View.GONE);
                            sendButtons.get(activatedPhotoNum).setVisibility(View.GONE);
                        }

                        activatedPhotoNum = photoNum;
                    } else if (activatedPhotoNum == photoNum) {
                        sendButtons.get(photoNum).setVisibility(View.GONE); //  hide buttons
                        blurViews.get(photoNum).setVisibility(View.GONE);   //  hide blurred images
                        activatedPhotoNum = -1;
                        Log.d("isActivated=true", "にはいった");
                    }
                }
            };
            photoClickListeners.add(plistener);

            // SendボタンのOnClickListener
            View.OnClickListener slistener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        iv.setImageBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), mPhotoListItem.get(photoNum).getRawUri()));
                        v.setVisibility(View.GONE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    blurViews.get(activatedPhotoNum).setVisibility(View.GONE);
                    sendButtons.get(activatedPhotoNum).setVisibility(View.GONE);
                    activatedPhotoNum = -1;
                }
            };
            sendButtonClickListeners.add(slistener);

            //  写真を入れるRelativeLayout
            RelativeLayout photoFrame = new RelativeLayout(this);
            photoFrames.add(photoFrame);
            //  にルールをセット
            RelativeLayout.LayoutParams rparams = new RelativeLayout.LayoutParams(pixelToDp(200), ViewGroup.LayoutParams.MATCH_PARENT);
            photoFrames.get(i).setPadding(0, 0, 2, 0);
            photoLayout.addView(photoFrames.get(i), rparams);
            //  に写真をセット
            photoView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            photoFrames.get(i).addView(photoView);
            Picasso.with(getApplicationContext()).load(mPhotoListItem.get(i).getMiniUri())
                    .fit()
                    .centerCrop()
                    .into(photoView);
            //  にブラー写真をセット
            blurView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            photoFrames.get(i).addView(blurView);
            //  にボタンをセット
            RelativeLayout.LayoutParams bparams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            //bparams.setMargins(5, 5, 5, 5);
            bparams.addRule(RelativeLayout.CENTER_IN_PARENT);
            photoFrames.get(i).addView(button, bparams);
            //  とOnClickListenerもセット
            photoFrames.get(i).setOnClickListener(photoClickListeners.get(photoNum));
            //  ボタンにOnClickListenerをセット
            sendButtons.get(photoNum).setOnClickListener(sendButtonClickListeners.get(photoNum));
        }

    }

    @Override
    public void onLoaderReset(Loader<List<PhotoItem>> loader) {
        Log.d("onLoaderReset", "に入った");
        mPhotoListItem.clear();
    }

    private void initCameraRoll() {
        activatedPhotoNum = -1;
        photoLayout.removeAllViews();
        photoFrames.clear();
        sendButtons.clear();
        photoClickListeners.clear();
        sendButtonClickListeners.clear();
        photoViews.clear();
        blurViews.clear();
        buttonDrawables.clear();
        mPhotoListItem.clear();

        System.gc();

        getSupportLoaderManager().initLoader(0, null, this);
    }

    private int pixelToDp(int pixel) {
        int dp;
        dp = (int) (pixel * density);
        return dp;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
