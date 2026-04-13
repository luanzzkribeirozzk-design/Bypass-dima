#!/system/bin/sh
# Pato Universal - Instalação com Bypass de UID Dinâmico

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
    echo "Erro: Free Fire não encontrado para o bypass de UID."
    exit 1
fi

echo "Alvo detectado: $PKG_TARGET"

# Copia o APK para o diretório temporário
cp /storage/emulated/0/patoteam/F.apk /data/local/tmp/F.apk

# Obtém o UID do pacote alvo dinamicamente
UID_TARGET=$(dumpsys package "$PKG_TARGET" | grep userId= | awk -F'userId=' '{print $2}' | awk '{print $1}')

if [ -z "$UID_TARGET" ]; then
    echo "Erro: Não foi possível obter o UID do pacote $PKG_TARGET."
    exit 1
fi

echo "UID detectado: $UID_TARGET"

# Monta o payload de instalação com o UID dinâmico
PAYLOAD="@null
victim \"$UID_TARGET\" 1 /data/data/$PKG_TARGET default:targetSdkVersion=28 none 0 0 1 @null"

# Executa a instalação com o payload
pm install -i "$PAYLOAD" /data/local/tmp/F.apk

if [ $? -eq 0 ]; then
    echo "Instalação concluída com sucesso!"
else
    echo "Erro na instalação. Verifique as permissões do Shizuku."
fi

sleep 0.5
