import java.util.ArrayList;

/**
 * Custom generic hash map keyed by String, implemented using separate chaining.
 *
 * Designed for fast ID-based lookups under large workloads.
 * Rehashing is triggered when the load factor exceeds a predefined threshold.
 *
 * Expected time complexity:
 * - put / get / contains / remove: O(1) average
 * - rehash: O(n)
 */
public class UserHashMap<V> {

    //wikipedia says best threshold for seperate chaining is between 1<th<3 so i pick 2.
    private static double loadFactorTH = 2.0;


    //i implemented my own linkedlist hence i didnt know i was able to import it in this project
    private static class Entry<V> {
        String key;
        V value;
        Entry<V> next;


        Entry(String key, V value, Entry<V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }


    private Entry<V>[] table;
    private int capacity;
    private int size;


    //consturactor
    public UserHashMap(int initialCapacity) {
        this.capacity = initialCapacity;
        this.table = (Entry<V>[]) new Entry[capacity];
        this.size = 0;
    }

    private int hash(String key) {
        int h = 0;
        int base = 31; // our slides say 31 is a great base and i liked that idea.
        for (int i = 0; i < key.length(); i++) {
            h = h * base + key.charAt(i);
        }

        if (h<0) h=-h;
        return h %capacity;

    }

    public void put(String key, V value) {
        int index = hash(key);
        Entry<V> head = table[index]; //bucket

        for (Entry<V> e = head; e != null; e = e.next) {
            if (e.key.equals(key)) {
                e.value = value;
                return;
            }
        }

        table[index] = new Entry<>(key, value, head);
        size++;

        if (loadFactor() > loadFactorTH) {
            rehash();
        }
    }

    public V get(String key) {
        int index = hash(key);
        for (Entry<V> e = table[index]; e != null; e = e.next) {
            if (e.key.equals(key)) {
                return e.value;
            }
        }
        return null;
    }


    public boolean contains(String key){
        int index = hash(key);
        Entry<V> head=  table[index];
        for(Entry<V> e = head ; e!=null ; e=e.next){
            if(e.key.equals(key)) return true;
        }
        return false;
    }


    public ArrayList<String> keys() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < capacity; i++) {
            Entry<V> e = table[i];
            while (e != null) {
                list.add(e.key);
                e = e.next;
            }
        }
        return list;
    }

    public ArrayList<V> values() {
        ArrayList<V> list = new ArrayList<>();
        for (int i = 0; i < capacity; i++) {
            Entry<V> e = table[i];
            while (e != null) {
                list.add(e.value);
                e = e.next;
            }
        }
        return list;
    }

    public double loadFactor() {
        return (double) size / capacity;
    }

    private void rehash() {
        Entry<V>[] oldTable = table;
        int oldCapacity = capacity;

        capacity = capacity * 2;
        table = (Entry<V>[]) new Entry[capacity];
        size = 0;

        for (int i = 0; i < oldCapacity; i++) {
            for (Entry<V> e = oldTable[i]; e != null; e = e.next) {
                put(e.key, e.value);
            }
        }
    }
    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public V remove(String key) {
        int index = hash(key);
        Entry<V> prev = null;
        Entry<V> curr = table[index];

        while (curr != null) {
            if (curr.key.equals(key)) {
                if (prev == null) {
                    table[index] = curr.next;
                } else {
                    prev.next = curr.next;
                }
                size--;
                return curr.value;
            }
            prev = curr;
            curr = curr.next;
        }
        return null;
    }



}
