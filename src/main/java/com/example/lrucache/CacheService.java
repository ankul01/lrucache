package com.example.lrucache;

import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Thread-safe wrapper around LRUCache<String, String>.
 *
 * LRUCache is not thread-safe: every operation (including get) mutates the
 * doubly-linked list to update access order. All methods are synchronized on
 * "this" so only one HTTP thread accesses the cache at a time.
 */
@Service
public class CacheService {

    private final LRUCache<String, String> cache;

    public CacheService(LRUCache<String, String> cache) {
        this.cache = cache;
    }

    /** Returns the value for key, or empty if not present. Marks entry as MRU. */
    public synchronized Optional<String> get(String key) {
        return Optional.ofNullable(cache.get(key));
    }

    /**
     * Inserts or updates an entry. Returns true if created, false if updated.
     */
    public synchronized boolean create(String key, String value) {
        boolean isNew = !cache.containsKey(key);
        cache.put(key, value);
        return isNew;
    }

    /**
     * Updates an existing entry. Returns false (→ 404) if the key is not present.
     */
    public synchronized boolean update(String key, String value) {
        if (!cache.containsKey(key)) return false;
        cache.put(key, value);
        return true;
    }

    /** Removes the key. Returns true if it was present. */
    public synchronized boolean invalidate(String key) {
        return cache.invalidate(key);
    }

    public synchronized int size() { return cache.size(); }
    public int capacity()          { return cache.capacity(); }
}
