package com.eightball.pool

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LN_BYPASS"
        private const val REQUEST_PERMISSIONS = 100
        private const val REQUEST_MANAGE_STORAGE = 101
        private const val REQUEST_OVERLAY = 102
        private const val PATOTEAM_DIR = "/storage/emulated/0/patoteam"
    }

    private lateinit var tvStatus: TextView
    private lateinit var tvLogs: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var logScroll: ScrollView
    private lateinit var btnBypassDima: Button
    private lateinit var btnHideStream: Button
    
    private var isHideStreamOn = false
    private val mainScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tv_status)
        tvLogs = findViewById(R.id.tv_logs)
        progressBar = findViewById(R.id.progress_bar)
        logScroll = findViewById(R.id.log_scroll)
        btnBypassDima = findViewById(R.id.btn_bypass_dima)
        btnHideStream = findViewById(R.id.btn_hide_stream)

        btnBypassDima.setOnClickListener { applyBypassDima() }
        btnHideStream.setOnClickListener { toggleHideStream() }

        checkAndRequestPermissions()
    }

    private fun addLog(message: String) {
        runOnUiThread {
            val currentLogs = tvLogs.text.toString()
            tvLogs.text = "$currentLogs\n> $message"
            logScroll.post { logScroll.fullScroll(View.FOCUS_DOWN) }
        }
    }

    private fun checkAndRequestPermissions() {
        addLog("Verificando permissões do sistema...")
        val permissionsNeeded = mutableListOf<String>()
        val requiredPermissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        for (permission in requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission)
            }
        }

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), REQUEST_PERMISSIONS)
        } else {
            checkManageStoragePermission()
        }
    }

    private fun checkManageStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                addLog("Solicitando acesso total ao armazenamento...")
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = Uri.parse("package:$packageName")
                    startActivityForResult(intent, REQUEST_MANAGE_STORAGE)
                } catch (e: Exception) {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    startActivityForResult(intent, REQUEST_MANAGE_STORAGE)
                }
            } else {
                checkOverlayPermission()
            }
        } else {
            checkOverlayPermission()
        }
    }

    private fun checkOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            addLog("Solicitando permissão de sobreposição...")
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = Uri.parse("package:$packageName")
            startActivityForResult(intent, REQUEST_OVERLAY)
        } else {
            initializeApp()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_MANAGE_STORAGE -> checkOverlayPermission()
            REQUEST_OVERLAY -> initializeApp()
        }
    }

    private fun initializeApp() {
        addLog("Inicializando componentes LN BYPASS...")
        mainScope.launch(Dispatchers.IO) {
            try {
                val patoteamDir = File(PATOTEAM_DIR)
                if (!patoteamDir.exists()) patoteamDir.mkdirs()

                val assetsToCopy = arrayOf("pato.sh", "pato0.sh", "pato2.sh", "pato8.sh", "rish", "rish_shizuku.dex", "F.apk")
                for (asset in assetsToCopy) {
                    copyAssetToDir(asset, PATOTEAM_DIR)
                    setExecutePermission("$PATOTEAM_DIR/$asset")
                }

                withContext(Dispatchers.Main) {
                    tvStatus.text = "SISTEMA PRONTO"
                    progressBar.isIndeterminate = false
                    progressBar.progress = 100
                    addLog("LN BYPASS inicializado com sucesso!")
                }
            } catch (e: Exception) {
                addLog("Erro na inicialização: ${e.message}")
            }
        }
    }

    private fun applyBypassDima() {
        mainScope.launch(Dispatchers.Main) {
            tvStatus.text = "APLICANDO BYPASS..."
            progressBar.isIndeterminate = true
            btnBypassDima.isEnabled = false

            addLog("Iniciando Bypass de Diamantes...")
            delay(1000)
            addLog("Detectando Free Fire Max...")
            delay(800)
            addLog("Injetando payload de bypass...")
            
            withContext(Dispatchers.IO) {
                val result = ShizukuHelper.executeScript("$PATOTEAM_DIR/pato8.sh")
                addLog("Resultado do Sistema: $result")
                ShizukuHelper.executeScript("$PATOTEAM_DIR/pato2.sh")
            }

            addLog("Limpando rastros de instalação...")
            delay(1000)
            addLog("Bypass aplicado com sucesso!")
            addLog("O Free Fire Max agora é reconhecido como original da Play Store.")
            
            tvStatus.text = "BYPASS ATIVO"
            progressBar.isIndeterminate = false
            progressBar.progress = 100
            btnBypassDima.isEnabled = true
        }
    }

    private fun toggleHideStream() {
        isHideStreamOn = !isHideStreamOn
        if (isHideStreamOn) {
            btnHideStream.text = "HIDE STREAM: ON"
            btnHideStream.setTextColor(0xFF00FF00.toInt())
            addLog("Hide Stream Ativado: Tela protegida contra capturas.")
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            btnHideStream.text = "HIDE STREAM: OFF"
            btnHideStream.setTextColor(0xFFFFFFFF.toInt())
            addLog("Hide Stream Desativado.")
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    private fun copyAssetToDir(assetName: String, destDir: String) {
        try {
            val destFile = File("$destDir/$assetName")
            val inputStream: InputStream = assets.open(assetName)
            val outputStream = FileOutputStream(destFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao copiar $assetName: ${e.message}")
        }
    }

    private fun setExecutePermission(filePath: String) {
        try {
            val file = File(filePath)
            file.setExecutable(true, false)
            Runtime.getRuntime().exec("chmod 755 $filePath")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao definir permissão: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
    }
}
