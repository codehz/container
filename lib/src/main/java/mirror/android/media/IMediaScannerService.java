package mirror.android.media;

import android.os.IBinder;
import android.os.IInterface;

import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefStaticMethod;

public class IMediaScannerService {
	public static Class<?> TYPE = RefClass.load(IMediaScannerService.class, "android.media.IMediaScannerService");

	public static class Stub {
		public static Class<?> TYPE = RefClass.load(Stub.class, "android.media.IMediaScannerService$Stub");
		@MethodParams({IBinder.class})
		public static RefStaticMethod<IInterface> asInterface;
	}
}
