package com.lody.virtual.client.hook.delegate;

public interface UncheckedExceptionDelegate {
	void onThreadGroupUncaughtException(Thread t, Throwable e);
	void onShutdown(Throwable e);
}
