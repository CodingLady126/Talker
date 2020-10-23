package com.islavstan.free_talker.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;


import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.islavstan.free_talker.App;
import com.islavstan.free_talker.R;
import com.islavstan.free_talker.bus.CallEvent;
import com.islavstan.free_talker.call_functions.fragments.AudioConversationFragment;
import com.islavstan.free_talker.call_functions.fragments.BaseConversationFragment;
import com.islavstan.free_talker.call_functions.fragments.ConversationFragmentCallbackListener;
import com.islavstan.free_talker.call_functions.fragments.FragmentExecutor;
import com.islavstan.free_talker.call_functions.fragments.IncomeCallFragment;
import com.islavstan.free_talker.call_functions.fragments.IncomeCallFragmentCallbackListener;
import com.islavstan.free_talker.call_functions.fragments.OnCallEventsController;
import com.islavstan.free_talker.gcm.PushNotificationSender;
import com.islavstan.free_talker.utils.Constants;
import com.islavstan.free_talker.utils.Consts;
import com.islavstan.free_talker.utils.PreferenceHelper;
import com.islavstan.free_talker.utils.RingtonePlayer;
import com.islavstan.free_talker.utils.WebRtcSessionManager;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.listeners.QBChatDialogParticipantListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBPresence;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.AppRTCAudioManager;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCConfig;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.QBSignalingSpec;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionEventsCallback;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSignalingCallback;
import com.quickblox.videochat.webrtc.exception.QBRTCSignalException;

import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smack.AbstractConnectionListener;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.webrtc.CameraVideoCapturer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


public class CallActivity extends AppCompatActivity  implements QBRTCClientSessionCallbacks, QBRTCSessionStateCallback, QBRTCSignalingCallback,
        OnCallEventsController, IncomeCallFragmentCallbackListener, ConversationFragmentCallbackListener {

    private static final String TAG = CallActivity.class.getSimpleName();

    public static final String OPPONENTS_CALL_FRAGMENT = "opponents_call_fragment";
    public static final String INCOME_CALL_FRAGMENT = "income_call_fragment";
    public static final String CONVERSATION_CALL_FRAGMENT = "conversation_call_fragment";
    public static final String CALLER_NAME = "caller_name";
    public static final String SESSION_ID = "sessionID";
    public static final String START_CONVERSATION_REASON = "start_conversation_reason";

    private static final int REQUEST_MEDIA_PROJECTION = 1;

    private QBRTCSession currentSession;
    public List<QBUser> opponentsList;
    private Runnable showIncomingCallWindowTask;
    private Handler showIncomingCallWindowTaskHandler;
    private boolean closeByWifiStateAllow = true;
    private String hangUpReason;
    private boolean isInCommingCall;
    private QBRTCClient rtcClient;
    private OnChangeDynamicToggle onChangeDynamicCallback;
    private ConnectionListener connectionListener;
    private boolean wifiEnabled = true;
    // private SharedPreferences sharedPref;
    public RingtonePlayer ringtonePlayer;
    private LinearLayout connectionView;
    private AppRTCAudioManager audioManager;
    // private NetworkConnectionChecker networkConnectionChecker;
    private WebRtcSessionManager sessionManager;
    // private QbUsersDbManager dbManager;
    private ArrayList<CurrentCallStateCallback> currentCallStateCallbackList = new ArrayList<>();
    private List<Integer> opponentsIdsList;
    private boolean callStarted;
    private boolean isVideoCall;
    private long expirationReconnectionTime;
    private int reconnectHangUpTimeMillis;
    private boolean headsetPlugged;
    private boolean previousDeviceEarPiece;
    private boolean showToastAfterHeadsetPlugged = true;

    private MediaProjectionManager mMediaProjectionManager;
    private boolean callAccept = false;

    public PreferenceHelper preferenceHelper;
    List<Integer> usersList;
    int opponentId;
    private AdView mAdView;
    QBUser qbUser;
    int callType;




    HashSet<Integer> usersHashSet = new HashSet<>();
    private QBChatDialogParticipantListener participantListener;




    public static void start(Context context, boolean isIncomingCall, ArrayList<Integer> list, int opponentId, int type) {
        Intent intent = new Intent(context, CallActivity.class);
        intent.putExtra(Consts.EXTRA_IS_INCOMING_CALL, isIncomingCall);
        intent.putExtra("USERS", list);
        intent.putExtra("CALL_ID", opponentId);
        intent.putExtra("type", type);
        context.startActivity(intent);
    }

    public static void start(Context context, boolean isIncomingCall) {
        Intent intent = new Intent(context, CallActivity.class);
        intent.putExtra(Consts.EXTRA_IS_INCOMING_CALL, isIncomingCall);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.call_activity_call);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

     mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        preferenceHelper = PreferenceHelper.getInstance();
        preferenceHelper.init(getApplicationContext());

        qbUser = preferenceHelper.getQbUser();

        parseIntentExtras();

        sessionManager = WebRtcSessionManager.getInstance(this);
        if (!currentSessionExist()) {
            finish();
            return;
        }
        initFields();
        initCurrentSession(currentSession);

        initQBRTCClient();
        initAudioManager();


        ringtonePlayer = new RingtonePlayer(this, R.raw.trance, 0);


        startSuitableFragment(isInCommingCall);


        getChatDialogById();

        participantListener = new QBChatDialogParticipantListener() {
            @Override
            public void processPresence(String dialogId, QBPresence qbPresence) {
                if (QBPresence.Type.online.equals(qbPresence.getType())){
                    usersHashSet.add(qbPresence.getUserId());

                } else {
                    usersHashSet.remove(qbPresence.getUserId());

                }
            }
        };



    }




    private void getChatDialogById() {
        QBRestChatService.getChatDialogById(Constants.GROUP_DIALOG_ID).performAsync(
                new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog dialog, Bundle params) {

                        //присоединяемся к чату
                        QBChatDialog groupChatDialog = dialog;
                        DiscussionHistory discussionHistory = new DiscussionHistory();
                        discussionHistory.setMaxStanzas(0);
                        groupChatDialog.join(discussionHistory, new QBEntityCallback() {
                            @Override
                            public void onSuccess(Object o, Bundle bundle) {

                                groupChatDialog.addParticipantListener(participantListener);

                            }

                            @Override
                            public void onError(QBResponseException e) {
                                Log.d(TAG, "error join  " + e.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onError(QBResponseException responseException) {
                        Log.d(TAG, "error getChatDialogById  " + responseException.getMessage());
                    }
                });
    }






    private void startSuitableFragment(boolean isInComingCall) {
        if (isInComingCall) {
            initIncomingCallTask();
            addIncomeCallFragment();

        } else {
            addConversationFragment(isInComingCall);
        }
    }


    private void needUpdateOpponentsList(ArrayList<QBUser> newUsers) {
        notifyCallStateListenersNeedUpdateOpponentsList(newUsers);
    }

    private boolean currentSessionExist() {
        currentSession = sessionManager.getCurrentSession();
        return currentSession != null;
    }

    private void initFields() {
        opponentsIdsList = currentSession.getOpponents();
    }


    private void parseIntentExtras() {
        isInCommingCall = getIntent().getExtras().getBoolean(Consts.EXTRA_IS_INCOMING_CALL);
        if (getIntent().getExtras().getIntegerArrayList("USERS") != null) {
            usersList = getIntent().getExtras().getIntegerArrayList("USERS");
            opponentId = getIntent().getExtras().getInt("CALL_ID");
            callType = getIntent().getExtras().getInt("type");
        }


    }

    private void removeId(int id, List<Integer> list) {
        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) == id) {
                    Log.d("stas", "remove");
                    list.remove(i);

                }
            }

        }
    }

    private void shortToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private void initAudioManager() {
        Log.d(TAG, "initAudioManager");
        audioManager = AppRTCAudioManager.create(this, new AppRTCAudioManager.OnAudioManagerStateListener() {
            @Override
            public void onAudioChangedState(AppRTCAudioManager.AudioDevice audioDevice) {
                if (callStarted) {
                    if (audioManager.getSelectedAudioDevice() == AppRTCAudioManager.AudioDevice.EARPIECE) {
                        previousDeviceEarPiece = true;
                    } else if (audioManager.getSelectedAudioDevice() == AppRTCAudioManager.AudioDevice.SPEAKER_PHONE) {
                        previousDeviceEarPiece = false;
                    }
                    if (showToastAfterHeadsetPlugged) {
                       // shortToast("Audio device switched to  " + audioDevice);
                    }
                }
            }
        });

        isVideoCall = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO.equals(currentSession.getConferenceType());
        if (isVideoCall) {
            audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
            Log.d(TAG, "AppRTCAudioManager.AudioDevice.SPEAKER_PHONE");
        } else {
            audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
            previousDeviceEarPiece = true;
            Log.d(TAG, "AppRTCAudioManager.AudioDevice.EARPIECE");
        }

        audioManager.setOnWiredHeadsetStateListener(new AppRTCAudioManager.OnWiredHeadsetStateListener() {
            @Override
            public void onWiredHeadsetStateChanged(boolean plugged, boolean hasMicrophone) {
                Log.d(TAG, "hasMicrophone = " + hasMicrophone + " plugged = " + plugged);
                headsetPlugged = plugged;
                if (callStarted) {
                    shortToast("Headset " + (plugged ? "plugged" : "unplugged"));
                }
                if (onChangeDynamicCallback != null) {
                    if (!plugged) {
                        showToastAfterHeadsetPlugged = false;
                        if (previousDeviceEarPiece) {
                            setAudioDeviceDelayed(AppRTCAudioManager.AudioDevice.EARPIECE);
                        } else {
                            setAudioDeviceDelayed(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
                        }
                    }
                    onChangeDynamicCallback.enableDynamicToggle(plugged, previousDeviceEarPiece);
                }
            }
        });
        audioManager.init();
    }

    private void setAudioDeviceDelayed(final AppRTCAudioManager.AudioDevice audioDevice) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showToastAfterHeadsetPlugged = true;
                audioManager.setAudioDevice(audioDevice);
            }
        }, 500);
    }

    private void initQBRTCClient() {
        rtcClient = QBRTCClient.getInstance(this);

        rtcClient.setCameraErrorHendler(new CameraVideoCapturer.CameraEventsHandler() {
            @Override
            public void onCameraError(final String s) {

                showToast("Camera error: " + s);
            }

            @Override
            public void onCameraDisconnected() {
                showToast("Camera onCameraDisconnected: ");
            }

            @Override
            public void onCameraFreezed(String s) {
                showToast("Camera freezed: " + s);
                hangUpCurrentSession();
            }

            @Override
            public void onCameraOpening(String s) {
                showToast("Camera aOpening: " + s);
            }

            @Override
            public void onFirstFrameAvailable() {
                showToast("onFirstFrameAvailable: ");
            }

            @Override
            public void onCameraClosed() {
            }
        });


        // Configure
        //
        QBRTCConfig.setMaxOpponentsCount(Consts.MAX_OPPONENTS_COUNT);
        QBRTCConfig.setAnswerTimeInterval(7);
        QBRTCConfig.setDebugEnabled(true);


        // Add activity as callback to RTCClient
        rtcClient.addSessionCallbacksListener(this);
        // Start mange QBRTCSessions according to VideoCall parser's callbacks
        rtcClient.prepareToProcessCalls();
        connectionListener = new ConnectionListener();
        QBChatService.getInstance().addConnectionListener(connectionListener);
    }


    private void hangUpAfterLongReconnection() {
        if (expirationReconnectionTime < System.currentTimeMillis()) {
            hangUpCurrentSession();
        }
    }


    public void connectivityChanged(boolean availableNow) {
        if (callStarted) {
            showToast("Internet connection " + (availableNow ? "available" : " unavailable"));
        }
    }


    private void initIncomingCallTask() {
        showIncomingCallWindowTaskHandler = new Handler(Looper.myLooper());
        showIncomingCallWindowTask = new Runnable() {
            @Override
            public void run() {
                if (currentSession == null) {
                    return;
                }

                QBRTCSession.QBRTCSessionState currentSessionState = currentSession.getState();
                if (QBRTCSession.QBRTCSessionState.QB_RTC_SESSION_NEW.equals(currentSessionState)) {
                    rejectCurrentSession();
                } else {
                    ringtonePlayer.stop();
                    hangUpCurrentSession();
                }
                longToast("Call was stopped by timer");
            }
        };
    }


    private void longToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    private QBRTCSession getCurrentSession() {
        return currentSession;
    }

    public void rejectCurrentSession() {
        if (getCurrentSession() != null) {
            getCurrentSession().rejectCall(new HashMap<String, String>());
        }
    }

    public void hangUpCurrentSession() {
        ringtonePlayer.stop();
        if (getCurrentSession() != null) {
            callAccept = true;
            getCurrentSession().hangUp(new HashMap<String, String>());
        }
    }

    private void setAudioEnabled(boolean isAudioEnabled) {
        if (currentSession != null && currentSession.getMediaStreamManager() != null) {
            currentSession.getMediaStreamManager().getLocalAudioTrack().setEnabled(isAudioEnabled);
        }
    }

    private void setVideoEnabled(boolean isVideoEnabled) {
        if (currentSession != null && currentSession.getMediaStreamManager() != null) {
            currentSession.getMediaStreamManager().getLocalVideoTrack().setEnabled(isVideoEnabled);
        }
    }

    private void startIncomeCallTimer(long time) {
        showIncomingCallWindowTaskHandler.postAtTime(showIncomingCallWindowTask, SystemClock.uptimeMillis() + time);
    }

    private void stopIncomeCallTimer() {
        Log.d(TAG, "stopIncomeCallTimer");
        showIncomingCallWindowTaskHandler.removeCallbacks(showIncomingCallWindowTask);
    }


    @Override
    protected void onResume() {
        super.onResume();
        App.setAppOpen(true);

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
        QBChatService.getInstance().removeConnectionListener(connectionListener);
    }


    private void forbiddenCloseByWifiState() {
        closeByWifiStateAllow = false;
    }


    public void initCurrentSession(QBRTCSession session) {
        if (session != null) {
            Log.d(TAG, "Init new QBRTCSession");
            this.currentSession = session;
            this.currentSession.addSessionCallbacksListener(CallActivity.this);
            this.currentSession.addSignalingCallback(CallActivity.this);
        }
    }

    public void releaseCurrentSession() {
        Log.d(TAG, "Release current session");
        if (currentSession != null) {
            this.currentSession.removeSessionCallbacksListener(CallActivity.this);
            this.currentSession.removeSignalingCallback(CallActivity.this);
            rtcClient.removeSessionsCallbacksListener(CallActivity.this);
            this.currentSession = null;
        }
    }



    @Override
    public void onReceiveNewSession(final QBRTCSession session) {
        if (getCurrentSession() != null) {
            session.rejectCall(null);
        }
    }

    @Override
    public void onUserNotAnswer(QBRTCSession session, Integer userID) {
        if (!session.equals(getCurrentSession())) {
            return;
        }

    }

    @Override
    public void onUserNoActions(QBRTCSession qbrtcSession, Integer integer) {
        startIncomeCallTimer(0);
    }

    @Override
    public void onCallAcceptByUser(QBRTCSession session, Integer userId, Map<String, String> userInfo) {
        Log.d("stas2", "onCallAcceptByUser ");
        if (session.getOpponents().get(0) != null) {
            preferenceHelper.putInt(PreferenceHelper.LAST_CALLER, session.getOpponents().get(0));
        }
        callAccept = true;
        if (!session.equals(getCurrentSession())) {
            return;
        }
        ringtonePlayer.stop();
    }

    @Override
    public void onCallRejectByUser(QBRTCSession session, Integer userID, Map<String, String> userInfo) {
        Log.d("stas2", "onCallRejectByUser ");
        if (!session.equals(getCurrentSession())) {
            return;
        }

    }

    @Override
    public void onConnectionClosedForUser(QBRTCSession session, Integer userID) {
        // Close app after session close of network was disabled
        if (hangUpReason != null && hangUpReason.equals(Consts.WIFI_DISABLED)) {
            Intent returnIntent = new Intent();
            setResult(Consts.CALL_ACTIVITY_CLOSE_WIFI_DISABLED, returnIntent);
            finish();
        }
    }

    @Override
    public void onConnectedToUser(QBRTCSession session, final Integer userID) {
        callStarted = true;
        notifyCallStateListenersCallStarted();
        forbiddenCloseByWifiState();
        if (isInCommingCall) {
            stopIncomeCallTimer();
        }
        Log.d(TAG, "onConnectedToUser() is started");
    }



    private void  deleteFromHash(int id){
        usersHashSet.remove(opponentId);
    }



    @Override
    public void onSessionClosed(final QBRTCSession session) {

        if (!callAccept && !isInCommingCall) {
            if (callType == 1) {
                deleteFromHash(opponentId);
                if (usersHashSet.size() > 0) {
                    releaseCurrentSession();
                    for (int id : usersHashSet) {
                        opponentId = id;
                        break;
                    }
                    ArrayList<Integer> opponentsList = new ArrayList<>();
                    opponentsList.add(opponentId);
                    QBRTCTypes.QBConferenceType conferenceType = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO;
                    QBRTCClient qbrtcClient = QBRTCClient.getInstance(getApplicationContext());
                    QBRTCSession newQbRtcSession = qbrtcClient.createNewSessionWithOpponents(opponentsList, conferenceType);
                    WebRtcSessionManager.getInstance(this).setCurrentSession(newQbRtcSession);
                    initCurrentSession(newQbRtcSession);
                    initQBRTCClient();
                    EventBus.getDefault().post(new CallEvent());

                } else {
                    Toast.makeText(this, R.string.no_users_online, Toast.LENGTH_LONG).show();
                    if (session.equals(getCurrentSession())) {
                        ringtonePlayer.stop();
                        if (audioManager != null) {
                            audioManager.close();
                        }
                        releaseCurrentSession();

                        closeByWifiStateAllow = true;
                        finish();
                    }
                }
            } else {

                removeId(opponentId, usersList);
                if (usersList.size() > 0) {
                    releaseCurrentSession();
                    opponentId = usersList.get(0);
                    ArrayList<Integer> opponentsList = new ArrayList<>();
                    opponentsList.add(opponentId);
                    QBRTCTypes.QBConferenceType conferenceType = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO;
                    QBRTCClient qbrtcClient = QBRTCClient.getInstance(getApplicationContext());
                    QBRTCSession newQbRtcSession = qbrtcClient.createNewSessionWithOpponents(opponentsList, conferenceType);
                    WebRtcSessionManager.getInstance(this).setCurrentSession(newQbRtcSession);
                    initCurrentSession(newQbRtcSession);
                    initQBRTCClient();
                    EventBus.getDefault().post(new CallEvent());

                } else {
                    Toast.makeText(this, R.string.no_users_online, Toast.LENGTH_LONG).show();
                    if (session.equals(getCurrentSession())) {
                        ringtonePlayer.stop();
                        if (audioManager != null) {
                            audioManager.close();
                        }
                        releaseCurrentSession();

                        closeByWifiStateAllow = true;
                        finish();
                    }
                }
            }

        } else {
            if (session.equals(getCurrentSession())) {
                ringtonePlayer.stop();
                if (audioManager != null) {
                    audioManager.close();
                }
                releaseCurrentSession();

                closeByWifiStateAllow = true;
                finish();
            }
        }
    }






    @Override
    public void onSessionStartClose(final QBRTCSession session) {
        if (session.equals(getCurrentSession())) {
            session.removeSessionCallbacksListener(CallActivity.this);
            notifyCallStateListenersCallStopped();
        }
    }

    @Override
    public void onDisconnectedFromUser(QBRTCSession session, Integer userID) {

    }

    private void showToast(final int message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                shortToast(message + "");
            }
        });
    }

    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                shortToast(message);
            }
        });
    }

    @Override
    public void onReceiveHangUpFromUser(final QBRTCSession session, final Integer userID, Map<String, String> map) {
        if (session.equals(getCurrentSession())) {

            if (userID.equals(session.getCallerID())) {
                hangUpCurrentSession();
                Log.d(TAG, "initiator hung up the call");
            }


        }
    }

    private android.support.v4.app.Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }

    private void addIncomeCallFragment() {
        Log.d(TAG, "QBRTCSession in addIncomeCallFragment is " + currentSession);

        if (currentSession != null) {
            IncomeCallFragment fragment = new IncomeCallFragment();
            FragmentExecutor.addFragment(getSupportFragmentManager(), R.id.fragment_container, fragment, INCOME_CALL_FRAGMENT);
        } else {
            Log.d(TAG, "SKIP addIncomeCallFragment method");
        }
    }

    private void addConversationFragment(boolean isIncomingCall) {
        Log.d(TAG, "addConversationFragment from incomeCallFragment");
        BaseConversationFragment conversationFragment = BaseConversationFragment.newInstance(new AudioConversationFragment(), isIncomingCall);
        FragmentExecutor.addFragment(getSupportFragmentManager(), R.id.fragment_container, conversationFragment, conversationFragment.getClass().getSimpleName());
    }


    @Override
    public void onSuccessSendingPacket(QBSignalingSpec.QBSignalCMD qbSignalCMD, Integer integer) {
    }

    @Override
    public void onErrorSendingPacket(QBSignalingSpec.QBSignalCMD qbSignalCMD, Integer userId, QBRTCSignalException e) {

    }


    public void onUseHeadSet(boolean use) {
        audioManager.setManageHeadsetByDefault(use);
    }

    public void sendHeadsetState() {
        if (isInCommingCall) {
            onChangeDynamicCallback.enableDynamicToggle(headsetPlugged, previousDeviceEarPiece);
        }
    }

    ////////////////////////////// IncomeCallFragmentCallbackListener ////////////////////////////

    @Override
    public void onAcceptCurrentSession() {
        if (currentSession != null) {
            addConversationFragment(true);
            if (currentSession.getCallerID() != null)
                preferenceHelper.putInt(PreferenceHelper.LAST_CALLER, currentSession.getCallerID());


        } else {
            Log.d(TAG, "SKIP addConversationFragment method");
        }
    }

    @Override
    public void onRejectCurrentSession() {
        rejectCurrentSession();
    }
    //////////////////////////////////////////   end   /////////////////////////////////////////////


    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    ////////////////////////////// ConversationFragmentCallbackListener ////////////////////////////

    @Override
    public void addTCClientConnectionCallback(QBRTCSessionStateCallback clientConnectionCallbacks) {
        if (currentSession != null) {
            currentSession.addSessionCallbacksListener(clientConnectionCallbacks);
        }
    }

    @Override
    public void addRTCSessionEventsCallback(QBRTCSessionEventsCallback eventsCallback) {
        QBRTCClient.getInstance(this).addSessionCallbacksListener(eventsCallback);
    }

    @Override
    public void onSetAudioEnabled(boolean isAudioEnabled) {
        setAudioEnabled(isAudioEnabled);
    }

    @Override
    public void onSetVideoEnabled(boolean isNeedEnableCam) {

    }

    @Override
    public void onHangUpCurrentSession() {
        hangUpCurrentSession();
    }

    @Override
    public void onStartScreenSharing() {

    }

    @Override
    public void onSwitchCamera(CameraVideoCapturer.CameraSwitchHandler cameraSwitchHandler) {

    }


    @Override
    public void onSwitchAudio() {
        if (audioManager.getSelectedAudioDevice() == AppRTCAudioManager.AudioDevice.WIRED_HEADSET
                || audioManager.getSelectedAudioDevice() == AppRTCAudioManager.AudioDevice.EARPIECE) {
            audioManager.setAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
        } else {
            audioManager.setAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
        }
    }

    @Override
    public void removeRTCClientConnectionCallback(QBRTCSessionStateCallback clientConnectionCallbacks) {
        if (currentSession != null) {
            currentSession.removeSessionCallbacksListener(clientConnectionCallbacks);
        }
    }

    @Override
    public void removeRTCSessionEventsCallback(QBRTCSessionEventsCallback eventsCallback) {
        QBRTCClient.getInstance(this).removeSessionsCallbacksListener(eventsCallback);
    }

    @Override
    public void addCurrentCallStateCallback(CurrentCallStateCallback currentCallStateCallback) {
        currentCallStateCallbackList.add(currentCallStateCallback);
    }

    @Override
    public void removeCurrentCallStateCallback(CurrentCallStateCallback currentCallStateCallback) {
        currentCallStateCallbackList.remove(currentCallStateCallback);
    }

    @Override
    public void addOnChangeDynamicToggle(OnChangeDynamicToggle onChangeDynamicCallback) {
        this.onChangeDynamicCallback = onChangeDynamicCallback;
        sendHeadsetState();
    }

    @Override
    public void removeOnChangeDynamicToggle(OnChangeDynamicToggle onChangeDynamicCallback) {
        this.onChangeDynamicCallback = null;
    }


    public void onStopPreview() {
        onBackPressed();
    }

    //////////////////////////////////////////   end   /////////////////////////////////////////////
    private class ConnectionListener extends AbstractConnectionListener {
        @Override
        public void connectionClosedOnError(Exception e) {
            // showNotificationPopUp(R.string.connection_was_lost, true);

        }

        @Override
        public void reconnectionSuccessful() {
            // showNotificationPopUp(R.string.connection_was_lost, false);
        }

        @Override
        public void reconnectingIn(int seconds) {
            if (!callStarted) {
                hangUpAfterLongReconnection();
            }
        }
    }

    public interface OnChangeDynamicToggle {
        void enableDynamicToggle(boolean plugged, boolean wasEarpiece);
    }


    public interface CurrentCallStateCallback {
        void onCallStarted();

        void onCallStopped();

        void onOpponentsListUpdated(ArrayList<QBUser> newUsers);
    }

    private void notifyCallStateListenersCallStarted() {
        for (CurrentCallStateCallback callback : currentCallStateCallbackList) {
            callback.onCallStarted();
        }
    }

    private void notifyCallStateListenersCallStopped() {
        for (CurrentCallStateCallback callback : currentCallStateCallbackList) {
            callback.onCallStopped();
        }
    }

    private void notifyCallStateListenersNeedUpdateOpponentsList(final ArrayList<QBUser> newUsers) {
        for (CurrentCallStateCallback callback : currentCallStateCallbackList) {
            callback.onOpponentsListUpdated(newUsers);
        }
    }
}