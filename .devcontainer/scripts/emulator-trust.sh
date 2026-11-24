#!/usr/bin/env bash
# Waits for AWS Emulator, then builds a JDK trust store that contains the
# default CAs *plus* the emulator's per-run CA (the emulator's own
# truststore.p12 would replace the defaults and break Maven/Gradle/etc.).
set -euo pipefail

PORT=9297
DIR="$HOME/.aws/emulator/$PORT"
OUT="$HOME/.aws/emulator-trust/truststore.p12"

for _ in $(seq 60); do
  curl -sf --cacert "$DIR/ca.pem" "https://127.0.0.1:$PORT/__admin/health" >/dev/null && break
  sleep 1
done
curl -sf --cacert "$DIR/ca.pem" "https://127.0.0.1:$PORT/__admin/health" >/dev/null \
  || { echo "AWS Emulator did not become healthy on :$PORT" >&2; exit 1; }

if command -v keytool >/dev/null; then
  JAVA_BIN="$(dirname "$(readlink -f "$(command -v java)")")"
  CACERTS="$JAVA_BIN/../lib/security/cacerts"
  mkdir -p "$(dirname "$OUT")"; rm -f "$OUT"
  # Run keytool without the JAVA_TOOL_OPTIONS that points at the (not yet built) store.
  env -u JAVA_TOOL_OPTIONS keytool -importkeystore -noprompt -srckeystore "$CACERTS" -srcstorepass changeit \
    -destkeystore "$OUT" -deststoretype PKCS12 -deststorepass changeit >/dev/null
  env -u JAVA_TOOL_OPTIONS keytool -importcert -noprompt -alias aws-emulator -file "$DIR/ca.pem" \
    -keystore "$OUT" -storetype PKCS12 -storepass changeit >/dev/null
fi
echo "AWS Emulator ready at https://127.0.0.1:$PORT (trust store: $OUT)"
