package com.eightball.pool

import android.util.Log
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuRemoteProcess

object ShizukuHelper {
    private const val TAG = "ShizukuHelper"
    private const val SHIZUKU_PERMISSION_CODE = 200

    fun isShizukuAvailable(): Boolean {
        return try { Shizuku.pingBinder() } catch (e: Exception) { false }
    }

    fun hasShizukuPermission(): Boolean {
        return try {
            if (Shizuku.isPreV11() || Shizuku.getVersion() < 11) false
            else Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) { false }
    }

    fun requestShizukuPermission(listener: Shizuku.OnRequestPermissionResultListener) {
        try {
            Shizuku.addRequestPermissionResultListener(listener)
            Shizuku.requestPermission(SHIZUKU_PERMISSION_CODE)
        } catch (e: Exception) { Log.e(TAG, "${e.message}") }
    }

    fun executeCommand(command: String): String {
        return try {
            if (!isShizukuAvailable()) return executeWithRuntime(command)
            if (!hasShizukuPermission()) return "No Shizuku permission"
            val p: Process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
            val out = p.inputStream.bufferedReader().readText()
            val err = p.errorStream.bufferedReader().readText()
            p.waitFor()
            if (out.isNotEmpty()) out else err
        } catch (e: Exception) { executeWithRuntime(command) }
    }

    fun executeScript(scriptPath: String): String {
        return executeCommand("sh $scriptPath")
    }

    fun executeRish(rishPath: String, scriptPath: String): String {
        return executeCommand("sh $rishPath $scriptPath")
    }

    private fun executeWithRuntime(command: String): String {
        return try {
            val p = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val out = p.inputStream.bufferedReader().readText()
            p.waitFor()
            out
        } catch (e: Exception) { "Error: ${e.message}" }
    }
}
