package com.firebasechat.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    public static final String DDMMYYYY = "dd/MM/yyyy";
    public static final String MDYYYY = "M/d/yyyy";
    public static final String HHMMA = "hh:mm a";
    public static final double LAT = 32.7870175;
    public static final double LNG = -117.1096997;
    //public static final double LAT = 22.272993;
    //public static final double LNG = 70.7547988;
    public static final boolean isDebugging = true;

    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    public static void showSnackbar(CoordinatorLayout coordinatorLayout, String strMsg) {
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, strMsg, Snackbar.LENGTH_LONG);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }

    public static void showToast(Context context, String strMsg) {
        Toast toast = Toast.makeText(context, strMsg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void showAlert(Context context, String title, String msg) {
        AlertDialog.Builder adb = new AlertDialog.Builder(context);
        adb.setTitle(title);
        adb.setMessage(msg);
        adb.setNegativeButton("Ok", null);
        adb.create().show();
    }

    public static boolean isThisDateValid(String dateToValidate) {
        if (dateToValidate == null || dateToValidate.trim().length() == 0) {
            return false;
        }

        Date date = null;

        SimpleDateFormat sdf = new SimpleDateFormat(DDMMYYYY);
        try {
            sdf.parse(dateToValidate);
            if (!dateToValidate.equals(sdf.format(date))) {
                return false;
            }
            return true;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }

    }

    public static boolean isValidFormat(String value) {
        if (value.matches("([0-9]{2})/([0-9]{2})/([0-9]{4})"))
            return true;
        else
            return false;
    }

    public static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat(HHMMA);
        try {
            Date date = new Date();
            return sdf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }


    public static String getUnixTime(String strDate) {
        if (strDate == null || strDate.trim().length() == 0) {
            return "";
        }

        SimpleDateFormat sdf = new SimpleDateFormat(DDMMYYYY);
        try {
            Date date = sdf.parse(strDate);
            System.out.println(date);

            long unixTime = (long) date.getTime() / 1000;
            return unixTime + "";
        } catch (ParseException e) {
            e.printStackTrace();

        }
        return "";
    }

    public static String getUnixToDateTime(String strDate) {
        if (strDate == null || strDate.trim().length() == 0) {
            return "";
        }

        long unixTime = Long.parseLong(strDate);
        Date date = new Date(unixTime * 1000L); // *1000 is to convert seconds to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat(DDMMYYYY);

        String formattedDate = sdf.format(date);
        return formattedDate;
    }

    // this method uses for the unix time to date of birth
    public static String getUnixToTime(String strDate) {
        if (strDate == null || strDate.trim().length() == 0) {
            return "";
        }

        long unixTime = Long.parseLong(strDate);
        Date date = new Date(unixTime * 1000L); // *1000 is to convert seconds to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat(HHMMA);

        String formattedDate = sdf.format(date);
        return formattedDate;
    }

    public static String getUnixToDateConversation(String strDate) {
        if (strDate == null || strDate.trim().length() == 0) {
            return "";
        }

        long unixTime = Long.parseLong(strDate);
        Date date = new Date(unixTime * 1000L); // *1000 is to convert seconds to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat(MDYYYY);

        String formattedDate = sdf.format(date);
        return formattedDate;
    }

    public static String getFormattedDate(String strDate, String strFDate, String strNDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(strFDate);
            Date testDate = sdf.parse(strDate);
            SimpleDateFormat formatter = new SimpleDateFormat(strNDate);
            return formatter.format(testDate);

        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    public static String getDeviceID(Context context) {
        String android_id = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return android_id;
    }

    public static boolean isAppInstalled(Context context, String name) {
        try {
            context.getPackageManager().getApplicationInfo(name, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }


    public static boolean isStringNull(String str) {
        if (str == "" || str == null || str.equalsIgnoreCase("null") || str.trim().length() == 0) {
            return true;
        }
        return false;
    }

    public static void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static String local(String latitudeFinal, String longitudeFinal) {
        return "https://maps.googleapis.com/maps/api/staticmap?center=" + latitudeFinal + "," + longitudeFinal + "&zoom=18&size=280x280&markers=color:red|" + latitudeFinal + "," + longitudeFinal;
    }

    public static CharSequence convertTimeStamp(String str) {
        return DateUtils.getRelativeTimeSpanString(Long.parseLong(str), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
    }
}