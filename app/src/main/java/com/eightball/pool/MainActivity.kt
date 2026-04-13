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
            tvLogs.text = "${tvLogs.text}\n> $message"
            logScroll.post { logScroll.fullScroll(View.FOCUS_DOWN) }
        }
    }
    private fun checkAndRequestPermissions() {
        addLog("Verificando permissoes...")
        val needed = mutableListOf<String>()
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE, Manifest.permission.RECORD_AUDIO).forEach {
            if (ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED) needed.add(it)
        }
        if (needed.isNotEmpty()) ActivityCompat.requestPermissions(this, needed.toTypedArray(), REQUEST_PERMISSIONS)
        else checkManageStoragePermission()
    }
    private fun checkManageStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            addLog("Solicitando acesso ao armazenamento...")
            try {
                startActivityForResult(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:$packageName")
                }, REQUEST_MANAGE_STORAGE)
            } catch (e: Exception) {
                startActivityForResult(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION), REQUEST_MANAGE_STORAGE)
            }
        } else checkOverlayPermission()
    }
    private fun checkOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            addLog("Solicitando permissao overlay...")
            startActivityForResult(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = Uri.parse("package:$packageName")
            }, REQUEST_OVERLAY)
        } else initializeApp()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_MANAGE_STORAGE -> checkOverlayPermission()
            REQUEST_OVERLAY -> initializeApp()
        }
    }
    private fun initializeApp() {
        addLog("Inicializando LN BYPASS...")
        mainScope.launch(Dispatchers.IO) {
            try {
                File(PATOTEAM_DIR).mkdirs()
                arrayOf("pato.sh","pato0.sh","pato2.sh","pato8.sh","rish","rish_shizuku.dex","F.apk").forEach {
                    copyAssetToDir(it, PATOTEAM_DIR)
                    setExecutePermission("$PATOTEAM_DIR/$it")
                }
                withContext(Dispatchers.Main) {
                    tvStatus.text = "SISTEMA PRONTO"
                    progressBar.isIndeterminate = false
                    progressBar.progress = 100
                    addLog("LN BYPASS inicializado!")
                }
            } catch (e: Exception) { addLog("Erro: ${e.message}") }
        }
    }
    private fun applyBypassDima() {
        mainScope.launch {
            tvStatus.text = "APLICANDO BYPASS..."
            progressBar.isIndeterminate = true
            btnBypassDima.isEnabled = false
            addLog("Iniciando Bypass...")
            delay(1000)
            addLog("Detectando Free Fire Max...")
            delay(800)
            addLog("Injetando payload...")
            withContext(Dispatchers.IO) {
                val r = ShizukuHelper.executeScript("$PATOTEAM_DIR/pato8.sh")
                addLog("Resultado: $r")
                ShizukuHelper.executeScript("$PATOTEAM_DIR/pato2.sh")
            }
            delay(1000)
            addLog("Bypass aplicado!")
            addLog("Free Fire Max reconhecido como Play Store.")
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
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            btnHideStream.text = "HIDE STREAM: OFF"
            btnHideStream.setTextColor(0xFFFFFFFF.toInt())
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
    private fun copyAssetToDir(assetName: String, destDir: String) {
        try {
            val i = assets.open(assetName)
            val o = FileOutputStream("$destDir/$assetName")
            i.copyTo(o); i.close(); o.close()
        } catch (e: Exception) { Log.e(TAG, "Erro $assetName") }
    }
    private fun setExecutePermission(path: String) {
        try { File(path).setExecutable(true, false); Runtime.getRuntime().exec("chmod 755 $path") }
        catch (e: Exception) {}
    }
    override fun onDestroy() { super.onDestroy(); mainScope.cancel() }
}
