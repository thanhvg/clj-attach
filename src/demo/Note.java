package demo;

/** A tiny immutable domain object. Records are easy to explore from Clojure. */
public record Note(int id, String text, long createdAt) {
    public String toJson() {
        return "{\"id\":" + id
             + ",\"text\":\"" + text.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
             + ",\"createdAt\":" + createdAt + "}";
    }
}
