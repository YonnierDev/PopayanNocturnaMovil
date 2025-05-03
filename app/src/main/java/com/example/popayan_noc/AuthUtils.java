package com.example.popayan_noc;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONException;
import org.json.JSONObject;

public class AuthUtils {
    private static final String PREFS_NAME = "popnoc_prefs";
    private static final String TOKEN_KEY = "token";
    private static final String USER_KEY = "usuario";

    public static String getToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(TOKEN_KEY, null);
    }

    public static JSONObject getUser(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String userStr = prefs.getString(USER_KEY, null);
        if (userStr == null) return null;
        try {
            return new JSONObject(userStr);
        } catch (JSONException e) {
            return null;
        }
    }

    public static void clearSession(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}
