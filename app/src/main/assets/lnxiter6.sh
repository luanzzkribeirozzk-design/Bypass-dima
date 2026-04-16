#!/system/bin/sh
# lnxiter6.sh - Extrator de Libs para Disfarce (Versão Corrigida)

PKG="com.facebook.lite"
# Pasta de destino para não dar conflito com os APKs de instalação
LIB_DEST="/data/local/tmp/libs_disguise"

# Nomes das bibliotecas que seu mod usa
LIB_NAME="libbase.so"
LIB_NAME2="libbrmods.so"
LIB_NAME3="libdaemon.so"

echo "Iniciando extração de bibliotecas de disfarce..."

# Cria a pasta de destino se não existir
mkdir -p "$LIB_DEST"

# Obtém o caminho real onde o Facebook Lite está instalado
BASE_APK=$(pm path "$PKG" | sed 's/package://')

if [ -z "$BASE_APK" ]; then
    echo "Erro: Facebook Lite nao encontrado. Certifique-se de que ele esta instalado."
    exit 1
fi

# Monta o caminho das pastas de biblioteca (suporta arm64)
# dirname remove o "base.apk" do final do caminho retornado pelo 'pm path'
LIB_DIR="$(dirname "$BASE_APK")/lib/arm64"

# Executa a cópia das bibliotecas para a pasta de disfarce
# Usamos nomes diferentes dos APKs para evitar o erro de 'Pacote Inválido'
cp "$LIB_DIR/$LIB_NAME" "$LIB_DEST/disguise_base.so" 2>/dev/null
cp "$LIB_DIR/$LIB_NAME2" "$LIB_DEST/disguise_mod.so" 2>/dev/null
cp "$LIB_DIR/$LIB_NAME3" "$LIB_DEST/disguise_daemon.so" 2>/dev/null

# Ajusta as permissões para o sistema conseguir ler
chmod -R 755 "$LIB_DEST"

if [ -f "$LIB_DEST/disguise_base.so" ]; then
    echo "Disfarce extraído com sucesso em $LIB_DEST"
else
    echo "Erro: As bibliotecas nao foram encontradas no pacote $PKG."
    echo "Dica: Verifique se a arquitetura do Facebook Lite é arm64-v8a."
fi
