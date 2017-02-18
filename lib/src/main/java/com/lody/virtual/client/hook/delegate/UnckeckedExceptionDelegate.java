package com.lody.virtual.client.hook.delegate;

public interface UnckeckedExceptionDelegate {
	void onThreadGroupUncaughtException(Thread t, Throwable e);
	void onShutdown(Throwable e);
}
