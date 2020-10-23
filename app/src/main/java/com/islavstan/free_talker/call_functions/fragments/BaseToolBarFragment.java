package com.islavstan.free_talker.call_functions.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

public abstract class BaseToolBarFragment extends Fragment {

    private static final String TAG = BaseToolBarFragment.class.getSimpleName();
    protected Handler mainHandler;

    abstract int getFragmentLayout();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mainHandler = new FragmentLifeCycleHandler(this);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getFragmentLayout(), container, false);
        return view;
    }


    static class FragmentLifeCycleHandler extends Handler {

        private WeakReference<Fragment> fragmentRef;

        FragmentLifeCycleHandler(Fragment fragment) {

            this.fragmentRef = new WeakReference<>(fragment);
        }

        @Override
        public void dispatchMessage(Message msg) {
            Fragment fragment = fragmentRef.get();
            if (fragment == null) {
                return;
            }
            if (fragment.isAdded() && (fragment.getActivity() != null)) {
                super.dispatchMessage(msg);
            } else {
                Log.d(TAG, "Fragment under destroying");
            }
        }
    }
}
