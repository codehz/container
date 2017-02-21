package com.lody.virtual.client.hook.patchs.wifi;

import android.content.Context;
import android.net.wifi.WifiInfo;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchBinderDelegate;
import com.lody.virtual.client.hook.base.StaticHook;
import com.lody.virtual.client.hook.delegate.PhoneInfoDelegate;
import com.lody.virtual.helper.utils.Reflect;

import java.lang.reflect.Method;

import mirror.android.net.wifi.IWifiManager;

/**
 * @author Lody
 *
 * @see android.net.wifi.WifiManager
 */
@Patch({GetBatchedScanResults.class, GetScanResults.class, SetWifiEnabled.class})
public class WifiManagerPatch extends PatchBinderDelegate {
	public WifiManagerPatch() {
		super(IWifiManager.Stub.TYPE, Context.WIFI_SERVICE);
	}

	@Override
	protected void onBindHooks() {
		super.onBindHooks();
		addHook(new StaticHook("getConnectionInfo") {
			@Override
			public Object call(Object who, Method method, Object... args) throws Throwable {
				WifiInfo info = (WifiInfo) super.call(who, method, args);
				if (info != null) {
					if (info.getMacAddress() != null) {
						PhoneInfoDelegate phoneInfoDelegate = VirtualCore.get().getPhoneInfoDelegate();
						if (phoneInfoDelegate != null) {
							try {
								Reflect.on(info).set("mMacAddress", phoneInfoDelegate.getMacAddress((String) Reflect.on(info).get("mMacAddress")));
							} catch (Exception ignored) {
							}
						}
					}
				}
				return info;
			}
		});
	}
}
