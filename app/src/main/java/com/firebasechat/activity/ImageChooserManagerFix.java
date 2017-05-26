package com.firebasechat.activity;



import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.kbeanie.imagechooser.api.BChooser;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ChosenImages;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.exceptions.ChooserException;
import com.kbeanie.imagechooser.threads.ImageProcessorListener;
import com.kbeanie.imagechooser.threads.ImageProcessorThread;

import java.util.ArrayList;


public class ImageChooserManagerFix extends BChooser implements ImageProcessorListener {
    private static final String TAG = com.kbeanie.imagechooser.api.ImageChooserManager.class.getSimpleName();
    private ImageChooserListener listener;
    private Uri cameraTempUri;

    public ImageChooserManagerFix(Activity activity, int type) {
        super(activity, type, true);
    }

    public ImageChooserManagerFix(Fragment fragment, int type) {
        super(fragment, type, true);
    }

    public ImageChooserManagerFix(android.app.Fragment fragment, int type) {
        super(fragment, type, true);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public ImageChooserManagerFix(Activity activity, int type, String folderName) {
        super(activity, type, folderName, true);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public ImageChooserManagerFix(Fragment fragment, int type, String folderName) {
        super(fragment, type, folderName, true);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public ImageChooserManagerFix(android.app.Fragment fragment, int type, String folderName) {
        super(fragment, type, folderName, true);
    }

    public ImageChooserManagerFix(Activity activity, int type, boolean shouldCreateThumbnails) {
        super(activity, type, shouldCreateThumbnails);
    }

    public ImageChooserManagerFix(Fragment fragment, int type, boolean shouldCreateThumbnails) {
        super(fragment, type, shouldCreateThumbnails);
    }

    public ImageChooserManagerFix(android.app.Fragment fragment, int type, boolean shouldCreateThumbnails) {
        super(fragment, type, shouldCreateThumbnails);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public ImageChooserManagerFix(Activity activity, int type, String foldername, boolean shouldCreateThumbnails) {
        super(activity, type, foldername, shouldCreateThumbnails);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public ImageChooserManagerFix(Fragment fragment, int type, String foldername, boolean shouldCreateThumbnails) {
        super(fragment, type, foldername, shouldCreateThumbnails);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public ImageChooserManagerFix(android.app.Fragment fragment, int type, String foldername, boolean shouldCreateThumbnails) {
        super(fragment, type, foldername, shouldCreateThumbnails);
    }

    public void setImageChooserListener(ImageChooserListener listener) {
        this.listener = listener;
    }

    public String choose() throws ChooserException {
        String path = null;
        if (this.listener == null) {
            throw new ChooserException("ImageChooserListener cannot be null. Forgot to set ImageChooserListener???");
        } else {
            switch (this.type) {
                case 291:
                    this.choosePicture();
                    break;
                case 294:
                    path = this.takePicture();
                    break;
                default:
                    throw new ChooserException("Cannot choose a video in ImageChooserManager");
            }

            return path;
        }
    }

    private void choosePicture() throws ChooserException {
        this.checkDirectory();

        try {
            Intent e = new Intent("android.intent.action.GET_CONTENT");
            e.setType("image/*");
            if (this.extras != null) {
                e.putExtras(this.extras);
            }

            e.addFlags(1);
            this.startActivity(e);
        } catch (ActivityNotFoundException var2) {
            throw new ChooserException(var2);
        }
    }

    private String takePicture() throws ChooserException {
        this.checkDirectory();

        try {
            Intent e = new Intent("android.media.action.IMAGE_CAPTURE");

            ContentValues values = new ContentValues(1);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");

            cameraTempUri = getContext().getContentResolver()
                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (cameraTempUri == null) {
                throw new ActivityNotFoundException();
            }

            this.filePathOriginal = cameraTempUri.toString();
            e.putExtra("output", cameraTempUri);
            if (this.extras != null) {
                e.putExtras(this.extras);
            }

            this.startActivity(e);
        } catch (ActivityNotFoundException var2) {
            throw new ChooserException(var2);
        }

        return this.filePathOriginal;
    }

    public void submit(int requestCode, Intent data) {
        try {
            if (requestCode != this.type) {
                this.onError("onActivityResult requestCode is different from the type the chooser was initialized with.");
            } else {
                switch (requestCode) {
                    case 291:
                    case 294:
                        this.processImageFromGallery(data);
                        break;
                }
            }
        } catch (Exception var4) {
            this.onError(var4.getMessage());
        }

    }

    @SuppressLint({"NewApi"})
    private void processImageFromGallery(Intent data) {
        if (cameraTempUri != null || (data != null && data.getDataString() != null)) {
            Uri uri = cameraTempUri != null ? cameraTempUri : data.getData();
            String var7 = uri.toString();
            this.sanitizeURI(var7);
            if (this.filePathOriginal != null && !TextUtils.isEmpty(this.filePathOriginal)) {
                String var10 = this.filePathOriginal;
                ImageProcessorThread var11 = new ImageProcessorThread(var10, this.foldername, this.shouldCreateThumbnails);
                var11.clearOldFiles(this.clearOldFiles);
                var11.setListener(this);
                var11.setContext(this.getContext());
                var11.start();
            } else {
                this.onError("File path was null");
            }
        } else if (data != null && data.getClipData() == null && !data.hasExtra("uris")) {
            this.onError("Image Uri was null!");
        } else if (data != null) {
            String[] filePaths;
            int count;
            if (data.hasExtra("uris")) {
                ArrayList var8 = data.getParcelableArrayListExtra("uris");
                filePaths = new String[var8.size()];

                for (count = 0; count < var8.size(); ++count) {
                    filePaths[count] = ((Uri) var8.get(count)).toString();
                }
            } else {
                ClipData thread = data.getClipData();
                count = thread.getItemCount();
                filePaths = new String[count];

                for (int i = 0; i < count; ++i) {
                    ClipData.Item item = thread.getItemAt(i);
                    //Log.i(TAG, "processImageFromGallery: Item: " + item.getUri());
                    filePaths[i] = item.getUri().toString();
                }
            }

            ImageProcessorThread var9 = new ImageProcessorThread(filePaths, this.foldername, this.shouldCreateThumbnails);
            var9.clearOldFiles(this.clearOldFiles);
            var9.setListener(this);
            var9.setContext(this.getContext());
            var9.start();
        }

    }

    public void onProcessedImage(ChosenImage image) {
        if (this.listener != null) {
            this.listener.onImageChosen(image);
        }

    }

    public void onError(String reason) {
        if (this.listener != null) {
            this.listener.onError(reason);
        }

    }

    public void onProcessedImages(ChosenImages images) {
        if (this.listener != null) {
            this.listener.onImagesChosen(images);
        }

    }
}

