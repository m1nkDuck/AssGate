#!/bin/sh
cd "$(dirname "$0")" || exit 1
if [ -n "${JAVA:-}" ]; then
    ashgate_java="$JAVA"
elif [ -n "${JAVA_HOME:-}" ]; then
    ashgate_java="$JAVA_HOME/bin/java"
else
    ashgate_java=java
fi
if ! command -v "$ashgate_java" >/dev/null 2>&1; then
    echo "Java was not found. Install Java 8+, or set JAVA / JAVA_HOME." >&2
    exit 1
fi
if [ ! -f tools/microemulator.jar ] || [ ! -f dist/AshGate.jar ] || [ ! -f dist/AshGate.jad ]; then
    echo "Missing emulator or game. Extract the complete AshGate package." >&2
    exit 1
fi
exec "$ashgate_java" -jar tools/microemulator.jar --resizableDevice 320 240 dist/AshGate.jad
