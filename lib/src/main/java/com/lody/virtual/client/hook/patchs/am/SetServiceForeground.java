package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.patchs.notification.compat.NotificationHandler;
import com.lody.virtual.client.ipc.VActivityManager;
import com.lody.virtual.client.ipc.VNotificationManager;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.IBinder;

/**
 * @author Lody
 *
 *
 * android.app.IActivityManager.setServiceForeground(ComponentName,
 *      IBinder, int, Notification, boolean)
 *
 *  N:
 *  android.app.IActivityManager.setServiceForeground(ComponentName,
 *      IBinder, int, Notification, boolean)
 */
/* package */ class SetServiceForeground extends Hook {

	@Override
	public String getName() {
		return "setServiceForeground";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		ComponentName cn = (ComponentName) args[0];
		int id = (int) args[2];
		Notification notification = (Notification) args[3];
		if (notification == null)
			return 0;
		if (!VNotificationManager.get().dealNotification(id, notification, cn.getPackageName()))
			return 0;
		VNotificationManager.get().addNotification(id, null, cn.getPackageName(), getVUserId());
		NotificationManager manager = (NotificationManager) VirtualCore.get().getContext().getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(id, notification);
		return 0;
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
