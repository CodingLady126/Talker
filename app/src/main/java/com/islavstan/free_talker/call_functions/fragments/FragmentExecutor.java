package com.islavstan.free_talker.call_functions.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

public class FragmentExecutor {

    public static void addFragment(FragmentManager fragmentManager, int containerId, Fragment fragment, String tag) {
        fragmentManager.beginTransaction().replace(containerId, fragment, tag).commitAllowingStateLoss();
    }

}
