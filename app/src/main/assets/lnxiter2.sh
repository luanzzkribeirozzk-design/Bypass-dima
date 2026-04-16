#!/system/bin/sh

# 1. Define o local do APK (O seu app vai copiar ele para o tmp primeiro)
APK_PATH="/data/local/tmp/F.apk"
# 2. Define o pacote do jogo
PKG="com.dts.freefiremax"

echo "Iniciando instalador Lnxiter com bypass de loja..."

# O comando real que faz a mágica:
# -r (reinstalar mantendo dados)
# -i com.android.vending (Diz ao Android que a Play Store instalou)
pm install -r -i com.android.vending "$APK_PATH"

if [ $? -eq 0 ]; then
    echo "Sucesso: Instalado como oficial da Play Store!"
else
    echo "Erro: Falha na instalacao. Verifique se o F.apk esta em /data/local/tmp/"
fi

sleep 0.5
