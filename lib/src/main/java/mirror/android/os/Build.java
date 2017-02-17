package mirror.android.os;

import mirror.RefClass;
import mirror.RefStaticObject;

public class Build {
	public static Class<?> TYPE = RefClass.load(Build.class, "android.os.Build");

	public static RefStaticObject<String> MODEL;
	public static RefStaticObject<String> MANUFACTURER;
	public static RefStaticObject<String> BRAND;
	public static RefStaticObject<String> PRODUCT;
	public static RefStaticObject<String> DEVICE;
}
