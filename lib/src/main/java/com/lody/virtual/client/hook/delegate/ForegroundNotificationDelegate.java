package com.lody.virtual.client.hook.delegate;

import android.app.Notification;

public interface ForegroundNotificationDelegate {

	Notification getNotification();

	boolean isEnable();

	boolean isTryToHide();

	String getGroup(String orig);
}