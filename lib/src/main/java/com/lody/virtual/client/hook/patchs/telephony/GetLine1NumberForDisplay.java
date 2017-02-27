package com.lody.virtual.client.hook.patchs.telephony;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.ReplaceLastPkgHook;

import java.lang.reflect.Method;

public class GetLine1NumberForDisplay extends ReplaceLastPkgHook {
	public GetLine1NumberForDisplay() {
		super("getLine1NumberForDisplay");
	}

	@Override
	public Object afterCall(Object who, Method method, Object[] args, Object result) throws Throwable {
		if (VirtualCore.get().getPhoneInfoDelegate() != null) {
			String res = VirtualCore.get().getPhoneInfoDelegate().getLine1Number((String) result, getAppUserId());
			if (res != null) {
				return res;
			}
		}
		return super.afterCall(who, method, args, result);
	}
}
