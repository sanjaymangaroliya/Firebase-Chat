package com.firebasechat.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import com.firebasechat.controller.ConstantData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.firebasechat.R;
import com.firebasechat.controller.Utils;
import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ChosenImages;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.kbeanie.imagechooser.api.IntentUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, ImageChooserListener {


    private SharedPreferences preferences;
    private EditText etFullName, etEmail, etPassword, etMobileNumber, etBirthDate;
    private RadioButton rbMale, rbFemale;
    private CoordinatorLayout coordinatorLayout;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private String strFullName, strEmail, strPassword, strMobileNumber, strBirthDate, strGender = "0";
    private ProgressDialog progressDialog;
    //Date Picker
    private Calendar calendar;
    private DatePickerDialog datePickerDialog;
    private int Year, Month, Day;
    //Profile Picture
    private String finalImagePath = "";
    private ProgressBar progressBar;
    private ImageView imgProfilePicture;
    private int selectType = 1;
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
        setContentView(R.layout.register);

        preferences = getSharedPreferences(ConstantData.PREFERENCES, MODE_PRIVATE);
        FirebaseCrash.report(new Exception("RegisterActivity"));
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl(ConstantData.URL);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        //
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(true);
        progressDialog.setMessage("Loading...");
        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Register");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initUI();
    }

    private void initUI() {
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        imgProfilePicture = (ImageView) findViewById(R.id.imgProfilePicture);
        etFullName = (EditText) findViewById(R.id.etFullName);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etMobileNumber = (EditText) findViewById(R.id.etMobileNumber);
        etBirthDate = (EditText) findViewById(R.id.etBirthDate);
        rbMale = (RadioButton) findViewById(R.id.rbMale);
        rbFemale = (RadioButton) findViewById(R.id.rbFemale);
        //
        etBirthDate.setFocusable(false);

        rbMale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rbMale.setChecked(true);
                rbFemale.setChecked(false);
                strGender = "0";
            }
        });
        rbFemale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rbMale.setChecked(false);
                rbFemale.setChecked(true);
                strGender = "1";
            }
        });
    }

    ////////////////////////////////////////  REGISTER //////////////////////////////////////////////
    public void onClickRegister(View view) {
        strFullName = etFullName.getText().toString().trim();
        strEmail = etEmail.getText().toString().trim();
        strPassword = etPassword.getText().toString().trim();
        strMobileNumber = etMobileNumber.getText().toString().trim();
        strBirthDate = etBirthDate.getText().toString().trim();
        if (strFullName.length() == 0) {
            Utils.showSnackbar(coordinatorLayout, "Required full name!");
        } else if (strEmail.length() == 0) {
            Utils.showSnackbar(coordinatorLayout, "Required email!");
        } else if (!Utils.isValidEmail(strEmail)) {
            Utils.showSnackbar(coordinatorLayout, "Required valid email!");
        } else if (strPassword.length() == 0) {
            Utils.showSnackbar(coordinatorLayout, "Required password!");
        } else if (strPassword.length() < 6) {
            Utils.showSnackbar(coordinatorLayout, "Required password minimum 6 characters!");
        } else if (strMobileNumber.length() == 0) {
            Utils.showSnackbar(coordinatorLayout, "Required mobile number!");
        } else if (strMobileNumber.length() < 10) {
            Utils.showSnackbar(coordinatorLayout, "Required valid mobile number!");
        } else if (strBirthDate.length() == 0) {
            Utils.showSnackbar(coordinatorLayout, "Select birth date!");
        } else {
            Utils.hideKeyboard(this);
            if (Utils.isNetworkAvailable(this)) {
                progressDialog.show();
                auth.createUserWithEmailAndPassword(strEmail, strPassword)
                        .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (progressDialog != null && progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                if (task.isSuccessful()) {
                                    HashMap<String, String> map = new HashMap<>();
                                    map.put("dob", strBirthDate);
                                    map.put("email", strEmail);
                                    map.put("gender", strGender);
                                    map.put("name", strFullName);
                                    map.put("phone", strMobileNumber);
                                    map.put("profile_picture", finalImagePath);
                                    map.put("status", "0");
                                    databaseReference.child("user").push().setValue(map);
                                    sendProfilePicture(finalImagePath);
                                    //
                                    Utils.showToast(RegisterActivity.this, "Registration successfully");
                                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                    RegisterActivity.this.finish();

                                } else {
                                    Utils.showSnackbar(coordinatorLayout, "Registration failed. maybe email already exist");
                                }
                            }
                        });
            } else {
                Utils.showSnackbar(coordinatorLayout, getResources().getString(R.string.internet_error));

            }
        }
    }

    //////////////////////////////////  DATE PICKER /////////////////////////////////////
    public void onClickBirthDate(View view) {
        calendar = Calendar.getInstance();
        Year = calendar.get(Calendar.YEAR);
        Month = calendar.get(Calendar.MONTH);
        Day = calendar.get(Calendar.DAY_OF_MONTH);

        datePickerDialog = DatePickerDialog.newInstance(RegisterActivity.this, Year, Month, Day);
        datePickerDialog.setThemeDark(false);
        datePickerDialog.showYearPickerFirst(false);
        datePickerDialog.setAccentColor(Color.parseColor("#1a75bb"));
        datePickerDialog.setTitle("SELECT YOUR BIRTH DATE");
        datePickerDialog.show(getFragmentManager(), "DatePickerDialog");
    }

    @Override
    public void onDateSet(DatePickerDialog view, int Year, int Month, int Day) {
        int month = Month + 1;
        String date = "" + String.format("%02d", Day) + "-" + String.format("%02d", month) + "-" + Year;
        etBirthDate.setText(date);
    }

    ///////////////////////////// TAKE PICTURE FROM CAMERA OR GALLERY  /////////////////////////////////

    public void onClickProfilePicture(View view) {
        if (Utils.isNetworkAvailable(this)) {
            selectProfilePicture();
        } else {
            Utils.showSnackbar(coordinatorLayout, getResources().getString(R.string.internet_error));
        }
    }

    private void selectProfilePicture() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    selectType = 1;
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (checkAndRequestPermissions()) {
                            takePicture();
                        }
                    } else {
                        takePicture();
                    }
                } else if (options[item].equals("Choose from Gallery")) {
                    selectType = 2;
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (checkAndRequestPermissions()) {
                            chooseImage();
                        }
                    } else {
                        chooseImage();
                    }
                } else if (options[item].equals("Cancel")) {
                }
            }
        });
        builder.create().show();
    }

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && (requestCode == ChooserType.REQUEST_PICK_PICTURE
                || requestCode == ChooserType.REQUEST_CAPTURE_PICTURE)) {
            if (imageChooserManager == null) {
                reinitializeImageChooser();
            }
            imageChooserManager.submit(requestCode, data);
        } else {
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
                    loadImage(imgProfilePicture, image.getFileThumbnail());
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

    private void loadImage(ImageView iv, final String path) {
        sendProfilePicture(path);
        Picasso.with(this)
                .load(Uri.fromFile(new File(path)))
                .fit()
                .centerInside()
                .into(iv, new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError() {
                    }
                });
    }

    @Override
    public void onError(final String reason) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(RegisterActivity.this, reason,
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
        loadImage(imgProfilePicture, thumbnailFilePath);
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

    public void sendProfilePicture(String path) {
        Uri uri = Uri.fromFile(new File(path));
        progressBar.setVisibility(View.VISIBLE);
        StorageReference storageRef = storage.getReferenceFromUrl
                (ConstantData.URL_STORAGE).child(ConstantData.FOLDER_PROFILE_PICTURE);
        if (storageRef != null) {
            final String name = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString();
            StorageReference storageReference2 = storageRef.child(name);
            UploadTask uploadTask = storageReference2.putFile(uri);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.GONE);
                    Utils.showSnackbar(coordinatorLayout, "Failure profile picture");
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressBar.setVisibility(View.GONE);
                    Utils.showSnackbar(coordinatorLayout, "Success profile picture");
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    finalImagePath = downloadUrl.toString();
                }
            });
        } else {
            Utils.showSnackbar(coordinatorLayout, "Storage reference error");
        }
    }

    ///////---------------------- Permissions ---------------------///////

    private boolean checkAndRequestPermissions() {

        int permissionCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int permissionWriteExternalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        List<String> listPermissionsNeeded = new ArrayList<>();

        if (permissionCamera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }

        if (permissionWriteExternalStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
                    ConstantData.REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case ConstantData.REQUEST_ID_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<>();
                perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    if (perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        if (selectType == 1) {
                            takePicture();
                        } else {
                            chooseImage();
                        }
                    } else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                Manifest.permission.CAMERA)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                RegisterActivity.this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
