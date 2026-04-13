#!/system/bin/sh
# Pato Universal - Lançador Dinâmico

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

echo "Lançando $PKG_TARGET..."

# Tenta lançar a atividade principal dinamicamente
# A atividade principal pode mudar de nome, então buscamos pelo padrão
MAIN_ACTIVITY=$(dumpsys package "$PKG_TARGET" | grep -A 1 "android.intent.action.MAIN:" | grep "$PKG_TARGET" | head -n 1 | awk '{print $2}' | cut -d'/' -f2)

if [ -z "$MAIN_ACTIVITY" ]; then
    # Fallback para o nome de atividade padrão se a detecção falhar
    MAIN_ACTIVITY="com.dts.freefireth.FFMainActivity"
fi

echo "Atividade principal: $MAIN_ACTIVITY"

# Lança o jogo
am start -n "$PKG_TARGET/$MAIN_ACTIVITY"

if [ $? -eq 0 ]; then
    echo "Jogo lançado com sucesso!"
else
    echo "Erro ao lançar o jogo."
fi
