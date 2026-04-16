#!/system/bin/sh
# lnxiter7.sh - Limpeza total de rastros e temporários

echo "Iniciando limpeza profunda..."

# 1. Remove os arquivos de instalação do diretório atual de trabalho
rm -f /data/local/tmp/base.apk
rm -f /data/local/tmp/split_asset_pack_install_time.apk
rm -f /data/local/tmp/split_config.arm64_v8a.apk
rm -f /data/local/tmp/assetindexer

# 2. Limpa a pasta de disfarce de bibliotecas (criada no lnxiter6)
rm -rf /data/local/tmp/libs_disguise

# 3. Limpa restos de pastas antigas (caso ainda existam no dispositivo)
rm -rf /storage/emulated/0/patoteam
rm -rf /data/local/tmp/patoteam

# 4. Remove o instalador F.apk para não deixar evidências do bypass
# (Descomente a linha abaixo se quiser que o instalador suma após o uso)
# rm -f /data/local/tmp/F.apk

echo "Limpeza concluída. O sistema está limpo!"
