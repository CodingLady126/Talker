package com.islavstan.free_talker.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.quickblox.users.model.QBUser;


public class PreferenceHelper {
    private static PreferenceHelper instance;
    private Context context;
    private SharedPreferences preferences;

    public static final String QB_USER_LOGIN = "qb_user_login";
    public static final String QB_USER_YEAR = "qb_user_year";
    public static final String QB_USER_PASSWORD = "qb_user_password";
    public static final String QB_USER_SEX = "qb_user_sex";
    public static final String QB_USER_ID = "qb_user_id";
    public static final String LEVEL2 = "level2";
    public static final String LEVEL2_EXIST = "level2_exist";
    public static final String LAST_CALLER = "last_caller";

    public static PreferenceHelper getInstance() {
        if (instance == null) {
            instance = new PreferenceHelper();
        }
        return instance;
    }

    public void init(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE);

    }

    public void putString(String key, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void putInt(String key, int value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public void putBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBoolean(String key) {
        return preferences.getBoolean(key, false);

    }


    public String getString(String key) {
        return preferences.getString(key, "");

    }
    public int getInt(String key) {
        return preferences.getInt(key, 0);

    }

    public void saveQbUser(QBUser qbUser, String sex, String year) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(QB_USER_ID, qbUser.getId());
        editor.putString(QB_USER_LOGIN, qbUser.getLogin());
        editor.putString(QB_USER_PASSWORD, Constants.PASSWORD);
        editor.putString(QB_USER_SEX, sex);
        editor.putString(QB_USER_YEAR, year);
        editor.apply();

    }

    public boolean hasQbUser() {
        int qbUser = preferences.getInt(QB_USER_ID, 0);
        return qbUser != 0;
    }

    public String getLogin(){
        String login = preferences.getString(QB_USER_LOGIN, "");
        return login;
    }

    public String getPassword(){
        String password = preferences.getString(QB_USER_PASSWORD, "");
        return password;
    }

    public int getId(){
        int id = preferences.getInt(QB_USER_ID, 0);
        return id;
    }

    public String getSex(){
        String sex = preferences.getString(QB_USER_SEX, "");
        return sex;
    }

    public QBUser getQbUser() {
        if (hasQbUser()) {
            int id = preferences.getInt(QB_USER_ID, 0);
            String login = preferences.getString(QB_USER_LOGIN, "");
            String password = preferences.getString(QB_USER_PASSWORD, "");
           // String sex = preferences.getString(QB_USER_SEX, "");
            QBUser user = new QBUser(login, password);
            user.setId(id);
            return user;
        } else {
            return null;
        }
    }

}
