package com.eightball.pool

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
            Log.e(TAG, "Error checking Shizuku permission: ${e.message}")
            false
        }
    }

    fun requestShizukuPermission(listener: Shizuku.OnRequestPermissionResultListener) {
        try {
            Shizuku.addRequestPermissionResultListener(listener)
            Shizuku.requestPermission(SHIZUKU_PERMISSION_CODE)
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting Shizuku permission: ${e.message}")
        }
    }

    fun executeCommand(command: String): String {
        return try {
            if (!isShizukuAvailable()) {
                Log.w(TAG, "Shizuku not available, falling back to Runtime")
                return executeWithRuntime(command)
            }

            if (!hasShizukuPermission()) {
                Log.w(TAG, "No Shizuku permission")
                return "No Shizuku permission"
            }

            val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
            val output = process.inputStream.bufferedReader().readText()
            val error = process.errorStream.bufferedReader().readText()
            process.waitFor()

            Log.d(TAG, "Shizuku command output: $output")
            if (error.isNotEmpty()) Log.e(TAG, "Shizuku command error: $error")

            output.ifEmpty { error }
        } catch (e: Exception) {
            Log.e(TAG, "Shizuku execution error: ${e.message}", e)
            executeWithRuntime(command)
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
            process.waitFor()
            output
        } catch (e: Exception) {
            Log.e(TAG, "Runtime execution error: ${e.message}", e)
            "Error: ${e.message}"
        }
    }
}
