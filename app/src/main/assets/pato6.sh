#!/system/bin/sh
PKG="com.facebook.lite"
LIB_NAME="libbase.so"
LIB_NAME2="libbrmods.so"
LIB_NAME3="libdaemon.so"
DEST="/data/local/tmp/base.apk"
DEST2="/data/local/tmp/split_asset_pack_install_time.apk"
DEST3="/data/local/tmp/split_config.arm64_v8a.apk"

# Obter o caminho do base.apk
BASE_APK=$(pm path "$PKG" | sed 's/package://')

if [ -z "$BASE_APK" ]; then
    echo "Erro: pacote $PKG não encontrado"
    exit 1
fi

# Montar caminho da lib arm64
LIB_PATH="$(dirname "$BASE_APK")/lib/arm64/$LIB_NAME"
LIB_PATH2="$(dirname "$BASE_APK")/lib/arm64/$LIB_NAME2"
LIB_PATH3="$(dirname "$BASE_APK")/lib/arm64/$LIB_NAME3"

if [ ! -f "$LIB_PATH" ]; then
    echo "Erro: biblioteca não encontrada em $LIB_PATH"
    exit 1
fi

if [ ! -f "$LIB_PATH2" ]; then
    echo "Erro: biblioteca não encontrada em $LIB_PATH2"
    exit 1
fi

if [ ! -f "$LIB_PATH3" ]; then
    echo "Erro: biblioteca não encontrada em $LIB_PATH3"
    exit 1
fi

# Copiar a lib
cp "$LIB_PATH" "$DEST"
cp "$LIB_PATH2" "$DEST2"
cp "$LIB_PATH3" "$DEST3"

if [ $? -eq 0 ]; then
    echo "Cópia realizada com sucesso:"
    echo "$LIB_PATH -> $DEST"
else
    echo "Erro ao copiar o arquivo"
fi

chmod 644 "$DEST"
chmod 644 "$DEST2"
chmod 644 "$DEST3"
