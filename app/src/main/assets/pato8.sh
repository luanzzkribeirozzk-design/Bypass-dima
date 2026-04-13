#!/system/bin/sh
# Pato Universal - Instalação por Sessão Dinâmica

# --- Configuração ---
TEMP_DIR="/data/local/tmp/patoteam"
mkdir -p "$TEMP_DIR" && chmod 777 "$TEMP_DIR"

# --- Variáveis de Pacote ---
PKG_FF_MAX="com.dts.freefiremax"
PKG_FF_TH="com.dts.freefireth"
PKG_TARGET=""

# --- Funções ---
find_ff_package() {
    if pm list packages | grep -q "$PKG_FF_MAX"; then
        PKG_TARGET="$PKG_FF_MAX"
    elif pm list packages | grep -q "$PKG_FF_TH"; then
        PKG_TARGET="$PKG_FF_TH"
    fi
}

# --- Lógica Principal ---
find_ff_package

if [ -z "$PKG_TARGET" ]; then
    echo "Erro: Free Fire não encontrado."
    exit 1
fi

echo "Alvo detectado: $PKG_TARGET"

# Obtém o caminho do APK instalado dinamicamente
SOURCE_APK=$(pm list packages -f | grep "$PKG_TARGET" | head -n 1 | sed 's/package://' | cut -d'=' -f1)

if [ -z "$SOURCE_APK" ]; then
    echo "Erro: Não foi possível encontrar o caminho do APK para $PKG_TARGET."
    exit 1
fi

# Diretório raiz do APK instalado
ROOT_PATH="$(dirname "$SOURCE_APK")"

# Mapeia os arquivos de split (podem variar de nome, então usamos padrões)
BASE_APK="$TEMP_DIR/base.apk"
SPLIT_ABI="$TEMP_DIR/split_config.arm64_v8a.apk"
SPLIT_DPI="$TEMP_DIR/split_asset_pack_install_time.apk"

# Copia as partes do APK se existirem
cp "$ROOT_PATH/base.apk" "$BASE_APK" 2>/dev/null
cp "$ROOT_PATH/split_config.arm64_v8a.apk" "$SPLIT_ABI" 2>/dev/null
cp "$ROOT_PATH/split_asset_pack_install_time.apk" "$SPLIT_DPI" 2>/dev/null

# Verifica se o base.apk foi copiado
if [ ! -f "$BASE_APK" ]; then
    echo "Erro: Não foi possível copiar o base.apk de $ROOT_PATH."
    exit 1
fi

chmod 777 "$BASE_APK" "$SPLIT_ABI" "$SPLIT_DPI" 2>/dev/null

# Cria a sessão de instalação simulando a Google Play
SESSION=$(pm install-create -i com.android.vending -r | sed -n 's/.*\[\(.*\)\].*/\1/p')

if [ -z "$SESSION" ]; then
    echo "[!] Falha ao criar sessão de instalação."
    exit 1
fi

echo "Sessão criada: $SESSION"

# Escreve as partes na sessão
pm install-write "$SESSION" base "$BASE_APK" || exit 1
[ -f "$SPLIT_ABI" ] && pm install-write "$SESSION" abi "$SPLIT_ABI"
[ -f "$SPLIT_DPI" ] && pm install-write "$SESSION" dpi "$SPLIT_DPI"

# Finaliza a instalação
pm install-commit "$SESSION"

if [ $? -eq 0 ]; then
    echo "Instalação por sessão concluída com sucesso!"
else
    echo "Erro ao finalizar a instalação."
fi

# Limpeza
rm -rf "$TEMP_DIR"
