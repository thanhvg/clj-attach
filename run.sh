#!/usr/bin/env bash
# Compile the Java app, then launch it THROUGH clojure.main so that a socket REPL
# (port 5555) and nREPL (port 7888) live inside the same JVM as the web server (port 8080).
set -euo pipefail
cd "$(dirname "$0")"

LIBDIR="${LIBDIR:-lib}"          # override to use jars from somewhere else
mkdir -p out
javac -d out src/demo/*.java

exec java -cp "out:$LIBDIR/*" \
  -Dclojure.server.repl='{:port 5555 :accept clojure.core.server/repl}' \
  clojure.main -e "
    (try (require 'nrepl.server)
         ((resolve 'nrepl.server/start-server) :port 7888 :bind \"127.0.0.1\")
         (println \"nREPL on 7888\")
         (catch java.io.FileNotFoundException _ (println \"nREPL jar not found, skipping\")))
    (demo.Main/main (into-array String []))
  "
