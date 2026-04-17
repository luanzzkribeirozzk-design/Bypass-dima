package com.eightball.pool

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.eightball.pool.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import rikka.shizuku.Shizuku
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity(), Shizuku.OnRequestPermissionResultListener {

    companion object {
        private const val TAG = "YGLN_BYPASS"
        private const val REQUEST_PERMS = 100
        private const val PICK_FILE_REQUEST = 101
        private const val WORKING_DIR = "/data/local/tmp"
    }

    private lateinit var binding: ActivityMainBinding
    private val mainScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botão Bypass Diamantes
        binding.btnBypassDiamantes.setOnClickListener {
            if (verificarShizuku()) {
                applyBypassDima()
            }
        }

        // Botão Upload para APK
        binding.btnUploadApk.setOnClickListener {
            if (verificarShizuku()) {
                openFilePicker()
            }
        }

        checkAndRequestPermissions()
    }

    private fun verificarShizuku(): Boolean {
        if (!ShizukuHelper.isShizukuAvailable()) {
            addLog("❌ Shizuku não está rodando.")
            binding.txtStatus.text = "SHIZUKU OFF"
            Toast.makeText(this, "Erro: Shizuku não está a correr!", Toast.LENGTH_LONG).show()
            return false
        }
        if (!ShizukuHelper.hasShizukuPermission()) {
            ShizukuHelper.requestPermission(this)
            return false
        }
        return true
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(intent, PICK_FILE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                processUploadedFile(uri)
            }
        }
    }

    private fun processUploadedFile(uri: Uri) {
        addLog("Arquivo selecionado para transformação...")
        mainScope.launch(Dispatchers.IO) {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val outFile = File(cacheDir, "temp_f.apk")
                inputStream?.use { input ->
                    FileOutputStream(outFile).use { output ->
                        input.copyTo(output)
                    }
                }
                
                // Usar Shizuku para mover para /data/local/tmp
                val moveCmd = "cp ${outFile.absolutePath} $WORKING_DIR/F.apk && chmod 777 $WORKING_DIR/F.apk"
                val res = ShizukuHelper.executeScript(moveCmd)
                
                withContext(Dispatchers.Main) {
                    if (res.contains("Permission denied") || res.contains("error")) {
                        addLog("❌ Erro ao mover arquivo: $res")
                    } else {
                        addLog("✅ Arquivo transformado em F.apk com sucesso!")
                        Toast.makeText(this@MainActivity, "Upload concluído!", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    addLog("❌ Erro no upload: ${e.message}")
                }
            }
        }
    }

    private fun applyBypassDima() {
        mainScope.launch {
            binding.txtStatus.text = "EXECUTANDO..."
            binding.progress_bar.isIndeterminate = true
            binding.btnBypassDiamantes.isEnabled = false
            
            addLog("Iniciando sequência de segurança...")

            withContext(Dispatchers.IO) {
                // 1. Mudar instalador
                addLog("> Alterando origem para Play Store...")
                ShizukuHelper.executeScript("pm set-installer com.dts.freefiremax com.android.vending")
                
                // 2. Executar scripts lnxiter se existirem
                addLog("> Aplicando camuflagem e injeção...")
                ShizukuHelper.executeScript("sh $WORKING_DIR/lnxiter1.sh")
                delay(500)
                ShizukuHelper.executeScript("sh $WORKING_DIR/lnxiter6.sh")
                ShizukuHelper.executeScript("sh $WORKING_DIR/lnxiter8.sh")
                ShizukuHelper.executeScript("sh $WORKING_DIR/lnxiter2.sh")
                ShizukuHelper.executeScript("sh $WORKING_DIR/lnxiter.sh")
                ShizukuHelper.executeScript("sh $WORKING_DIR/lnxiter7.sh")
                
                addLog("> Abrindo Free Fire Max...")
                ShizukuHelper.executeScript("sh $WORKING_DIR/lnxiter0.sh")
            }

            addLog("✅ Bypass finalizado!")
            binding.txtStatus.text = "BYPASS ATIVO ✅"
            binding.progress_bar.isIndeterminate = false
            binding.btnBypassDiamantes.isEnabled = true
        }
    }

    private fun addLog(message: String) {
        runOnUiThread {
            binding.txtLogs.append("\n> $message")
            binding.log_scroll.post { binding.log_scroll.fullScroll(View.FOCUS_DOWN) }
        }
    }

    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMS)
        }
    }

    override fun onRequestPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                addLog("✅ Permissão de armazenamento concedida.")
            }
        }
    }

    override fun onPermissionResult(requestCode: Int, grantResult: Int) {
        if (grantResult == PackageManager.PERMISSION_GRANTED) {
            addLog("✅ Shizuku autorizado!")
            binding.txtStatus.text = "SHIZUKU ATIVO"
        } else {
            addLog("⚠ Shizuku negado.")
            showShizukuDeniedDialog()
        }
    }

    private fun showShizukuDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Erro")
            .setMessage("O Bypass não funciona sem o Shizuku.")
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
        ShizukuHelper.removePermissionListener(this)
    }
}
