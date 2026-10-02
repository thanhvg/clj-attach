;; Paste these into the REPL one at a time (socket REPL via ./connect.sh,
;; or any editor connected to nREPL on port 7888).

(import '[demo Main Note NoteStore]
        '[com.sun.net.httpserver HttpHandler])

;; ---- 1. Look at the live state ---------------------------------------------
Main/REQUESTS                          ; AtomicLong, requests served so far
(.get Main/REQUESTS)
(.size Main/STORE)
(map #(.text %) (.all Main/STORE))     ; record accessors are just methods
(bean (.find Main/STORE 1))            ; Java object -> Clojure map
(Main/server)                          ; the HttpServer instance itself
(.. (Main/server) getAddress getPort)

;; ---- 2. Use Clojure's tools on Java data -----------------------------------
(->> (.all Main/STORE) (map bean) (map #(select-keys % [:id :text])))
(require '[clojure.reflect :as r] '[clojure.pprint :refer [print-table]])
(print-table (->> (r/reflect NoteStore) :members (filter :return-type)
                  (map #(select-keys % [:name :parameter-types :return-type]))))

;; ---- 3. Change behaviour of the running server -----------------------------
;; curl localhost:8080/hello  ->  "Howdy, world!"
(set! (. Main greeting) "Howdy")

;; mutate app state; then curl localhost:8080/notes
(.add Main/STORE "added from the REPL")

;; ---- 4. Add a brand-new endpoint without restarting ------------------------
;; curl localhost:8080/clj
(.put Main/ROUTES "/clj"
      (reify HttpHandler
        (handle [_ ex]
          (Main/respond ex 200 (str "live from Clojure, notes=" (.size Main/STORE) "\n")))))

;; ---- 5. Wrap an existing handler (poor man's middleware) -------------------
(let [orig (.get Main/ROUTES "/hello")]
  (.put Main/ROUTES "/hello"
        (reify HttpHandler
          (handle [_ ex]
            (println "GET /hello from" (str (.getRemoteAddress ex)))
            (.handle ^HttpHandler orig ex)))))
;; hit /hello again; the println shows up in the server's terminal.

;; ---- 6. Peek at the JVM -----------------------------------------------------
(count (Thread/getAllStackTraces))
(.. Runtime getRuntime totalMemory)
(into [] (.keySet Main/ROUTES))
