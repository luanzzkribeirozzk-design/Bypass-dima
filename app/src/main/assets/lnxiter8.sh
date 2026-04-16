#!/system/bin/sh
# lnxiter8.sh - Reinstalação por Sessão (Bypass Total)

# ===== CONFIGURAÇÃO =====
# Usamos direto o local onde a MainActivity extraiu os arquivos
TMP_DIR="/data/local/tmp"
BASE_APK="$TMP_DIR/base.apk"
SPLIT_ABI="$TMP_DIR/split_config.arm64_v8a.apk"
SPLIT_DPI="$TMP_DIR/split_asset_pack_install_time.apk"

PKG="com.dts.freefiremax"

echo "Iniciando Bypass de Sessão Shizuku..."

# 1. Verifica se os arquivos necessários existem no /tmp
if [ ! -f "$BASE_APK" ]; then
    echo "Erro: base.apk não encontrado em $TMP_DIR"
    exit 1
fi

# 2. Garante permissão de leitura para o instalador do sistema
chmod 777 "$BASE_APK" "$SPLIT_ABI" "$SPLIT_DPI"

echo "Criando sessão de instalação oficial (Play Store)..."
# Cria a sessão fingindo ser o instalador oficial (com.android.vending)
SESSION=$(pm install-create -i com.android.vending -r | sed -n 's/.*\[\(.*\)\].*/\1/p')

if [ -z "$SESSION" ]; then
    echo "Erro: Não foi possível criar a sessão de instalação."
    exit 1
fi

echo "Sessão ID: $SESSION"

# 3. Escreve os arquivos na sessão
echo "Adicionando Base..."
pm install-write "$SESSION" base.apk "$BASE_APK" || exit 1

echo "Adicionando Configs..."
pm install-write "$SESSION" split_config "$SPLIT_ABI" || exit 1
pm install-write "$SESSION" split_dpi "$SPLIT_DPI" || exit 1

# 4. Finaliza e aplica a instalação
echo "Commitando instalação..."
pm install-commit "$SESSION"

if [ $? -eq 0 ]; then
    echo "✅ Bypass de sessão concluído com sucesso!"
else
    echo "❌ Erro ao finalizar instalação."
    exit 1
fi
 concluído!"
