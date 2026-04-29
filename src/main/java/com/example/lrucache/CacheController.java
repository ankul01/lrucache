package com.example.lrucache;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/cache")
public class CacheController {

    private final CacheService cacheService;

    public CacheController(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    record PostRequest(String key, String value) {}
    record PutRequest(String value) {}

    // ── GET /api/cache/stats ──────────────────────────────────────────────────

    /**
     * 200 OK  {"size": 2, "capacity": 100}
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Integer>> stats() {
        return ResponseEntity.ok(Map.of(
                "size",     cacheService.size(),
                "capacity", cacheService.capacity()
        ));
    }

    // ── POST /api/cache ───────────────────────────────────────────────────────

    /**
     * Creates a new cache entry. Rejects if the key already exists.
     *
     * Request body: {"key":"k","value":"v"}
     * 201 Created   {"key":"k","value":"v"}
     * 409 Conflict  — key already exists
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> create(@RequestBody PostRequest body) {
        if (body.key() == null || body.value() == null) {
            return ResponseEntity.badRequest().build();
        }
        boolean created = cacheService.create(body.key(), body.value());
        return ResponseEntity.status(created ? HttpStatus.CREATED : HttpStatus.OK)
                .body(Map.of("key", body.key(), "value", body.value()));
    }

    // ── GET /api/cache/{key} ─────────────────────────────────────────────────

    /**
     * Fetches an entry by key. Marks it as most recently used on hit.
     *
     * 200 OK        {"key":"k","value":"v"}
     * 404 Not Found
     */
    @GetMapping("/{key}")
    public ResponseEntity<Map<String, String>> get(@PathVariable String key) {
        Optional<String> value = cacheService.get(key);
        if (value.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("key", key, "value", value.get()));
    }

    // ── PUT /api/cache/{key} ─────────────────────────────────────────────────

    /**
     * Updates the value of an existing entry. Rejects if the key does not exist.
     *
     * Request body: {"value":"v"}
     * 200 OK        {"key":"k","value":"v"}
     * 404 Not Found — key does not exist
     */
    @PutMapping("/{key}")
    public ResponseEntity<Map<String, String>> update(
            @PathVariable String key,
            @RequestBody PutRequest body) {

        if (body.value() == null) {
            return ResponseEntity.badRequest().build();
        }
        boolean updated = cacheService.update(key, body.value());
        if (!updated) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("key", key, "value", body.value()));
    }

    // ── DELETE /api/cache/{key} ──────────────────────────────────────────────

    /**
     * Removes an entry from the cache.
     *
     * 204 No Content — key existed and was removed
     * 404 Not Found  — key was not present
     */
    @DeleteMapping("/{key}")
    public ResponseEntity<Void> delete(@PathVariable String key) {
        boolean removed = cacheService.invalidate(key);
        return removed
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
