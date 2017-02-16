package com.lody.virtual.client.hook.providers;

import android.database.AbstractCursor;
import android.database.Cursor;

import com.lody.virtual.client.core.VirtualCore;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author Lody
 */

public class ExternalProviderHook extends ProviderHook {

    private static Map<String, String> mapList = VirtualCore.get().ioRedirectDelegate.getContentReversedRedirect();
    private static boolean mapInited = false;
    public ExternalProviderHook(Object base) {
        super(base);
    }

    @Override
    public Cursor query(Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        return new ProxyCursor(super.query(method, args));
    }

    @Override
    protected void processArgs(Method method, Object... args) {
        if (args != null && args.length > 0 && args[0] instanceof String) {
            String pkg = (String) args[0];
            if (VirtualCore.get().isAppInstalled(pkg)) {
                args[0] = VirtualCore.get().getHostPkg();
            }
        }
    }

    private class ProxyCursor extends AbstractCursor {
        Cursor src;

        ProxyCursor(Cursor src) {
            if (!mapInited) {
                if (VirtualCore.get().ioRedirectDelegate != null)
                    mapList = VirtualCore.get().ioRedirectDelegate.getContentReversedRedirect();
                mapInited = true;
            }
            this.src = src;
        }

        @Override
        public int getCount() {
            return src.getCount();
        }

        @Override
        public String[] getColumnNames() {
            return src.getColumnNames();
        }

        @Override
        public String getString(int column) {
            String ret = src.getString(column);
            if (src.getColumnName(column).equals("_data") && mapList != null) {
                for (Map.Entry<String, String> entry : mapList.entrySet())
                    if (ret.startsWith(entry.getKey()))
                        return entry.getValue() + ret.substring(entry.getKey().length());
                return ret;
            }
            return ret;
        }

        @Override
        public short getShort(int column) {
            return src.getShort(column);
        }

        @Override
        public int getInt(int column) {
            return src.getInt(column);
        }

        @Override
        public long getLong(int column) {
            return src.getLong(column);
        }

        @Override
        public float getFloat(int column) {
            return src.getFloat(column);
        }

        @Override
        public double getDouble(int column) {
            return src.getDouble(column);
        }

        @Override
        public boolean isNull(int column) {
            return src.isNull(column);
        }

        @Override
        public int getType(int column) {
            return src.getType(column);
        }

        @Override
        public boolean onMove(int oldPosition, int newPosition) {
            src.moveToPosition(newPosition);
            return true;
        }
    }
}
