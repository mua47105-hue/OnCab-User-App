package com.oncab.user.Utils;

import android.content.Context;
import android.content.SharedPreferences;

/** Session storage helper on top of the original USER_DETAILS prefs file. */
public final class Prefs {

    private final SharedPreferences sp;

    public Prefs(Context context) {
        sp = context.getApplicationContext()
                .getSharedPreferences(Constants.FILE_NAME, Context.MODE_PRIVATE);
    }

    public boolean isLoggedIn() {
        return sp.contains(Constants.USER_ID) && sp.getString(Constants.USER_ID, "").length() > 0;
    }

    public String getUserId() {
        return sp.getString(Constants.USER_ID, "");
    }

    public void put(String key, String value) {
        sp.edit().putString(key, value).apply();
    }

    public String get(String key) {
        return sp.getString(key, "");
    }

    public void clearSession() {
        sp.edit().remove(Constants.USER_ID)
                .remove(Constants.USER_NAME)
                .remove(Constants.USER_EMAIL)
                .remove(Constants.USER_WALLET)
                .apply();
    }

    public void setProfile(String userId, String name, String email, String wallet) {
        sp.edit()
                .putString(Constants.USER_ID, userId)
                .putString(Constants.USER_NAME, name == null ? "" : name)
                .putString(Constants.USER_EMAIL, email == null ? "" : email)
                .putString(Constants.USER_WALLET, wallet == null ? "0" : wallet)
                .apply();
    }
}
