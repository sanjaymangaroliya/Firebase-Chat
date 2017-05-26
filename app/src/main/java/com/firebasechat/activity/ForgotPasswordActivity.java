package com.firebasechat.activity;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.firebasechat.controller.ConstantData;
import com.firebasechat.controller.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crash.FirebaseCrash;
import com.firebasechat.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    //Global variable
    private EditText etEmail;
    private SharedPreferences preferences;
    private CoordinatorLayout coordinatorLayout;
    private FirebaseAuth auth;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgotpassword);

        preferences = getSharedPreferences(ConstantData.PREFERENCES, MODE_PRIVATE);
        FirebaseCrash.report(new Exception("ForgotPasswordActivity"));
        auth = FirebaseAuth.getInstance();
        //
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(true);
        progressDialog.setMessage("Loading...");

        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Forgot Password");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        initUI();
    }

    private void initUI() {
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        etEmail = (EditText) findViewById(R.id.etEmail);
    }

    public void onClickForgotPassword(View view) {
        final String strEmail = etEmail.getText().toString().trim();
        if (strEmail.length() == 0) {
            Utils.showSnackbar(coordinatorLayout, "Required Email!");
        } else if (!Utils.isValidEmail(strEmail)) {
            Utils.showSnackbar(coordinatorLayout, "Required Valid Email!");
        } else {
            Utils.hideKeyboard(this);
            if (Utils.isNetworkAvailable(this)) {
                progressDialog.show();
                auth.sendPasswordResetEmail(strEmail)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (progressDialog != null && progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                if (task.isSuccessful()) {
                                    final AlertDialog.Builder adb = new AlertDialog.Builder(ForgotPasswordActivity.this);
                                    adb.setTitle("Alert!");
                                    adb.setCancelable(false);
                                    adb.setMessage("Password reset link has been sent to:" + "\n" + strEmail);
                                    adb.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
                                            ForgotPasswordActivity.this.finish();
                                        }
                                    });
                                    adb.create().show();
                                } else {
                                    Utils.showSnackbar(coordinatorLayout, "Failed to forgot password!");
                                }
                            }
                        });

            } else {
                Utils.showSnackbar(coordinatorLayout, getResources().getString(R.string.internet_error));
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
                ForgotPasswordActivity.this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
