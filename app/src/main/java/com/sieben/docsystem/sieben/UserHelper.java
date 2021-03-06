package com.sieben.docsystem.sieben;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

class UserHelper {

    private static final String TAG = UserHelper.class.getSimpleName();
    private final Context mContext;
    private SharedPreferences mSharedPreferences;
    private static final String USER = "user";
    private static final String PASSWORD = "password";

    UserHelper(Context ctx) {
        mContext = ctx;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                mContext);
    }

    User getCredentials() {
        String userLoginSharedPref = mSharedPreferences.getString(USER, "");
        String userPasswordSharedPref = mSharedPreferences.getString(PASSWORD, "");
        if (TextUtils.isEmpty(userLoginSharedPref) || TextUtils.isEmpty(userPasswordSharedPref)) {
            return null;
        }
        User user = new User();
        user.setLogin(userLoginSharedPref);
        user.setPassword(userPasswordSharedPref);
        return user;
    }

    @SuppressLint("ApplySharedPref")
    void setCredentials(User user) {
        if (user == null || user.getPassword() == null | user.getLogin() == null) {
            Log.e(TAG, "setCredentials(): Cannot write a null user");
            return;
        }
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                mContext);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(USER, user.getLogin());
        editor.putString(PASSWORD, user.getPassword());
        editor.commit();
    }
}
