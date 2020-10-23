package com.islavstan.free_talker.call_functions.fragments;




import com.islavstan.free_talker.activities.CallActivity;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionEventsCallback;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback;

import org.webrtc.CameraVideoCapturer;

public interface ConversationFragmentCallbackListener {

    void addTCClientConnectionCallback(QBRTCSessionStateCallback clientConnectionCallbacks);
    void removeRTCClientConnectionCallback(QBRTCSessionStateCallback clientConnectionCallbacks);

    void addRTCSessionEventsCallback(QBRTCSessionEventsCallback eventsCallback);
    void removeRTCSessionEventsCallback(QBRTCSessionEventsCallback eventsCallback);

    void addCurrentCallStateCallback(CallActivity.CurrentCallStateCallback currentCallStateCallback);
    void removeCurrentCallStateCallback(CallActivity.CurrentCallStateCallback currentCallStateCallback);

    void addOnChangeDynamicToggle(CallActivity.OnChangeDynamicToggle onChangeDynamicCallback);
    void removeOnChangeDynamicToggle(CallActivity.OnChangeDynamicToggle onChangeDynamicCallback);

    void onSetAudioEnabled(boolean isAudioEnabled);

    void onSetVideoEnabled(boolean isNeedEnableCam);

    void onSwitchAudio();

    void onHangUpCurrentSession();

    void onStartScreenSharing();

    void onSwitchCamera(CameraVideoCapturer.CameraSwitchHandler cameraSwitchHandler);
}
