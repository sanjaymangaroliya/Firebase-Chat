package com.firebasechat.conversation;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.firebasechat.R;
import com.google.firebase.crash.FirebaseCrash;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;


public class FullScreenImageActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private ZoomableImageView imageView;
    private String strImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fullscreen_image);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("MEDIA");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        FirebaseCrash.report(new Exception("FullScreenImageActivity"));
        strImage = getIntent().getStringExtra("image");
        initUI();

    }

    private void initUI() {
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        imageView = (ZoomableImageView) findViewById(R.id.imageView);

        ImageLoader.getInstance().displayImage(strImage, imageView, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                FullScreenImageActivity.this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
