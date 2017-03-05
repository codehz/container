package com.lody.virtual.client.hook.delegate;


import android.content.Intent;

import android.app.Activity;

public interface ComponentDelegate {

    ComponentDelegate EMPTY = new ComponentDelegate() {

        @Override
        public void beforeActivityCreate(Activity activity) {
            // Empty
        }

        @Override
        public void beforeActivityResume(Activity activity) {
            // Empty
        }

        @Override
        public void beforeActivityPause(Activity activity) {
            // Empty
        }

        @Override
        public void beforeActivityDestroy(Activity activity) {
            // Empty
        }

        @Override
        public void afterActivityCreate(Activity activity) {
            // Empty
        }

        @Override
        public void afterActivityResume(Activity activity) {
            // Empty
        }

        @Override
        public void afterActivityPause(Activity activity) {
            // Empty
        }

        @Override
        public void afterActivityDestroy(Activity activity) {
            // Empty
        }

        @Override
        public boolean onSetForeground(String pkgName) {
            return true;
        }

        @Override
        public boolean onStartService(Intent intent) {
            return true;
        }

        @Override
        public boolean onSendBroadcast(Intent intent) {
            return true;
        }

        @Override
        public boolean onAcquireContentProvider(String name) {
            return true;
        }

        @Override
        public boolean onAcquireWakeLock(String name) {
            return true;
        }
    };

    void beforeActivityCreate(Activity activity);

    void beforeActivityResume(Activity activity);

    void beforeActivityPause(Activity activity);

    void beforeActivityDestroy(Activity activity);

    void afterActivityCreate(Activity activity);

    void afterActivityResume(Activity activity);

    void afterActivityPause(Activity activity);

    void afterActivityDestroy(Activity activity);

    boolean onSetForeground(String pkgName);

    boolean onStartService(Intent intent);

    boolean onSendBroadcast(Intent intent);

    boolean onAcquireContentProvider(String name);

    boolean onAcquireWakeLock(String name);
}
