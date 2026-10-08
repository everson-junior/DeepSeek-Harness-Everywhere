#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

VERSION="1.0.0"
INSTALL=false

# Processar argumentos
for arg in "$@"; do
    if [[ "$arg" == "--install" ]] || [[ "$arg" == "-i" ]]; then
        INSTALL=true
    elif [[ "$arg" =~ ^[0-9]+\.[0-9]+\.[0-9]+ ]]; then
        VERSION="$arg"
    fi
done

echo "=========================================================="
echo " Building Everywhere (DeepSeek Harness) for Eclipse IDE   "
echo " Target Version: $VERSION                                "
echo "=========================================================="

BUILD_DIR="$SCRIPT_DIR/build"
BIN_DIR="$SCRIPT_DIR/bin"
JAR_NAME="everywhere-eclipse-${VERSION}.jar"
TARGET_JAR="$BUILD_DIR/$JAR_NAME"

mkdir -p "$BIN_DIR" "$BUILD_DIR"

# 1. Localizar compilador Java (JRE bundled no Eclipse ou JDK no PATH)
JAVAC=""
JAR_BIN=""

# Tenta encontrar primeiro o compilador do próprio Eclipse (JustJ Java 25)
JUSTJ_JAVAC=$(find /snap/eclipse -path "*/jre/bin/javac" 2>/dev/null | head -n 1 || true)
if [ -n "$JUSTJ_JAVAC" ] && [ -x "$JUSTJ_JAVAC" ]; then
    JAVAC="$JUSTJ_JAVAC"
    JAR_BIN="$(dirname "$JAVAC")/jar"
elif command -v javac >/dev/null 2>&1 && command -v jar >/dev/null 2>&1; then
    JAVAC="$(command -v javac)"
    JAR_BIN="$(command -v jar)"
elif [ -f "/home/linux/.local/jdk-17/bin/javac" ]; then
    JAVAC="/home/linux/.local/jdk-17/bin/javac"
    JAR_BIN="/home/linux/.local/jdk-17/bin/jar"
fi

if [ -z "$JAVAC" ] || [ ! -x "$JAVAC" ]; then
    echo "ERRO: Compilador javac não encontrado."
    exit 1
fi

echo "-> Compilador: $JAVAC"
echo "-> Ferramenta JAR: $JAR_BIN"

# 2. Montar Classpath a partir dos plugins do Eclipse
ECLIPSE_PLUGINS_DIR=""
if [ -n "$ECLIPSE_HOME" ]; then
    if [ -d "$ECLIPSE_HOME/plugins" ]; then
        ECLIPSE_PLUGINS_DIR="$ECLIPSE_HOME/plugins"
    elif [ -d "$ECLIPSE_HOME" ]; then
        ECLIPSE_PLUGINS_DIR="$ECLIPSE_HOME"
    fi
fi

if [ -z "$ECLIPSE_PLUGINS_DIR" ] || [ ! -d "$ECLIPSE_PLUGINS_DIR" ]; then
    for candidate in \
        "/snap/eclipse/current/plugins" \
        "/snap/eclipse/150/plugins" \
        "/opt/eclipse/plugins" \
        "/usr/lib/eclipse/plugins" \
        "/usr/share/eclipse/plugins"; do
        if [ -d "$candidate" ]; then
            ECLIPSE_PLUGINS_DIR="$candidate"
            break
        fi
    done
fi

if [ -z "$ECLIPSE_PLUGINS_DIR" ] || [ ! -d "$ECLIPSE_PLUGINS_DIR" ]; then
    echo "ERRO: Diretório de plugins do Eclipse não encontrado. Defina a variável ECLIPSE_HOME."
    exit 1
fi

echo "-> Montando classpath com plugins de: $ECLIPSE_PLUGINS_DIR"
CP=""
for jar in "$ECLIPSE_PLUGINS_DIR"/org.eclipse.*.jar; do
    CP="$CP:$jar"
done

# 3. Atualizar versão no MANIFEST.MF
sed -i "s/Bundle-Version: .*/Bundle-Version: ${VERSION}/" META-INF/MANIFEST.MF

# 4. Compilar código fonte
echo "-> Compilando fontes Java..."
rm -rf "$BIN_DIR"/*
SOURCES=$(find src -name "*.java")
"$JAVAC" -encoding UTF-8 -d "$BIN_DIR" -cp "$CP" $SOURCES

# 5. Empacotar JAR OSGi
echo "-> Empacotando bundle OSGi ($JAR_NAME)..."
rm -f "$BUILD_DIR"/*.jar
"$JAR_BIN" cvfm "$TARGET_JAR" META-INF/MANIFEST.MF -C "$BIN_DIR" . -C . plugin.xml -C . icons

echo "-> Bundle gerado com sucesso em: $TARGET_JAR"

# 6. Instalação se solicitado
if [ "$INSTALL" = true ]; then
    echo "-> Instalando no Eclipse local..."
    INSTALL_DIR="/home/linux/snap/eclipse/common/eclipse/dropins"
    mkdir -p "$INSTALL_DIR"
    cp -f "$TARGET_JAR" "$INSTALL_DIR/"

    PLUGIN_INSTALL_DIR="/home/linux/snap/eclipse/common/eclipse/plugins"
    mkdir -p "$PLUGIN_INSTALL_DIR"
    cp -f "$TARGET_JAR" "$PLUGIN_INSTALL_DIR/"

    CONFIG_INI="/home/linux/snap/eclipse/common/eclipse/configuration/config.ini"
    if [ -f "$CONFIG_INI" ]; then
        if ! grep -q "org.eclipse.equinox.p2.reconciler.dropins.directory" "$CONFIG_INI"; then
            echo "org.eclipse.equinox.p2.reconciler.dropins.directory=/home/linux/snap/eclipse/common/eclipse/dropins" >> "$CONFIG_INI"
            echo "-> Diretório dropins configurado no config.ini do Eclipse!"
        fi
    fi

    BUNDLES_INFO="/home/linux/snap/eclipse/common/eclipse/configuration/org.eclipse.equinox.simpleconfigurator/bundles.info"
    if [ -f "$BUNDLES_INFO" ]; then
        sed -i '/com\.deepseek\.everywhere/d' "$BUNDLES_INFO"
        echo "com.deepseek.everywhere,$VERSION,file:$PLUGIN_INSTALL_DIR/$JAR_NAME,4,false" >> "$BUNDLES_INFO"
        echo "-> Bundle registrado no bundles.info do Eclipse com URI absoluta!"
    fi
    echo "-> Instalação concluída no Eclipse! Reinicie o Eclipse para aplicar."
fi

echo "=========================================================="
echo " Build concluído com sucesso!                             "
echo "=========================================================="
