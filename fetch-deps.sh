#!/usr/bin/env bash
# Downloads Clojure (+ its two required deps) from Maven Central and nREPL from Clojars into ./lib
set -euo pipefail
cd "$(dirname "$0")"
mkdir -p lib

CENTRAL=https://repo1.maven.org/maven2
CLOJARS=https://repo.clojars.org      # nREPL lives here, NOT on Maven Central

get() {  # get <base-url> <path>
    local url="$1/$2" dest="lib/$(basename "$2")"
    [ -f "$dest" ] && return 0
    echo "fetching $url"
    curl -fsSL -o "$dest" "$url" || { echo "FAILED: $url" >&2; rm -f "$dest"; exit 1; }
}

get $CENTRAL org/clojure/clojure/1.12.0/clojure-1.12.0.jar
get $CENTRAL org/clojure/spec.alpha/0.5.238/spec.alpha-0.5.238.jar
get $CENTRAL org/clojure/core.specs.alpha/0.4.74/core.specs.alpha-0.4.74.jar
get $CLOJARS nrepl/nrepl/1.3.1/nrepl-1.3.1.jar   # optional: run.sh works without it

echo "ok: $(ls lib | wc -l) jars in ./lib"
