package demo;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Plain Java web server. No Clojure imports anywhere.
 *
 * Note the public static state: that is what makes it easy to reach from a REPL
 * running in the same JVM.
 */
public class Main {
    public static final NoteStore STORE = new NoteStore();
    public static final AtomicLong REQUESTS = new AtomicLong();

    /** Path prefix -> handler. Routes can be added while the server is running. */
    public static final Map<String, HttpHandler> ROUTES = new ConcurrentHashMap<>();

    public static volatile String greeting = "Hello";

    private static HttpServer server;

    public static HttpServer server() {
        return server;
    }

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;

        STORE.add("first note");
        STORE.add("second note");

        ROUTES.put("/hello", Main::hello);
        ROUTES.put("/notes", Main::notes);
        ROUTES.put("/stats", Main::stats);

        server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/", Main::dispatch);
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();
        System.out.println("Java server listening on http://127.0.0.1:" + port);
    }

    // ---- routing ---------------------------------------------------------

    private static void dispatch(HttpExchange ex) throws IOException {
        REQUESTS.incrementAndGet();
        String path = ex.getRequestURI().getPath();

        // longest matching prefix wins
        String best = null;
        for (String prefix : ROUTES.keySet()) {
            boolean match = path.equals(prefix) || path.startsWith(prefix + "/");
            if (match && (best == null || prefix.length() > best.length())) best = prefix;
        }
        if (best == null) {
            respond(ex, 404, "not found: " + path + "\n");
            return;
        }
        try {
            ROUTES.get(best).handle(ex);
        } catch (Exception e) {
            respond(ex, 500, "error: " + e + "\n");
        }
    }

    // ---- handlers --------------------------------------------------------

    private static void hello(HttpExchange ex) throws IOException {
        respond(ex, 200, greeting + ", world!\n");
    }

    private static void notes(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path = ex.getRequestURI().getPath();

        if (method.equals("POST") && path.equals("/notes")) {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8).trim();
            if (body.isEmpty()) { respond(ex, 400, "empty body\n"); return; }
            respond(ex, 201, STORE.add(body).toJson() + "\n");
        } else if (method.equals("GET") && path.equals("/notes")) {
            String json = STORE.all().stream().map(Note::toJson).collect(Collectors.joining(",", "[", "]"));
            respond(ex, 200, json + "\n");
        } else if (method.equals("GET") && path.startsWith("/notes/")) {
            Note n = STORE.find(Integer.parseInt(path.substring("/notes/".length())));
            if (n == null) respond(ex, 404, "no such note\n");
            else respond(ex, 200, n.toJson() + "\n");
        } else {
            respond(ex, 405, "method not allowed\n");
        }
    }

    private static void stats(HttpExchange ex) throws IOException {
        respond(ex, 200, "{\"requests\":" + REQUESTS.get() + ",\"notes\":" + STORE.size() + "}\n");
    }

    // ---- helpers ---------------------------------------------------------

    public static void respond(HttpExchange ex, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        ex.sendResponseHeaders(code, bytes.length);
        try (var out = ex.getResponseBody()) { out.write(bytes); }
    }
}
