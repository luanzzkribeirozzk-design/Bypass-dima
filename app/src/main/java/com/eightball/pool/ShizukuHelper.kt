package com.eightball.pool

import android.util.Log
import rikka.shizuku.Shizuku

object ShizukuHelper {

    private const val TAG = "ShizukuHelper"
    const val SHIZUKU_PERMISSION_CODE = 200

    /** Retorna true se o binder do Shizuku está ativo */
    fun isShizukuAvailable(): Boolean {
        return try { Shizuku.pingBinder() } catch (e: Exception) { false }
    }

    /** Retorna true se o app já tem permissão concedida */
    fun hasShizukuPermission(): Boolean {
        return try {
            if (Shizuku.isPreV11() || Shizuku.getVersion() < 11) false
            else Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) { false }
    }

    /**
     * Registra o listener e solicita permissão ao Shizuku.
     * Chame removeListener(listener) quando não precisar mais.
     */
    fun requestPermission(listener: Shizuku.OnRequestPermissionResultListener) {
        try {
            Shizuku.addRequestPermissionResultListener(listener)
            Shizuku.requestPermission(SHIZUKU_PERMISSION_CODE)
        } catch (e: Exception) {
            Log.e(TAG, "requestPermission error: ${e.message}")
        }
    }

    fun removePermissionListener(listener: Shizuku.OnRequestPermissionResultListener) {
        try { Shizuku.removeRequestPermissionResultListener(listener) } catch (_: Exception) {}
    }

    /** Executa um comando shell. Usa Shizuku se disponível e autorizado, senão Runtime. */
    fun executeCommand(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val output = process.inputStream.bufferedReader().readText()
            val error  = process.errorStream.bufferedReader().readText()
            process.waitFor()
            if (output.isNotEmpty()) output else error
        } catch (e: Exception) {
            Log.e(TAG, "executeCommand error: ${e.message}")
            "Error: ${e.message}"
        }
    }

    fun executeScript(scriptPath: String): String = executeCommand("sh $scriptPath")

    fun executeRish(rishPath: String, scriptPath: String): String =
        executeCommand("sh $rishPath $scriptPath")
}
