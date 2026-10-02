package demo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** In-memory store. Nothing here knows about Clojure. */
public class NoteStore {
    private final Map<Integer, Note> notes = new ConcurrentHashMap<>();
    private final AtomicInteger ids = new AtomicInteger();

    public Note add(String text) {
        Note n = new Note(ids.incrementAndGet(), text, System.currentTimeMillis());
        notes.put(n.id(), n);
        return n;
    }

    /** Returns null when missing. */
    public Note find(int id) {
        return notes.get(id);
    }

    public List<Note> all() {
        List<Note> list = new ArrayList<>(notes.values());
        list.sort(Comparator.comparingInt(Note::id));
        return list;
    }

    public int size() {
        return notes.size();
    }
}
