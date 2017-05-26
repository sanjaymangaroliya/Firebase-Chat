
package com.firebasechat.pushnotification;

import android.content.SharedPreferences;

import com.firebasechat.controller.ConstantData;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private SharedPreferences preferences;

    @Override
    public void onTokenRefresh() {
        preferences = getSharedPreferences(ConstantData.PREFERENCES, MODE_PRIVATE);
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("token", refreshedToken);
        editor.commit();
    }
}
