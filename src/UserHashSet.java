/**
 * Custom hash set implementation backed by UserHashMap.
 *
 * Stores only unique keys and is primarily used for
 * customer-level freelancer blacklisting with expected O(1) access.
 */

public class UserHashSet {

    private UserHashMap<Object> map;

    //consturactor
    public UserHashSet(int initialCapacity) {
        this.map = new UserHashMap<>(initialCapacity);
    }

    public UserHashSet() {
        this(16);
    }

    // since i implemented hashset over hashmap, i dont need an object.
    public void add(String key) {
        map.put(key, new Object());
    }

    public boolean contains(String key) {
        return map.contains(key);
    }

    public void remove(String key) {
        map.remove(key);
    }

    public int size() {
        return map.size();
    }

}
