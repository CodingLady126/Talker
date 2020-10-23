package com.islavstan.free_talker.registration;


import android.os.Bundle;
import android.util.Log;

import com.islavstan.free_talker.utils.Constants;
import com.islavstan.free_talker.utils.PreferenceHelper;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.Random;

public class RegistrationInteractor implements RegistrationContract.RegistrationInteractor {
    String TAG = "stas";

    private RegistrationContract.OnRegistrationListener listener;

    public RegistrationInteractor(RegistrationContract.OnRegistrationListener listener) {
        this.listener = listener;
    }

    @Override
    public void registration(final String sex, final String birthday, final PreferenceHelper preferenceHelper) {
        final QBUser newUser = new QBUser();
        StringifyArrayList<String> sexList = new StringifyArrayList<>();
        sexList.add(sex);
        newUser.setLogin(randomString() + "_" + birthday);
        newUser.setPassword(Constants.PASSWORD);
        //в тег записываю пол
        newUser.setTags(sexList);
        QBUsers.signUp(newUser).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser result, Bundle params) {
                Log.d(TAG, "signUp success" + result.getLogin());
                preferenceHelper.saveQbUser(result, sex, birthday);
                listener.onSuccess();


            }

            @Override
            public void onError(QBResponseException e) {
                listener.onFailure(e.getMessage());

            }
        });
    }


    private void signIn(QBUser qbUser) {
        qbUser.setPassword(Constants.PASSWORD);
        QBUsers.signIn(qbUser).performAsync(new QBEntityCallbackImpl<QBUser>() {
            @Override
            public void onSuccess(QBUser result, Bundle params) {
                Log.d(TAG, "onSuccess signIn " + result.getLogin());
                listener.onSuccess();
            }

            @Override
            public void onError(QBResponseException responseException) {
                Log.d(TAG, "error signIn  " + responseException.getMessage());
                listener.onFailure(responseException.getMessage());
            }
        });
    }

    private String randomString() {
        char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 7; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        String output = sb.toString();
        return output;
    }
}
