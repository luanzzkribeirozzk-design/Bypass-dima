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
import android.widget.Button
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import rikka.shizuku.Shizuku
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LNXITER_BYPASS"
        private const val REQUEST_PERMS = 100
        private const val PICK_FILE_REQUEST = 101
        private const val WORKING_DIR = "/data/local/tmp"
    }

    private lateinit var tvStatus: TextView
    private lateinit var tvLogs: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var logScroll: ScrollView
    private lateinit var btnBypassDima: Button
    private lateinit var btnHideStream: Button
    private lateinit var btnUploadApk: Button

    private var isHideStreamOn = false
    private val mainScope = CoroutineScope(Dispatchers.Main + Job())

    private val shizukuPermissionListener =
        Shizuku.OnRequestPermissionResultListener { _, grantResult ->
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                addLog("✅ Shizuku autorizado!")
                tvStatus.text = "SHIZUKU ATIVO"
                initializeApp()
            } else {
                addLog("⚠ Shizuku negado.")
                showShizukuDeniedDialog()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tv_status)
        tvLogs = findViewById(R.id.tv_logs)
        progressBar = findViewById(R.id.progress_bar)
        logScroll = findViewById(R.id.log_scroll)
        btnBypassDima = findViewById(R.id.btn_bypass_dima)
        btnHideStream = findViewById(R.id.btn_hide_stream)
        btnUploadApk = findViewById(R.id.btn_upload_apk)

        btnBypassDima.setOnClickListener { applyBypassDima() }
        btnHideStream.setOnClickListener { toggleHideStream() }
        btnUploadApk.setOnClickListener { openFilePicker() }

        checkAndRequestPermissions()
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
                val outFile = File(WORKING_DIR, "F.apk")
                inputStream?.use { input ->
                    FileOutputStream(outFile).use { output ->
                        input.copyTo(output)
                    }
                }
                setExecutePermission(outFile.absolutePath)
                withContext(Dispatchers.Main) {
                    addLog("✅ Arquivo transformado em F.apk com sucesso!")
                    Toast.makeText(this@MainActivity, "Upload concluído!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    addLog("❌ Erro no upload: ${e.message}")
                }
            }
        }
    }

    private fun initializeApp() {
        addLog("Extraindo recursos Lnxiter...")
        mainScope.launch(Dispatchers.IO) {
            try {
                val assetsToCopy = arrayOf(
                    "lnxiter.sh", "lnxiter0.sh", "lnxiter1.sh", "lnxiter2.sh",
                    "lnxiter3.sh", "lnxiter6.sh", "lnxiter7.sh", "lnxiter8.sh",
                    "F.apk", "rish", "rish_shizuku.dex"
                )

                assetsToCopy.forEach { fileName ->
                    copyAssetToDir(fileName, WORKING_DIR)
                    setExecutePermission("$WORKING_DIR/$fileName")
                }

                withContext(Dispatchers.Main) {
                    tvStatus.text = "SISTEMA PRONTO"
                    progressBar.isIndeterminate = false
                    progressBar.progress = 100
                    addLog("Recursos prontos no diretório tmp.")
                }
            } catch (e: Exception) {
                addLog("Erro na extração: ${e.message}")
            }
        }
    }

    private fun applyBypassDima() {
        if (!ShizukuHelper.isShizukuAvailable() || !ShizukuHelper.hasShizukuPermission()) {
            showShizukuDeniedDialog()
            return
        }

        mainScope.launch {
            tvStatus.text = "EXECUTANDO..."
            progressBar.isIndeterminate = true
            btnBypassDima.isEnabled = false
            
            addLog("Iniciando sequência de segurança...")

            withContext(Dispatchers.IO) {
                addLog("> Desativando conexão...")
                ShizukuHelper.executeScript("sh $WORKING_DIR/lnxiter1.sh")
                delay(500)

                addLog("> Aplicando camuflagem...")
                ShizukuHelper.executeScript("sh $WORKING_DIR/lnxiter6.sh")

                addLog("> Executando Bypass de Sessão...")
                val res8 = ShizukuHelper.executeScript("sh $WORKING_DIR/lnxiter8.sh")
                addLog(res8)

                addLog("> Forçando assinatura Play Store...")
                ShizukuHelper.executeScript("sh $WORKING_DIR/lnxiter2.sh")

                addLog("> Injetando Payload...")
                ShizukuHelper.executeScript("sh $WORKING_DIR/lnxiter.sh")

                addLog("> Limpando rastros...")
                ShizukuHelper.executeScript("sh $WORKING_DIR/lnxiter7.sh")
                
                addLog("> Abrindo Free Fire Max...")
                ShizukuHelper.executeScript("sh $WORKING_DIR/lnxiter0.sh")
            }

            addLog("✅ Bypass finalizado!")
            tvStatus.text = "BYPASS ATIVO"
            progressBar.isIndeterminate = false
            btnBypassDima.isEnabled = true
        }
    }

    private fun copyAssetToDir(assetName: String, destDir: String) {
        try {
            val outFile = File(destDir, assetName)
            assets.open(assetName).use { input ->
                FileOutputStream(outFile).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao copiar $assetName: ${e.message}")
        }
    }

    private fun setExecutePermission(path: String) {
        try {
            Runtime.getRuntime().exec("chmod 777 $path")
        } catch (e: Exception) {
            Log.e(TAG, "Erro permissão: ${e.message}")
        }
    }

    private fun addLog(message: String) {
        runOnUiThread {
            tvLogs.append("\n> $message")
            logScroll.post { logScroll.fullScroll(View.FOCUS_DOWN) }
        }
    }

    private fun toggleHideStream() {
        isHideStreamOn = !isHideStreamOn
        if (isHideStreamOn) {
            btnHideStream.text = "HIDE STREAM: ON"
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            addLog("Gravação de tela bloqueada.")
        } else {
            btnHideStream.text = "HIDE STREAM: OFF"
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            addLog("Gravação de tela permitida.")
        }
    }

    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMS)
        } else {
            checkShizukuPermission()
        }
    }

    private fun checkShizukuPermission() {
        if (!ShizukuHelper.isShizukuAvailable()) {
            tvStatus.text = "SHIZUKU OFF"
            addLog("❌ Shizuku não está rodando.")
        } else if (ShizukuHelper.hasShizukuPermission()) {
            tvStatus.text = "SHIZUKU ATIVO"
            initializeApp()
        } else {
            Shizuku.addRequestPermissionResultListener(shizukuPermissionListener)
            Shizuku.requestPermission(REQUEST_PERMS)
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
        Shizuku.removeRequestPermissionResultListener(shizukuPermissionListener)
    }
}
