-adaptresourcefilecontents META-INF/xposed/java_init.list

-keep class io.github.liuran001.fakebluetoothconnect.ModuleMain {
    public <init>(...);
    public void onPackageLoaded(...);
}

-keep class * implements io.github.libxposed.api.XposedInterface$Hooker

-keepclassmembers class * implements io.github.libxposed.api.XposedInterface$Hooker {
    public static void before(...);
    public static void after(...);
    public static java.lang.Object replace(...);
}
