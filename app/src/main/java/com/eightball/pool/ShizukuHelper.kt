package com.eightball.pool

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder

object ShizukuHelper {

    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Exception) {
            false
        }
    }

    fun hasShizukuPermission(): Boolean {
        return if (Shizuku.isPreV11()) {
            false
        } else {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestPermission(listener: Shizuku.OnRequestPermissionResultListener) {
        Shizuku.addRequestPermissionResultListener(listener)
        Shizuku.requestPermission(100)
    }

    fun executeScript(command: String): String {
        return try {
            // Comando reforçado: libera a pasta tmp e executa o pedido
            val internalCommand = "chmod 777 /data/local/tmp && chmod 777 /data/local/tmp/* && $command"
            
            val process = Shizuku.newProcess(arrayOf("sh", "-c", internalCommand), null, null)
            val output = StringBuilder()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            
            var line: String? = ""
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            
            var errLine: String? = ""
            while (errorReader.readLine().also { errLine = it } != null) {
                output.append(errLine).append("\n")
            }
            
            process.waitFor()
            output.toString().trim()
        } catch (e: Exception) {
            "Falha Crítica: ${e.message}"
        }
    }
}
s.waitFor()
            output.toString().trim()
        } catch (e: Exception) {
            "Falha Crítica: ${e.message}"
        }
    }
}
