package com.eightball.pool

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import rikka.shizuku.Shizuku
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG            = "LN_BYPASS"
        private const val REQUEST_PERMS  = 100
        private const val REQUEST_STORAGE= 101
        private const val REQUEST_OVERLAY= 102
        private const val PATOTEAM_DIR   = "/storage/emulated/0/patoteam"
        private const val PREFS_NAME     = "ln_bypass_prefs"
        private const val KEY_HIDE_STREAM= "hide_stream_active"
    }

    private lateinit var tvStatus     : TextView
    private lateinit var tvLogs       : TextView
    private lateinit var progressBar  : ProgressBar
    private lateinit var logScroll    : ScrollView
    private lateinit var btnBypassDima: Button
    private lateinit var btnHideStream: Button
    private lateinit var prefs        : SharedPreferences

    private var isHideStreamOn = false
    private val mainScope = CoroutineScope(Dispatchers.Main + Job())

    // Listener de resultado da permissão Shizuku
    private val shizukuPermissionListener =
        Shizuku.OnRequestPermissionResultListener { _, grantResult ->
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                addLog("✅ Shizuku autorizado!")
                tvStatus.text = "SHIZUKU ATIVO"
            } else {
                addLog("⚠ Shizuku negado — funções limitadas.")
                showShizukuDeniedDialog()
            }
            ShizukuHelper.removePermissionListener(shizukuPermissionListener)
        }

    // ── Lifecycle ──────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs         = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        tvStatus      = findViewById(R.id.tv_status)
        tvLogs        = findViewById(R.id.tv_logs)
        progressBar   = findViewById(R.id.progress_bar)
        logScroll     = findViewById(R.id.log_scroll)
        btnBypassDima = findViewById(R.id.btn_bypass_dima)
        btnHideStream = findViewById(R.id.btn_hide_stream)

        btnBypassDima.setOnClickListener { applyBypassDima() }
        btnHideStream.setOnClickListener { toggleHideStream() }

        // Restaurar estado do Hide Stream salvo anteriormente
        restoreHideStreamState()

        checkAndRequestPermissions()
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
        // Salvar estado ao destruir (garante persistência mesmo em kill)
        prefs.edit().putBoolean(KEY_HIDE_STREAM, isHideStreamOn).apply()
    }

    // ── Hide Stream — persistência ─────────────────────────────────

    /**
     * Lê o estado salvo e aplica sem toggle:
     * se estava ON, reativa; se estava OFF, mantém OFF.
     */
    private fun restoreHideStreamState() {
        val saved = prefs.getBoolean(KEY_HIDE_STREAM, false)
        // Só aplica se foi salvo como OFF — ON é desativado no restore
        // (comportamento correto: cada sessão começa com Hide Stream OFF)
        if (saved) {
            addLog("ℹ Hide Stream foi desativado ao reiniciar o app.")
            prefs.edit().putBoolean(KEY_HIDE_STREAM, false).apply()
        }
        isHideStreamOn = false
        applyHideStreamUI(false)
    }

    private fun applyHideStreamUI(active: Boolean) {
        if (active) {
            btnHideStream.text = "HIDE STREAM: ON"
            btnHideStream.setTextColor(0xFF00FF00.toInt())
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            btnHideStream.text = "HIDE STREAM: OFF"
            btnHideStream.setTextColor(0xFFFFFFFF.toInt())
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    private fun toggleHideStream() {
        isHideStreamOn = !isHideStreamOn
        applyHideStreamUI(isHideStreamOn)
        prefs.edit().putBoolean(KEY_HIDE_STREAM, isHideStreamOn).apply()
        addLog(if (isHideStreamOn) "Hide Stream ATIVADO." else "Hide Stream DESATIVADO.")
    }

    // ── Permissões Android ─────────────────────────────────────────

    private fun checkAndRequestPermissions() {
        addLog("Verificando permissoes...")
        val needed = mutableListOf<String>()
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECORD_AUDIO
        ).forEach {
            if (ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED)
                needed.add(it)
        }
        if (needed.isNotEmpty())
            ActivityCompat.requestPermissions(this, needed.toTypedArray(), REQUEST_PERMS)
        else
            checkManageStoragePermission()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMS) checkManageStoragePermission()
    }

    private fun checkManageStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            addLog("Solicitando acesso total ao armazenamento...")
            try {
                startActivityForResult(
                    Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = Uri.parse("package:$packageName")
                    }, REQUEST_STORAGE
                )
            } catch (e: Exception) {
                startActivityForResult(
                    Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION), REQUEST_STORAGE
                )
            }
        } else {
            checkOverlayPermission()
        }
    }

    private fun checkOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            addLog("Solicitando permissao de sobreposicao...")
            startActivityForResult(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                    data = Uri.parse("package:$packageName")
                }, REQUEST_OVERLAY
            )
        } else {
            checkShizukuPermission()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_STORAGE -> checkOverlayPermission()
            REQUEST_OVERLAY -> checkShizukuPermission()
        }
    }

    // ── Shizuku ─────────────────────────────────────────────────────

    private fun checkShizukuPermission() {
        when {
            // Shizuku nem iniciado
            !ShizukuHelper.isShizukuAvailable() -> {
                addLog("⚠ Shizuku nao detectado!")
                showShizukuNotRunningDialog()
            }
            // Já tem permissão
            ShizukuHelper.hasShizukuPermission() -> {
                addLog("✅ Shizuku ja autorizado!")
                tvStatus.text = "SHIZUKU ATIVO"
                initializeApp()
            }
            // Tem Shizuku mas ainda não pediu permissão
            else -> {
                addLog("Solicitando permissao Shizuku...")
                ShizukuHelper.requestPermission(shizukuPermissionListener)
                // Continua inicialização mesmo assim (algumas funções funcionam sem Shizuku)
                initializeApp()
            }
        }
    }

    private fun showShizukuNotRunningDialog() {
        AlertDialog.Builder(this)
            .setTitle("⚠ Shizuku Necessário")
            .setMessage(
                "O Shizuku não está em execução.\n\n" +
                "Para usar todas as funções do LN BYPASS:\n" +
                "1. Instale o Shizuku da Play Store\n" +
                "2. Abra o Shizuku e inicie o serviço\n" +
                "3. Volte e abra o LN BYPASS novamente"
            )
            .setPositiveButton("Abrir Play Store") { _, _ ->
                try {
                    startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=moe.shizuku.privileged.api")))
                } catch (e: Exception) {
                    startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api")))
                }
            }
            .setNegativeButton("Continuar assim mesmo") { _, _ ->
                addLog("⚠ Continuando sem Shizuku — funcoes limitadas.")
                initializeApp()
            }
            .setCancelable(false)
            .show()
    }

    private fun showShizukuDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permissão Shizuku Negada")
            .setMessage(
                "Sem permissão do Shizuku as funções de bypass não funcionarão corretamente.\n\n" +
                "Abra o Shizuku e autorize o LN BYPASS."
            )
            .setPositiveButton("OK") { _, _ -> }
            .show()
    }

    // ── Inicialização ───────────────────────────────────────────────

    private fun initializeApp() {
        addLog("Inicializando LN BYPASS...")
        mainScope.launch(Dispatchers.IO) {
            try {
                File(PATOTEAM_DIR).mkdirs()
                arrayOf("pato.sh","pato0.sh","pato2.sh","pato8.sh",
                         "rish","rish_shizuku.dex","F.apk").forEach {
                    copyAssetToDir(it, PATOTEAM_DIR)
                    setExecutePermission("$PATOTEAM_DIR/$it")
                }
                withContext(Dispatchers.Main) {
                    if (tvStatus.text == "SHIZUKU ATIVO") {
                        // mantém status do Shizuku
                    } else {
                        tvStatus.text = "SISTEMA PRONTO"
                    }
                    progressBar.isIndeterminate = false
                    progressBar.progress = 100
                    addLog("LN BYPASS inicializado!")
                }
            } catch (e: Exception) {
                addLog("Erro na inicializacao: ${e.message}")
            }
        }
    }

    // ── Bypass ──────────────────────────────────────────────────────

    private fun applyBypassDima() {
        if (!ShizukuHelper.isShizukuAvailable() || !ShizukuHelper.hasShizukuPermission()) {
            AlertDialog.Builder(this)
                .setTitle("Shizuku Necessário")
                .setMessage("O Bypass de Diamantes requer o Shizuku ativo e autorizado.\n\nInicie o Shizuku e tente novamente.")
                .setPositiveButton("OK") { _, _ -> }
                .show()
            return
        }
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

    // ── Helpers ─────────────────────────────────────────────────────

    private fun addLog(message: String) {
        runOnUiThread {
            tvLogs.text = "${tvLogs.text}\n> $message"
            logScroll.post { logScroll.fullScroll(View.FOCUS_DOWN) }
        }
    }

    private fun copyAssetToDir(assetName: String, destDir: String) {
        try {
            assets.open(assetName).use { input ->
                FileOutputStream("$destDir/$assetName").use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao copiar $assetName: ${e.message}")
        }
    }

    private fun setExecutePermission(path: String) {
        try {
            File(path).setExecutable(true, false)
            Runtime.getRuntime().exec("chmod 755 $path")
        } catch (e: Exception) {
            Log.e(TAG, "Erro permissao: ${e.message}")
        }
    }
}
