#!/system/bin/sh
# Pato Universal v2 - Detecção Dinâmica e Instalação Condicional

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
    echo "Aviso: Free Fire não encontrado. Tentando instalar dependência..."
    # Se o FF não está instalado, a função principal pode ser instalar um mod/app auxiliar
    # Mantendo a lógica original de instalar o F.apk se o alvo principal não existe.
    if [ -f "/storage/emulated/0/patoteam/rish" ]; then
        sh /storage/emulated/0/patoteam/rish "/storage/emulated/0/patoteam/pato2.sh"
    else
        sh /storage/emulated/0/patoteam/pato2.sh
    fi
    exit 0
fi

echo "Alvo detectado: $PKG_TARGET"

# Caminho dinâmico para o diretório de assets do jogo
ASSET_DIR="/data/data/$PKG_TARGET/files/contentcache/Compulsory/android/gameassetbundles/avatar"

if [ ! -d "$ASSET_DIR" ]; then
    echo "Erro: Diretório de assets não encontrado em $ASSET_DIR"
    exit 1
fi

# Encontra o arquivo assetindexer dinamicamente
ASSET_FILE=$(ls "$ASSET_DIR" | grep -i "assetindexer" | head -n 1)

if [ -z "$ASSET_FILE" ]; then
    echo "Erro: Nenhum arquivo 'assetindexer' encontrado em $ASSET_DIR"
    exit 1
fi

SOURCE_PATH="$ASSET_DIR/$ASSET_FILE"
DEST_PATH="/storage/emulated/0/Android/data/$PKG_TARGET/"

echo "Copiando $SOURCE_PATH para $DEST_PATH"

# Cria o diretório de destino e executa a cópia com privilégios
mkdir -p "$DEST_PATH"
run-as "$PKG_TARGET" cp "$SOURCE_PATH" "$DEST_PATH"

if [ $? -eq 0 ]; then
    echo "Modificação aplicada com sucesso!"
else
    echo "Erro ao aplicar a modificação. Verifique as permissões do Shizuku."
fi
