package com.islavstan.free_talker.registration;


import com.islavstan.free_talker.utils.PreferenceHelper;

public interface RegistrationContract {
    interface RegistrationInteractor {
        void registration(String sex, String birthday, PreferenceHelper preferenceHelper);
    }

    interface RegistrationPresenter {
        void registration(String sex, String birthday, PreferenceHelper preferenceHelper);
    }

    interface View {
        void registration(String sex, String birthday, PreferenceHelper preferenceHelper);
        void onRegistrationSuccess();
        void onRegistrationFailure(String message);
    }

    interface OnRegistrationListener {
        void onSuccess();
        void onFailure(String message);
    }
}
