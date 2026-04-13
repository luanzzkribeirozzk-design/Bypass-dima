package com.eightball.pool

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import rikka.shizuku.Shizuku

object ShizukuHelper {

    private const val TAG = "ShizukuHelper"
    private const val SHIZUKU_PERMISSION_CODE = 200

    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Exception) {
            Log.e(TAG, "Shizuku not available: ${e.message}")
            false
        }
    }

    fun hasShizukuPermission(): Boolean {
        return try {
            if (Shizuku.isPreV11() || Shizuku.getVersion() < 11) {
                false
            } else {
                Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking permission: ${e.message}")
            false
        }
    }

    fun requestShizukuPermission(listener: Shizuku.OnRequestPermissionResultListener) {
        try {
            Shizuku.addRequestPermissionResultListener(listener)
            Shizuku.requestPermission(SHIZUKU_PERMISSION_CODE)
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting permission: ${e.message}")
        }
    }

    fun executeCommand(command: String): String {
        return try {
            executeWithRuntime(command)
        } catch (e: Exception) {
            Log.e(TAG, "Execution error: ${e.message}")
            "Error: ${e.message}"
        }
    }

    fun executeScript(scriptPath: String): String {
        return executeCommand("sh $scriptPath")
    }

    fun executeRish(rishPath: String, scriptPath: String): String {
        return executeCommand("sh $rishPath $scriptPath")
    }

    private fun executeWithRuntime(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val output = process.inputStream.bufferedReader().readText()
            val error = process.errorStream.bufferedReader().readText()
            process.waitFor()
            if (output.isNotEmpty()) output else error
        } catch (e: Exception) {
            Log.e(TAG, "Runtime error: ${e.message}")
            "Error: ${e.message}"
        }
    }
}
