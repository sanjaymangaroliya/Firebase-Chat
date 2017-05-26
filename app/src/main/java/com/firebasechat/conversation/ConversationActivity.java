package com.firebasechat.conversation;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebasechat.activity.ImageChooserManagerFix;
import com.firebasechat.activity.LoginActivity;
import com.firebasechat.controller.RecyclerItemClickListener;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.firebasechat.R;
import com.firebasechat.adapter.ConversationAdapter;
import com.firebasechat.controller.ConstantData;
import com.firebasechat.controller.Utils;
import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ChosenImages;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.kbeanie.imagechooser.api.IntentUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class ConversationActivity extends AppCompatActivity implements ImageChooserListener {

    private DatabaseReference databaseReference;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private RecyclerView recyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ImageView btEmoji;
    private EmojiconEditText edMessage;
    private View contentRoot;
    private EmojIconActions emojIcon;
    private HashMap<String, String> hashMap = null;
    private SharedPreferences preferences;
    private List<HashMap<String, String>> listOfConversation = new ArrayList<>();
    private ProgressDialog progressDialog;
    private String type = "0";
    private String imagePath = "";
    private int attachmentType = 1;
    private String strLatitude = "", strLongitude = "";
    private TextView tvName, tvNumber;
    private DisplayImageOptions options;
    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
    //Profile Picture
    private ImageView imgProfilePicture;
    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private ImageChooserManagerFix imageChooserManager;
    private String filePath;
    private int chooserType;
    private boolean isActivityResultOver = false;
    private String originalFilePath;
    private String thumbnailFilePath;
    private String thumbnailSmallFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_activity_main);

        //Toolbar
        FirebaseCrash.report(new Exception("ConversationActivity"));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tvName = (TextView) toolbar.findViewById(R.id.tvName);
        tvNumber = (TextView) toolbar.findViewById(R.id.tvNumber);
        imgProfilePicture = (ImageView) toolbar.findViewById(R.id.imgProfilePicture);

        preferences = getSharedPreferences(ConstantData.PREFERENCES, MODE_PRIVATE);
        databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl(ConstantData.URL);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(true);
        progressDialog.setMessage("Loading...");
        //
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.defaultuserwhite)
                .showImageForEmptyUri(R.drawable.defaultuserwhite)
                .showImageOnFail(R.drawable.defaultuserwhite)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();

        if (getIntent().getExtras() != null) {
            hashMap = (HashMap<String, String>) getIntent().getExtras().getSerializable("map");
            //Name
            String name = hashMap.get("name");
            String phone = hashMap.get("phone");
            String profile_picture = hashMap.get("profile_picture");
            tvName.setText(name);
            tvNumber.setText(phone);
            //PROFILE PICTURE
            ImageLoader.getInstance().displayImage(profile_picture, imgProfilePicture, options, animateFirstListener);
        }
        initUI();
        getConversation();
    }

    private void initUI() {
        contentRoot = findViewById(R.id.contentRoot);
        edMessage = (EmojiconEditText) findViewById(R.id.editTextMessage);
        btEmoji = (ImageView) findViewById(R.id.buttonEmoji);
        emojIcon = new EmojIconActions(this, contentRoot, edMessage, btEmoji);
        emojIcon.ShowEmojIcon();
        recyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Map<String, String> map = listOfConversation.get(position);
                        String type = map.get("type");
                        if (type.equals("0")) {
                            return;
                        } else if (type.equals("1")) {
                            onClickImage(position);
                        } else if (type.equals("2")) {
                            onClickMap(position);

                        }
                    }
                })
        );
    }

    ///////////////////////////////  GET CONVERSATION DATA ////////////////////////////////////
    private void getConversation() {
        if (Utils.isNetworkAvailable(this)) {
            progressDialog.show();
            databaseReference.child("conversation")
                    .addValueEventListener(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.getValue() != null) {
                                        listOfConversation.clear();
                                        for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                                            HashMap<String, String> map = new HashMap<>();
                                            map.put("sender", (String) messageSnapshot.child("sender").getValue());
                                            map.put("receiver", (String) messageSnapshot.child("receiver").getValue());
                                            map.put("message", (String) messageSnapshot.child("message").getValue());
                                            map.put("time", (String) messageSnapshot.child("time").getValue());
                                            map.put("type", (String) messageSnapshot.child("type").getValue());
                                            map.put("image", (String) messageSnapshot.child("image").getValue());
                                            map.put("lat", (String) messageSnapshot.child("lat").getValue());
                                            map.put("lng", (String) messageSnapshot.child("lng").getValue());
                                            //
                                            String loginUserEmail = preferences.getString("email", "");
                                            String selectUserEmail = hashMap.get("email");
                                            String sender = (String) messageSnapshot.child("sender").getValue();
                                            String receiver = (String) messageSnapshot.child("receiver").getValue();

                                            if (!Utils.isStringNull(loginUserEmail) && !Utils.isStringNull(sender) &&
                                                    !Utils.isStringNull(selectUserEmail) && !Utils.isStringNull(receiver)) {

                                                if (loginUserEmail.equals(sender) && selectUserEmail.equals(receiver)) {
                                                    listOfConversation.add(map);
                                                }
                                                if (loginUserEmail.equals(receiver) && selectUserEmail.equals(sender)) {
                                                    listOfConversation.add(map);
                                                }
                                            }
                                        }
                                        setData();
                                    } else

                                    {
                                        if (progressDialog != null && progressDialog.isShowing()) {
                                            progressDialog.dismiss();
                                        }
                                        //Utils.showToast(ConversationActivity.this, "Conversation not found");
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    if (progressDialog != null && progressDialog.isShowing()) {
                                        progressDialog.dismiss();
                                    }
                                    // Utils.showToast(ConversationActivity.this, "Conversation not found");
                                }
                            }

                    );
        } else {
            Utils.showToast(this, getResources().getString(R.string.internet_error));
        }
    }

    public void setData() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        if (listOfConversation.size() != 0) {
            ConversationAdapter adapter = new ConversationAdapter(this, listOfConversation);
            recyclerView.setLayoutManager(mLinearLayoutManager);
            recyclerView.setAdapter(adapter);
        } else {
            Utils.showToast(ConversationActivity.this, "Conversation not found");
        }
    }

    public void onClickSendMessage(View view) {
        sendMessage();
    }

    ///////////////////////////////  SEND MESSAGE ////////////////////////////////////

    public void sendMessage() {
        String message = edMessage.getText().toString().trim();
        if (type.equals("0") && message.length() == 0) {
            Utils.showToast(this, "Enter message");
            return;
        }
        HashMap<String, String> map = new HashMap<>();
        map.put("sender", preferences.getString("email", ""));
        map.put("receiver", hashMap.get("email"));
        map.put("message", message);
        map.put("time", Calendar.getInstance().getTime().getTime() + "");
        map.put("type", type);
        map.put("image", imagePath);
        map.put("lat", strLatitude);
        map.put("lng", strLongitude);
        databaseReference.child("conversation").push().setValue(map);
        String key = preferences.getString("key", "");
        databaseReference.child("user").child(key).child("unread").setValue("1");
        edMessage.setText("");
        type = "0";

    }

    ///////////////////////////  ATTACHMENT /////////////////////////////////////////////
    public void onClickAttach(View view) {
        attchment();
    }

    private void attchment() {
        final CharSequence[] options = {"Take Photo", "Choose Photo from Gallery", "Send Location", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Attachment");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    attachmentType = 1;
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (checkAndRequestPermissions()) {
                            takePicture();
                        }
                    } else {
                        takePicture();
                    }
                } else if (options[item].equals("Choose Photo from Gallery")) {
                    attachmentType = 2;
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (checkAndRequestPermissions()) {
                            chooseImage();
                        }
                    } else {
                        chooseImage();
                    }
                } else if (options[item].equals("Send Location")) {
                    attachmentType = 3;
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (checkAndRequestPermissions()) {
                            sendLocation();
                        }
                    } else {
                        sendLocation();
                    }
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.create().show();
    }

    //////////////////////////////////////////  CALL //////////////////////////////////
    public void onClickCall(View view) {
        attachmentType = 4;
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkAndRequestPermissions()) {
                callDial();
            }
        } else {
            callDial();
        }
    }

    public void callDial() {
        String phone = hashMap.get("phone");
        if (!Utils.isStringNull(phone)) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phone));
            startActivity(intent);
        } else {
            Utils.showToast(this, "Contact number not found");
        }
    }

    //////////////////////////////////////////  SEND LOCATION //////////////////////////////////
    private void sendLocation() {
        try {
            PlacePicker.IntentBuilder intentBuilder =
                    new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(ConversationActivity.this);
            startActivityForResult(intent, ConstantData.SELECT_LOCATION_PLACE);
        } catch (GooglePlayServicesRepairableException
                | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    ///////////////////////////// TAKE PICTURE FROM CAMERA OR GALLERY  /////////////////////////////////
    private void takePicture() {
        chooserType = ChooserType.REQUEST_CAPTURE_PICTURE;
        imageChooserManager = new ImageChooserManagerFix(this,
                ChooserType.REQUEST_CAPTURE_PICTURE, true);
        imageChooserManager.setImageChooserListener(this);
        try {
            filePath = imageChooserManager.choose();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void chooseImage() {
        chooserType = ChooserType.REQUEST_PICK_PICTURE;
        imageChooserManager = new ImageChooserManagerFix(this,
                ChooserType.REQUEST_PICK_PICTURE, true);
        Bundle bundle = new Bundle();
        bundle.putBoolean(Intent.EXTRA_ALLOW_MULTIPLE, true);
        imageChooserManager.setExtras(bundle);
        imageChooserManager.setImageChooserListener(this);
        imageChooserManager.clearOldFiles();
        try {
            filePath = imageChooserManager.choose();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onImageChosen(final ChosenImage image) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isActivityResultOver = true;
                originalFilePath = image.getFilePathOriginal();
                thumbnailFilePath = image.getFileThumbnail();
                thumbnailSmallFilePath = image.getFileThumbnailSmall();
                if (image != null) {
                    loadImage(image.getFileThumbnail());
                    //loadImage(imageViewThumbSmall, image.getFileThumbnailSmall());
                } else {
                }
            }
        });
    }

    @Override
    public void onImagesChosen(final ChosenImages images) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onImageChosen(images.getImage(0));
            }
        });
    }

    private void loadImage(final String path) {
        sendSelectPicture(path);
    }

    @Override
    public void onError(final String reason) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ConversationActivity.this, reason,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void reinitializeImageChooser() {
        imageChooserManager = new ImageChooserManagerFix(this, chooserType, true);
        Bundle bundle = new Bundle();
        bundle.putBoolean(Intent.EXTRA_ALLOW_MULTIPLE, true);
        imageChooserManager.setExtras(bundle);
        imageChooserManager.setImageChooserListener(this);
        imageChooserManager.reinitialize(filePath);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("activity_result_over", isActivityResultOver);
        outState.putInt("chooser_type", chooserType);
        outState.putString("media_path", filePath);
        outState.putString("orig", originalFilePath);
        outState.putString("thumb", thumbnailFilePath);
        outState.putString("thumbs", thumbnailSmallFilePath);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("chooser_type")) {
                chooserType = savedInstanceState.getInt("chooser_type");
            }
            if (savedInstanceState.containsKey("media_path")) {
                filePath = savedInstanceState.getString("media_path");
            }
            if (savedInstanceState.containsKey("activity_result_over")) {
                isActivityResultOver = savedInstanceState.getBoolean("activity_result_over");
                originalFilePath = savedInstanceState.getString("orig");
                thumbnailFilePath = savedInstanceState.getString("thumb");
                thumbnailSmallFilePath = savedInstanceState.getString("thumbs");
            }
        }
        if (isActivityResultOver) {
            populateData();
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void populateData() {
        loadImage(thumbnailFilePath);
        // loadImage(imageViewThumbSmall, thumbnailSmallFilePath);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void checkForSharedImage(Intent intent) {
        if (intent != null) {
            if (intent.getAction() != null && intent.getType() != null && intent.getExtras() != null) {
                ImageChooserManager m = new ImageChooserManager(this, ChooserType.REQUEST_PICK_PICTURE);
                m.setImageChooserListener(this);

                m.submit(ChooserType.REQUEST_PICK_PICTURE, IntentUtils.getIntentForMultipleSelection(intent));
            }
        }
    }

    public void sendSelectPicture(String path) {
        Uri uri = Uri.fromFile(new File(path));
        StorageReference storageRef = storage.getReferenceFromUrl
                (ConstantData.URL_STORAGE).child(ConstantData.FOLDER_CHAT_IMAGE);
        if (storageRef != null) {
            final String name = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString();
            StorageReference storageReference2 = storageRef.child(name);
            UploadTask uploadTask = storageReference2.putFile(uri);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    imagePath = downloadUrl.toString();
                    strLatitude = "";
                    strLongitude = "";
                    type = "1";
                    sendMessage();

                }
            });
        }
    }

    ///////////////////////////////// ON ACTIVITY RESULT //////////////////////////////
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ChooserType.REQUEST_PICK_PICTURE:
                if (imageChooserManager == null) {
                    reinitializeImageChooser();
                }
                imageChooserManager.submit(requestCode, data);
                break;
            case ChooserType.REQUEST_CAPTURE_PICTURE:
                if (imageChooserManager == null) {
                    reinitializeImageChooser();
                }
                imageChooserManager.submit(requestCode, data);
                break;
            case ConstantData.SELECT_LOCATION_PLACE:
                if (resultCode == RESULT_OK) {
                    Place place = PlacePicker.getPlace(this, data);
                    if (place != null) {
                        LatLng latLng = place.getLatLng();
                        imagePath = "";
                        strLatitude = String.valueOf(latLng.latitude);
                        strLongitude = String.valueOf(latLng.longitude);
                        type = "2";
                        sendMessage();
                    }
                }
                break;
            default:
                break;
        }
    }

    public void sendImageChat(Uri uri) {
        StorageReference storageRef = storage.getReferenceFromUrl
                (ConstantData.URL_STORAGE).child(ConstantData.FOLDER_CHAT_IMAGE);
        if (storageRef != null) {
            final String name = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString();
            StorageReference storageReference2 = storageRef.child(name);
            UploadTask uploadTask = storageReference2.putFile(uri);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Utils.showToast(ConversationActivity.this, "Failure profile picture");
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    imagePath = downloadUrl.toString();
                    strLatitude = "";
                    strLongitude = "";
                    type = "1";
                    sendMessage();
                }
            });
        } else {
            Utils.showToast(ConversationActivity.this, "Storage reference error");
        }
    }


    //////////////////////////////  PERMISSION ///////////////////////////////////////
    private boolean checkAndRequestPermissions() {
        int permissionWriteExternalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int permissionCall = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);

        List<String> listPermissionsNeeded = new ArrayList<>();

        if (permissionWriteExternalStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (permissionCamera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }

        if (permissionCall != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CALL_PHONE);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<>();
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.CALL_PHONE, PackageManager.PERMISSION_GRANTED);
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                        if (attachmentType == 1) {
                            takePicture();
                        } else if (attachmentType == 2) {
                            chooseImage();
                        } else if (attachmentType == 3) {
                            sendLocation();
                        } else if (attachmentType == 4) {
                            callDial();
                        }
                    } else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this,
                                Manifest.permission.ACCESS_FINE_LOCATION)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this,
                                Manifest.permission.CAMERA)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this,
                                Manifest.permission.CALL_PHONE)) {
                            showDialogOK("Permission required for this app",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    break;
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                }
            }
        }

    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_conversation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                this.finish();
                break;
            case R.id.action_mute:
                Utils.showToast(ConversationActivity.this, "action_mute");
                break;
            case R.id.action_block:
                Utils.showToast(ConversationActivity.this, "action_block");
                break;
            case R.id.action_clear_chat:
                Utils.showToast(ConversationActivity.this, "action_clear_chat");
                break;
            case R.id.action_logout:
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear();
                editor.commit();
                startActivity(new Intent(this, LoginActivity.class));
                ConversationActivity.this.finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {
        static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    displayedImages.add(imageUri);
                }
            }
        }
    }

    public void onClickImage(int position) {
        String image = listOfConversation.get(position).get("image");
        if (!Utils.isStringNull(image)) {
            Intent intent = new Intent(this, FullScreenImageActivity.class);
            intent.putExtra("image", image);
            startActivity(intent);
        } else {
            Utils.showToast(this, "Media path not found");
        }
    }

    public void onClickMap(int position) {
       /* String name = tvName.getText().toString().trim();
        String lat = listOfConversation.get(position).get("lat");
        String lng = listOfConversation.get(position).get("lng");
        if (!Utils.isStringNull(lat) && !Utils.isStringNull(lng)) {
            Intent intent = new Intent(this, DrawPathActivity.class);
            intent.putExtra("name", name);
            intent.putExtra("lat", lat);
            intent.putExtra("lng", lng);
            startActivity(intent);
        } else {
            Utils.showToast(this, "Location not found");
        }*/
    }
}
