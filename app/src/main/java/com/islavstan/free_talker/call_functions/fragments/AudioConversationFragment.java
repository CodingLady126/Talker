package com.islavstan.free_talker.call_functions.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.ToggleButton;


import com.islavstan.free_talker.R;
import com.islavstan.free_talker.activities.CallActivity;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;


public class AudioConversationFragment extends BaseConversationFragment implements CallActivity.OnChangeDynamicToggle {
    private static final String TAG = AudioConversationFragment.class.getSimpleName();

    private ToggleButton audioSwitchToggleButton;
    private TextView alsoOnCallText;
    private TextView firstOpponentNameTextView;
    private TextView otherOpponentsTextView;
    private boolean headsetPlugged;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        conversationFragmentCallbackListener.addOnChangeDynamicToggle(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void configureOutgoingScreen() {

    }

    @Override
    protected void configureToolbar() {
    }

    @Override
    protected void configureActionBar() {
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        timerChronometer = (Chronometer) view.findViewById(R.id.chronometer_timer_audio_call);
        alsoOnCallText = (TextView) view.findViewById(R.id.text_also_on_call);
        firstOpponentNameTextView = (TextView) view.findViewById(R.id.text_caller_name);
        otherOpponentsTextView = (TextView) view.findViewById(R.id.text_other_inc_users);
        audioSwitchToggleButton = (ToggleButton) view.findViewById(R.id.toggle_speaker);
        audioSwitchToggleButton.setVisibility(View.VISIBLE);

        actionButtonsEnabled(false);
    }


    @Override
    public void onStop() {
        super.onStop();
        conversationFragmentCallbackListener.removeOnChangeDynamicToggle(this);
    }

    @Override
    protected void initButtonsListener() {
        super.initButtonsListener();

        audioSwitchToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                conversationFragmentCallbackListener.onSwitchAudio();
            }
        });
    }

    @Override
    protected void actionButtonsEnabled(boolean inability) {
        super.actionButtonsEnabled(inability);
        if (!headsetPlugged) {
            audioSwitchToggleButton.setEnabled(inability);
        }
        audioSwitchToggleButton.setActivated(inability);
    }


    int getFragmentLayout() {
        return R.layout.call_fragment_audio_conversation;
    }

    @Override
    public void onOpponentsListUpdated(ArrayList<QBUser> newUsers) {

    }

    @Override
    public void enableDynamicToggle(boolean plugged, boolean previousDeviceEarPiece) {
        headsetPlugged = plugged;

        if (isStarted) {
            audioSwitchToggleButton.setEnabled(!plugged);

            if (plugged) {
                audioSwitchToggleButton.setChecked(true);
            } else if (previousDeviceEarPiece) {
                audioSwitchToggleButton.setChecked(true);
            } else {
                audioSwitchToggleButton.setChecked(false);
            }
        }
    }
}