package com.islavstan.free_talker.call_functions.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.islavstan.free_talker.R;
import com.islavstan.free_talker.activities.CallActivity;
import com.islavstan.free_talker.bus.CallEvent;
import com.islavstan.free_talker.db.DBHelper;
import com.islavstan.free_talker.utils.Consts;
import com.islavstan.free_talker.utils.PreferenceHelper;
import com.islavstan.free_talker.utils.WebRtcSessionManager;
import com.quickblox.chat.QBChatService;

import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public abstract class BaseConversationFragment extends BaseToolBarFragment implements CallActivity.CurrentCallStateCallback {

    private static final String TAG = BaseConversationFragment.class.getSimpleName();

    protected WebRtcSessionManager sessionManager;
    private boolean isIncomingCall;
    protected QBRTCSession currentSession;
    private QBRTCTypes.QBConferenceType qbConferenceType;
    private ToggleButton micToggleVideoCall;
    private ImageButton endCallBtn;
    protected ConversationFragmentCallbackListener conversationFragmentCallbackListener;
    protected Chronometer timerChronometer;
    private boolean isMessageProcessed;
    protected boolean isStarted;
    protected View outgoingOpponentsRelativeLayout;
    protected TextView ringingTextView;
    protected QBUser currentUser;
    //private ToggleButton likeBtn;
    private ImageButton blockBtn;
    DBHelper dbHelper;

    public static BaseConversationFragment newInstance(BaseConversationFragment baseConversationFragment, boolean isIncomingCall) {
        Log.d(TAG, "isIncomingCall =  " + isIncomingCall);
        Bundle args = new Bundle();
        args.putBoolean(Consts.EXTRA_IS_INCOMING_CALL, isIncomingCall);
        baseConversationFragment.setArguments(args);
        return baseConversationFragment;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            conversationFragmentCallbackListener = (ConversationFragmentCallbackListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ConversationFragmentCallbackListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        conversationFragmentCallbackListener.addCurrentCallStateCallback(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        sessionManager = WebRtcSessionManager.getInstance(getActivity());
        dbHelper = DBHelper.getInstance(getActivity());
        currentSession = sessionManager.getCurrentSession();
        if (currentSession == null) {
            Log.d(TAG, "currentSession = null onCreateView");
            return view;
        }
        initFields();
        initViews(view);
        initActionBar();
        initButtonsListener();
        prepareAndShowOutgoingScreen();
        return view;
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CallEvent event) {
        reloadSession();
    }


    void reloadSession() {
        sessionManager = WebRtcSessionManager.getInstance(getActivity());
        currentSession = sessionManager.getCurrentSession();
        conversationFragmentCallbackListener.addCurrentCallStateCallback(this);
        initButtonsListener();
        currentSession.startCall(null);
    }


    private void prepareAndShowOutgoingScreen() {
        configureOutgoingScreen();

    }

    protected abstract void configureOutgoingScreen();

    private void initActionBar() {
        configureToolbar();
        configureActionBar();
    }

    protected abstract void configureActionBar();

    protected abstract void configureToolbar();

    protected void initFields() {
        currentUser = QBChatService.getInstance().getUser();
        sessionManager = WebRtcSessionManager.getInstance(getActivity());
        currentSession = sessionManager.getCurrentSession();

        if (getArguments() != null) {
            isIncomingCall = getArguments().getBoolean(Consts.EXTRA_IS_INCOMING_CALL);
        }


        qbConferenceType = currentSession.getConferenceType();

    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        if (currentSession == null) {
            Log.d(TAG, "currentSession = null onStart");
            return;

        }

        if (currentSession.getState() != QBRTCSession.QBRTCSessionState.QB_RTC_SESSION_ACTIVE) {
            if (isIncomingCall) {
                currentSession.acceptCall(null);
            } else {
                currentSession.startCall(null);
                ((CallActivity) getActivity()).ringtonePlayer.play(true);
            }
            isMessageProcessed = true;
        }
    }

    @Override
    public void onDestroy() {
        conversationFragmentCallbackListener.removeCurrentCallStateCallback(this);
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    protected void initViews(View view) {
        micToggleVideoCall = (ToggleButton) view.findViewById(R.id.toggle_mic);
        endCallBtn = (ImageButton) view.findViewById(R.id.button_hangup_call);
        outgoingOpponentsRelativeLayout = view.findViewById(R.id.layout_background_outgoing_screen);
        ringingTextView = (TextView) view.findViewById(R.id.text_ringing);
        blockBtn = (ImageButton) view.findViewById(R.id.toggle_block);


        if (isIncomingCall) {
            ringingTextView.setText(R.string.conversation);

        }
    }

    protected void initButtonsListener() {

        blockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
                ad.setTitle("Block user");
                ad.setMessage("Do you really want to block this user?");
                ad.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        Log.d("stas", "caller id  == " + currentSession.getCallerID());
                        if (currentSession.getCallerID() != null) {
                            dbHelper.blockUser(currentSession.getCallerID())
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(result -> {
                                        if (result == 1) {
                                            Toast.makeText(getActivity(), R.string.user_blocked, Toast.LENGTH_SHORT).show();
                                            actionButtonsEnabled(false);
                                            endCallBtn.setEnabled(false);
                                            endCallBtn.setActivated(false);
                                            conversationFragmentCallbackListener.onHangUpCurrentSession();
                                            Log.d(TAG, "Call is stopped");
                                        }

                                    }, error -> Log.d(TAG, "blockUser error = " + error.getMessage()));
                        }


                    }
                });
                ad.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {

                    }
                });
                ad.setCancelable(true);
                ad.show();
            }
        });


        micToggleVideoCall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                conversationFragmentCallbackListener.onSetAudioEnabled(isChecked);
            }
        });

        endCallBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                actionButtonsEnabled(false);
                endCallBtn.setEnabled(false);
                endCallBtn.setActivated(false);

                conversationFragmentCallbackListener.onHangUpCurrentSession();
                Log.d(TAG, "Call is stopped");
            }
        });
    }

    protected void actionButtonsEnabled(boolean inability) {

        micToggleVideoCall.setEnabled(inability);

        // inactivate toggle buttons
        micToggleVideoCall.setActivated(inability);
    }

    private void startTimer() {
        if (!isStarted) {
            timerChronometer.setVisibility(View.VISIBLE);
            timerChronometer.setBase(SystemClock.elapsedRealtime());
            timerChronometer.start();
            Log.d("VOMER_DATA", "timer start");
            isStarted = true;
        }
    }

    private void stopTimer() {
        if (timerChronometer != null) {
            long durationCall = SystemClock.elapsedRealtime();
            timerChronometer.stop();
            isStarted = false;
            int conversationTime = (int) (durationCall - timerChronometer.getBase());
            if (conversationTime >= 240000) {
                int conv = ((CallActivity) getActivity()).preferenceHelper.getInt(PreferenceHelper.LEVEL2);
                conv++;
                ((CallActivity) getActivity()).preferenceHelper.putInt(PreferenceHelper.LEVEL2, conv);
            }

        }
    }


    @Override
    public void onCallStarted() {
        ringingTextView.setText(R.string.conversation);
        startTimer();
        actionButtonsEnabled(true);
    }

    @Override
    public void onCallStopped() {
        if (currentSession == null) {
            Log.d(TAG, "currentSession = null onCallStopped");
            return;
        }
        stopTimer();
        actionButtonsEnabled(false);
    }


}