package com.eightball.pool

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

object ShizukuHelper {

    // 1. Verifica se o serviço do Shizuku está rodando no sistema
    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Exception) {
            false
        }
    }

    // 2. Verifica se o usuário autorizou o seu app dentro do Shizuku
    fun hasShizukuPermission(): Boolean {
        return if (Shizuku.isPreV11()) {
            false
        } else {
            // Verifica se a permissão foi concedida
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        }
    }

    // 3. Solicita a permissão ao usuário
    fun requestPermission(listener: Shizuku.OnRequestPermissionResultListener) {
        Shizuku.addRequestPermissionResultListener(listener)
        Shizuku.requestPermission(100)
    }

    // 4. Remove o listener para evitar vazamento de memória (chame no onDestroy)
    fun removePermissionListener(listener: Shizuku.OnRequestPermissionResultListener) {
        Shizuku.removeRequestPermissionResultListener(listener)
    }

    // 5. Função para rodar os scripts .sh com privilégios de Shell
    fun executeScript(command: String): String {
        return try {
            // Executa o comando definindo /data/local/tmp como diretório de trabalho padrão
            val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, "/data/local/tmp")
            
            val output = StringBuilder()
            
            // Lê a saída padrão (sucesso) e a saída de erro simultaneamente
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            
            var errLine: String?
            while (errorReader.readLine().also { errLine = it } != null) {
                output.append("ERRO: ").append(errLine).append("\n")
            }
            
            process.waitFor()
            output.toString().trim()
        } catch (e: Exception) {
            "Falha Crítica: ${e.message}"
        }
    }
}
