package com.lody.virtual.client.hook.delegate;

import android.app.Notification;

public interface ForegroundNotificationDelegate {

	Notification getNotification();

	boolean isEnabled();

	boolean isTryToHide();

	String getGroup(String orig);
}