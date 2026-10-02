# Java web server + Clojure REPL attached

A plain-Java web server (`src/demo/*.java`, no Clojure imports) launched *through*
`clojure.main`, so a REPL lives inside the same JVM and can inspect and change it live.

```
   one JVM process
 ┌──────────────────────────────────────────┐
 │ Java app (HttpServer :8080, NoteStore)   │
 │ Clojure runtime + socket REPL :5555      │
 │                 + nREPL :7888 (optional) │
 └──────────────────────────────────────────┘
```

## Run it

```bash
./fetch-deps.sh     # one-time: downloads Clojure + nREPL jars into ./lib
./run.sh            # compiles Java and starts everything
```
Needs a JDK (11+; tested on 21) and curl.

In another terminal:

```bash
curl localhost:8080/hello
curl localhost:8080/notes
curl -X POST -d 'my note' localhost:8080/notes
curl localhost:8080/stats

./connect.sh        # socket REPL (uses rlwrap if installed)
```
Then work through `explore.clj`.

## Editor connection (nicer than a raw terminal)

Connect to nREPL on `127.0.0.1:7888`:
- **Calva** (VS Code): "Connect to a Running REPL Server in the Project" -> Generic -> localhost:7888
- **CIDER**: `M-x cider-connect-clj` -> localhost 7888
- **Cursive**: Run > Edit Configurations > Clojure REPL > Remote

## Why it works

- `run.sh` starts the JVM with `-Dclojure.server.repl=...` and main class `clojure.main`,
  which initialises Clojure (so the property is honoured), then calls `demo.Main/main`.
- The Java app exposes `public static` state (`STORE`, `ROUTES`, `greeting`). The REPL
  is in the same JVM and classloader, so it can use them directly.

## Gotchas

- **Java classes can't be redefined from the REPL.** Edit, recompile, restart.
  (Clojure code and the objects/state you poke at are fully live.)
- Reaching private state needs `(set! (. obj field) ...)` on public fields, or reflection
  with `setAccessible`. Static singletons and getters make exploration much easier.
- The REPLs bind to 127.0.0.1 only. A REPL is arbitrary code execution; never expose it.
- `connect.sh` is a raw socket, so no completion or multi-line editing. Use nREPL + an editor for that.
