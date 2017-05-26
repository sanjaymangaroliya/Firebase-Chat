package com.firebasechat.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.firebasechat.controller.ConstantData;
import com.firebasechat.controller.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.firebasechat.R;

public class LoginActivity extends AppCompatActivity {

    //Global variable
    private EditText etEmail, etPassword;
    private SharedPreferences preferences;
    private CoordinatorLayout coordinatorLayout;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;
    private Boolean isBoolean = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        preferences = getSharedPreferences(ConstantData.PREFERENCES, MODE_PRIVATE);
        FirebaseCrash.report(new Exception("LoginActivity"));
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl(ConstantData.URL);
        //
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(true);
        progressDialog.setMessage("Loading...");
        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Login");
        initUI();
    }

    private void initUI() {
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
    }

    public void onClickRegister(View view) {
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        LoginActivity.this.finish();
    }

    public void onClickForgotPassword(View view) {
        startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        LoginActivity.this.finish();
    }

    public void onClickLogin(View view) {
        String strEmail = etEmail.getText().toString().trim();
        String strPassword = etPassword.getText().toString().trim();
        if (strEmail.length() == 0) {
            Utils.showSnackbar(coordinatorLayout, "Required email!");
        } else if (!Utils.isValidEmail(strEmail)) {
            Utils.showSnackbar(coordinatorLayout, "Required valid email!");
        } else if (strPassword.length() == 0) {
            Utils.showSnackbar(coordinatorLayout, "Required password!");
        } else {
            Utils.hideKeyboard(this);
            if (Utils.isNetworkAvailable(this)) {
                progressDialog.show();
                auth.signInWithEmailAndPassword(strEmail, strPassword)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                    final String email = firebaseUser.getEmail();
                                    if (!Utils.isStringNull(email)) {
                                        isBoolean = true;
                                        checkUser(email);
                                    } else {
                                        dismissDialog();
                                        Utils.showSnackbar(coordinatorLayout, "User not fount");
                                    }
                                } else {
                                    dismissDialog();
                                    Utils.showSnackbar(coordinatorLayout, "Login failed. maybe invalid email or password");
                                }
                            }
                        });
            } else {
                Utils.showSnackbar(coordinatorLayout, getResources().getString(R.string.internet_error));
            }
        }
    }

    public void checkUser(final String email) {
        databaseReference.child("user").addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            if (isBoolean) {
                                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                                    String strEmail = (String) messageSnapshot.child("email").getValue();
                                    if (email.equals(strEmail)) {
                                        SharedPreferences.Editor editor = preferences.edit();
                                        editor.putString("key", messageSnapshot.getKey());
                                        editor.putString("dob", (String) messageSnapshot.child("dob").getValue());
                                        editor.putString("email", (String) messageSnapshot.child("email").getValue());
                                        editor.putString("gender", (String) messageSnapshot.child("gender").getValue());
                                        editor.putString("name", (String) messageSnapshot.child("name").getValue());
                                        editor.putString("phone", (String) messageSnapshot.child("phone").getValue());
                                        editor.putString("profile_picture", (String) messageSnapshot.child("profile_picture").getValue());
                                        editor.putString("status", (String) messageSnapshot.child("status").getValue());
                                        editor.putBoolean("login", true);
                                        editor.commit();
                                        dismissDialog();
                                        isBoolean = false;
                                        Utils.showToast(LoginActivity.this, "Login successfully");
                                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                        LoginActivity.this.finish();
                                    } else {
                                        dismissDialog();
                                        Utils.showSnackbar(coordinatorLayout, "Login failed");
                                    }
                                }
                            }
                        } else {
                            dismissDialog();
                            Utils.showSnackbar(coordinatorLayout, "Login failed");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        dismissDialog();
                        Utils.showSnackbar(coordinatorLayout, "Login failed");
                    }
                }
        );
    }

    public void dismissDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
