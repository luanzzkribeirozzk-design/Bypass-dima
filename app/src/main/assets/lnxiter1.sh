#!/system/bin/sh

echo "Desconectando internet para bypass..."

# Desativa Dados Móveis
svc data disable
# Desativa Wi-Fi
svc wifi disable

echo "Conexão interrompida."
