package io.github.liuran001.fakebluetoothconnect

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothProfile
import android.media.AudioManager
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class ModuleMain : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        if (lpparam.packageName == APPLICATION_ID) return
        if (hooksInstalled) return

        runCatching {
            installHooks()
            hooksInstalled = true
            XposedBridge.log("$TAG: hooked package=${lpparam.packageName}")
        }.onFailure {
            XposedBridge.log("$TAG: failed to register hooks for package=${lpparam.packageName}")
            XposedBridge.log(it)
        }
    }

    private fun installHooks() {
        XposedBridge.hookMethod(
            AudioManager::class.java.getDeclaredMethod("isBluetoothA2dpOn"),
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val current = param.result as? Boolean ?: false
                    if (!current) {
                        param.result = true
                    }
                }
            },
        )

        XposedBridge.hookMethod(
            BluetoothAdapter::class.java.getDeclaredMethod(
                "getProfileConnectionState",
                Int::class.javaPrimitiveType,
            ),
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val profile = param.args.firstOrNull() as? Int ?: return
                    if (profile != BluetoothProfile.A2DP) return

                    val current = param.result as? Int ?: return
                    if (current != BluetoothProfile.STATE_CONNECTED) {
                        param.result = BluetoothProfile.STATE_CONNECTED
                    }
                }
            },
        )
    }

    companion object {
        private const val TAG = "FakeBluetoothConnect"
        private const val APPLICATION_ID = "io.github.liuran001.fakebluetoothconnect"

        @Volatile
        private var hooksInstalled = false
    }
}
