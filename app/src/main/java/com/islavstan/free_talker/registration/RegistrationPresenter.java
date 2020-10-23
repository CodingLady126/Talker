package com.islavstan.free_talker.registration;

import com.islavstan.free_talker.utils.PreferenceHelper;


public class RegistrationPresenter implements RegistrationContract.RegistrationPresenter, RegistrationContract.OnRegistrationListener {

    private RegistrationContract.View mRegisterView;
    private RegistrationInteractor mRegisterInteractor;

    public RegistrationPresenter(RegistrationContract.View registerView) {
        this.mRegisterView = registerView;
        mRegisterInteractor = new RegistrationInteractor(this);
    }

    @Override
    public void onSuccess() {
        mRegisterView.onRegistrationSuccess();
    }

    @Override
    public void onFailure(String message) {
        mRegisterView.onRegistrationFailure(message);

    }

    @Override
    public void registration(String sex, String birthday, PreferenceHelper preferenceHelper) {
        mRegisterInteractor.registration(sex, birthday, preferenceHelper);

    }
}
