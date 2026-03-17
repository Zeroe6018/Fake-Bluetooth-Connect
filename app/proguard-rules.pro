-adaptresourcefilecontents assets/xposed_init

-keep class io.github.liuran001.fakebluetoothconnect.ModuleMain {
    public <init>();
    public void handleLoadPackage(...);
}
