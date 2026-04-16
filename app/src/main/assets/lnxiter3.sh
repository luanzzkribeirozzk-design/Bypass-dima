#!/system/bin/sh

echo "Reconectando internet..."

# Ativa Dados Móveis
svc data enable
# Ativa Wi-Fi
svc wifi enable

echo "Conexão restaurada."
