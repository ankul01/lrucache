package com.example.lrucache;

import java.util.HashMap;
import java.util.Map;

/**
 * Generic LRU (Least Recently Used) cache with O(1) get and put.
 *
 * Uses a HashMap for O(1) key lookup combined with a doubly linked list
 * to track access order. The most recently used node sits at the front;
 * the least recently used sits at the back and is evicted first.
 */
public class LRUCache<K, V> {

    private final int capacity;
    private final Map<K, Node<K, V>> map;
    private final Node<K, V> head; // dummy head (MRU side)
    private final Node<K, V> tail; // dummy tail (LRU side)

    // -------------------------------------------------------------------------
    // Node
    // -------------------------------------------------------------------------

    private static class Node<K, V> {
        private K key;
        private V value;
        private Node<K, V> prev;
        private Node<K, V> next;

        Node(K key, V value) {
            this.key   = key;
            this.value = value;
        }
    }

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    public LRUCache(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be a positive integer");
        }
        this.capacity = capacity;
        this.map      = new HashMap<>();

        // Sentinel nodes eliminate null-checks in add/remove helpers.
        head      = new Node<>(null, null);
        tail      = new Node<>(null, null);
        head.next = tail;
        tail.prev = head;
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Returns the value for {@code key}, or {@code null} if not present.
     * Marks the entry as most recently used.
     */
    public V get(K key) {
        Node<K, V> node = map.get(key);
        if (node == null) return null;
        moveToFront(node);
        return node.value;
    }

    /**
     * Inserts or updates {@code key → value}.
     * If the cache is full, the least recently used entry is evicted first.
     */
    public void put(K key, V value) {
        Node<K, V> node = map.get(key);
        if (node != null) {
            node.value = value;
            moveToFront(node);
            return;
        }

        if (map.size() == capacity) {
            Node<K, V> lru = tail.prev; // node just before dummy tail
            unlink(lru);
            map.remove(lru.key);
        }

        Node<K, V> fresh = new Node<>(key, value);
        map.put(key, fresh);
        linkAtFront(fresh);
    }

    /** Removes {@code key} from the cache. Returns true if it was present. */
    public boolean invalidate(K key) {
        Node<K, V> node = map.remove(key);
        if (node == null) return false;
        unlink(node);
        return true;
    }

    /** Returns true if the cache contains an entry for {@code key}, without affecting access order. */
    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    /** Number of entries currently in the cache. */
    public int size() {
        return map.size();
    }

    /** Maximum number of entries the cache can hold. */
    public int capacity() {
        return capacity;
    }

    /**
     * Returns a string showing entries from most- to least-recently used.
     * Example: {@code [c=3, b=2, a=1]}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        Node<K, V> cur = head.next;
        while (cur != tail) {
            sb.append(cur.key).append("=").append(cur.value);
            if (cur.next != tail) sb.append(", ");
            cur = cur.next;
        }
        return sb.append("]").toString();
    }

    // -------------------------------------------------------------------------
    // Doubly-linked list helpers
    // -------------------------------------------------------------------------

    /** Detach a node from wherever it currently sits. */
    private void unlink(Node<K, V> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    /** Insert a node immediately after the dummy head (MRU position). */
    private void linkAtFront(Node<K, V> node) {
        node.next       = head.next;
        node.prev       = head;
        head.next.prev  = node;
        head.next       = node;
    }

    private void moveToFront(Node<K, V> node) {
        unlink(node);
        linkAtFront(node);
    }
}
