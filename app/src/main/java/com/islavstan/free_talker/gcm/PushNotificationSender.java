package com.islavstan.free_talker.gcm;

import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.messages.QBPushNotifications;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBEvent;
import com.quickblox.messages.model.QBNotificationType;

import java.util.ArrayList;


public class PushNotificationSender {
    public static void sendPushMessage(ArrayList<Integer> recipients, String senderId) {
        QBEvent qbEvent = new QBEvent();
        qbEvent.setNotificationType(QBNotificationType.PUSH);
        qbEvent.setEnvironment(QBEnvironment.DEVELOPMENT);
        qbEvent.setMessage(senderId);
        StringifyArrayList<Integer> userIds = new StringifyArrayList<>(recipients);
        qbEvent.setUserIds(userIds);
        QBPushNotifications.createEvent(qbEvent).performAsync(null);
    }
}
