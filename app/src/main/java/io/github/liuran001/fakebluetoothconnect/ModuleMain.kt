package io.github.liuran001.fakebluetoothconnect

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothProfile
import android.media.AudioManager
import android.util.Log
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedInterface.AfterHookCallback
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageLoadedParam

class ModuleMain(
    base: XposedInterface,
    param: ModuleLoadedParam,
) : XposedModule(base, param) {

    init {
        log(Log.INFO, TAG, "module loaded", null)
    }

    override fun onPackageLoaded(param: PackageLoadedParam) {
        super.onPackageLoaded(param)

        if (!param.isFirstPackage) return
        if (param.packageName == APPLICATION_ID) return

        log(Log.INFO, TAG, "hooking package=${param.packageName}", null)

        runCatching {
            hook(AudioManager::class.java.getDeclaredMethod("isBluetoothA2dpOn"), ForceBluetoothA2dpOnHooker::class.java)
            hook(
                BluetoothAdapter::class.java.getDeclaredMethod(
                    "getProfileConnectionState",
                    Int::class.javaPrimitiveType,
                ),
                BluetoothProfileStateHooker::class.java,
            )
        }.onFailure {
            log(Log.ERROR, TAG, "failed to register hooks", it)
        }
    }

    class ForceBluetoothA2dpOnHooker : XposedInterface.Hooker {
        companion object {
            @JvmStatic
            fun after(callback: AfterHookCallback) {
                val current = callback.result as? Boolean ?: false
                if (!current) {
                    callback.setResult(true)
                }
            }
        }
    }

    class BluetoothProfileStateHooker : XposedInterface.Hooker {
        companion object {
            @JvmStatic
            fun after(callback: AfterHookCallback) {
                val args = callback.args
                val profile = args.firstOrNull() as? Int ?: return
                if (profile != BluetoothProfile.A2DP) return

                val current = callback.result as? Int ?: return
                if (current != BluetoothProfile.STATE_CONNECTED) {
                    callback.setResult(BluetoothProfile.STATE_CONNECTED)
                }
            }
        }
    }

    companion object {
        private const val TAG = "FakeBluetoothConnect"
        private const val APPLICATION_ID = "io.github.liuran001.fakebluetoothconnect"
    }
}
