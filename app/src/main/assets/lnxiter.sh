#!/system/bin/sh
# lnxiter.sh - Injetor de AssetIndexer (Coração do Bypass)

TARGET_PKG="com.dts.freefiremax"
FILE_NAME="assetindexer"
# Local onde a MainActivity extraiu o arquivo
TMP_FILE="/data/local/tmp/$FILE_NAME"

echo "Iniciando injeção Lnxiter..."

# 1. Verifica se o arquivo assetindexer existe no /tmp
if [ ! -f "$TMP_FILE" ]; then
    echo "Erro: Arquivo $FILE_NAME não encontrado em /data/local/tmp"
    exit 1
fi

# 2. Verifica se o jogo está instalado
if pm list packages | grep -q "$TARGET_PKG"; then
    echo "Alvo detectado: Free Fire Max"
    
    # 3. Injeção via run-as (Método seguro Shizuku)
    # Move o arquivo para a pasta interna de arquivos do jogo
    cat "$TMP_FILE" | run-as $TARGET_PKG sh -c "cat > files/$FILE_NAME"
    
    # 4. Ajusta a permissão dentro da pasta do jogo
    run-as $TARGET_PKG sh -c "chmod 644 files/$FILE_NAME"
    
    echo "✅ Injeção concluída com sucesso!"
else
    echo "❌ Erro: Free Fire Max não está instalado."
    exit 1
fi
h
fi
