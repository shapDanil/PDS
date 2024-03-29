import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;


public class PersistentMap<K, V> implements Map {

    private class PersistentMapEntry<K, V> implements Entry<K, V> {
        private final K key;
        private V value;

        public PersistentMapEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }
        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V old = this.value;
            this.value = value;
            return old;
        }
    }

    private int currentVersion = 0;
    private TreeMap<Integer, Integer> versionsLengths;
    private TreeMap<K, PersistentMapNode<V>> versionedData;

    /**
     * Constructs an empty persistent map.
     */
    public PersistentMap() {
        versionsLengths = new TreeMap<>();
        versionedData = new TreeMap<>();
        versionsLengths.put(0, 0);
    }

    /**
     * Returns the number of elements in the specified version of this map.
     * @param version version of this map
     * @return number of elements in the specified version of this map.
     */
    public int size(int version) {
        if (version < 0 || version > currentVersion)
            throw new NoSuchElementException(PersistentExceptionsMessege.NO_SUCH_VERSION);
        return versionsLengths.floorEntry(version).getValue();
    }

    /**
     * Returns the number of elements in the current version of this map.
     * @return number of elements in the current version of this map.
     */
    @Override
    public int size() {
        return size(currentVersion);
    }

    /**
     * Returns true if the specified version of this map contains no elements.
     * @param version version of this map
     * @return true if the specified version of this map contains no elements, false otherwise
     */
    public boolean isEmpty(int version) {
        if (version < 0 || version > currentVersion)
            throw new NoSuchElementException(PersistentExceptionsMessege.NO_SUCH_VERSION);
        return size(version) == 0;
    }

    /**
     * Returns true if the current version of this map contains no elements.
     * @return true if the current version of this map contains no elements, false otherwise
     */
    @Override
    public boolean isEmpty() {
        return isEmpty(currentVersion);
    }

    /**
     * Returns true if this version of map contains a mapping for the specified key.
     * More formally, returns true if and only if this version of map contains a mapping for a key k such that (key==null ? k==null : key.equals(k)).
     * (There can be at most one such mapping.)
     * @param key key whose presence in this map is to be tested
     * @param version version of this map
     * @return true if this version of map contains a mapping for the specified key
     */
    public boolean containsKey(Object key, int version) {
        if (version < 0 || version > currentVersion)
            throw new NoSuchElementException(PersistentExceptionsMessege.NO_SUCH_VERSION);

        if (versionedData.containsKey(key)) {
            PersistentMapNode node = versionedData.get(key);
            if (!node.isRemoved(version)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if current version of map contains a mapping for the specified key.
     * More formally, returns true if and only if current version of map contains a mapping for a key k such that (key==null ? k==null : key.equals(k)).
     * (There can be at most one such mapping.)
     * @param key key whose presence in this map is to be tested
     * @return true if this version of map contains a mapping for the specified key
     */
    @Override
    public boolean containsKey(Object key) {
        return containsKey(key, currentVersion);
    }

    /**
     * Returns true if this version of map maps one or more keys to the specified value.
     * More formally, returns true if and only if current version of map contains at least one mapping to a value v such that (value==null ? v==null : value.equals(v)).
     * This operation will probably require time linear in the map size for most implementations of the Map interface.
     * @param value value whose presence in current version of map is to be tested
     * @param version version of this map
     * @return if this version of map maps one or more keys to the specified value
     */
    public boolean containsValue(Object value, int version) {
        if (version < 0 || version > currentVersion)
            throw new NoSuchElementException(PersistentExceptionsMessege.NO_SUCH_VERSION);
        for (PersistentMapNode<V> node: versionedData.values()) {
            if (!node.isRemoved(version)) {
                if (null == value) {
                    if (node.getObject(version) == null)
                        return true;
                } else {
                    if (value.equals(node.getObject(version)))
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if current version of map maps one or more keys to the specified value.
     * More formally, returns true if and only if current version of map contains at least one mapping to a value v such that (value==null ? v==null : value.equals(v)).
     * This operation will probably require time linear in the map size for most implementations of the Map interface.
     * @param value value whose presence in current version of map is to be tested
     * @return if current version of map maps one or more keys to the specified value
     */
    @Override
    public boolean containsValue(Object value) {
        return containsValue(value, currentVersion);
    }

    /**
     * Returns the value to which the specified key is mapped, or null if this version of map contains no mapping for the key.
     * More formally, if this version of map contains a mapping from a key k to a value v such that (key==null ? k==null : key.equals(k)), then this method returns v; otherwise it returns null.
     * (There can be at most one such mapping.)
     * @param key the key whose associated value is to be returned
     * @param version version of this map
     * @return the value to which the specified key is mapped, or null if this version of map contains no mapping for the key
     */
    public Object get(Object key, int version) {
        if (version < 0 || version > currentVersion)
            throw new NoSuchElementException(PersistentExceptionsMessege.NO_SUCH_VERSION);
        if (!versionedData.containsKey(key))
            return null;
        PersistentMapNode node = versionedData.get(key);
        if (!node.isRemoved(version)) {
            return node.getObject(version);
        }
        return null;
    }

    /**
     * Returns the value to which the specified key is mapped, or null if current version of map contains no mapping for the key.
     * More formally, if current version of map contains a mapping from a key k to a value v such that (key==null ? k==null : key.equals(k)), then this method returns v; otherwise it returns null.
     * (There can be at most one such mapping.)
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or null if current version of map contains no mapping for the key
     */
    @Override
    public Object get(Object key) {
        return get(key, currentVersion);
    }

    /**
     * Associates the specified value with the specified key in current version of map (optional operation).
     * If the map previously contained a mapping for the key, the old value is replaced by the specified value.
     * (A map m is said to contain a mapping for a key k if and only if m.containsKey(k) would return true.)
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with key, or null if there was no mapping for key.
     */
    @Override
    public Object put(Object key, Object value) {
        Object oldValue = null;
        PersistentMapNode node = versionedData.get(key);
        currentVersion++;
        if (node == null) {
            versionedData.put((K)key, new PersistentMapNode<V>((V)value, currentVersion));
            int currSize = versionsLengths.floorEntry(currentVersion).getValue();
            versionsLengths.put(currentVersion, currSize + 1);
        } else {
            oldValue = node.getObject(currentVersion - 1);
            node.setObject(currentVersion, value);
        }
        return oldValue;
    }

    @Override
    public Object remove(Object key) {
        Object oldValue = null;
        PersistentMapNode node = versionedData.get(key);
        currentVersion++;
        if (null != node) {
            oldValue = node.getObject(currentVersion - 1);
            node.removeObject(currentVersion);
            int currSize = versionsLengths.floorEntry(currentVersion).getValue();
            versionsLengths.put(currentVersion, currSize - 1);
        }
        return oldValue;
    }

    @Override
    public void putAll(Map m) {
        currentVersion++;
        for (Object entry : m.entrySet()) {
            K key = ((Entry<K, V>) entry).getKey();
            V value = ((Entry<K, V>) entry).getValue();

            PersistentMapNode node = versionedData.get(key);
            if (null == node) {
                versionedData.put(key, new PersistentMapNode<V>(value, currentVersion));
            } else {
                node.setObject(currentVersion, value);
            }
        }
    }

    @Override
    public void clear() {
        currentVersion++;
        for (Entry<K, PersistentMapNode<V>> entry : versionedData.entrySet()) {
            if (!entry.getValue().isRemoved(currentVersion - 1)) {
                entry.getValue().removeObject(currentVersion);
            }
        }
        versionsLengths.put(currentVersion, 0);
    }

    public Set keySet(int version) {
        if (version < 0 || version > currentVersion)
            throw new NoSuchElementException(PersistentExceptionsMessege.NO_SUCH_VERSION);
        Set<K> keys = versionedData.keySet();
        Set<K> resultKeys = new HashSet<>(versionedData.keySet());

        for (K key : keys) {
            if (versionedData.get(key).isRemoved(version)) {
                resultKeys.remove(key);
            }
        }
        return resultKeys;
    }

    @Override
    public Set keySet() {
        return keySet(currentVersion);
    }

    public Collection values(int version) {
        if (version < 0 || version > currentVersion)
            throw new NoSuchElementException(PersistentExceptionsMessege.NO_SUCH_VERSION);

        LinkedList<V> result = new LinkedList<V>();
        for (Entry<K, PersistentMapNode<V>> entry : versionedData.entrySet()) {
            if (!entry.getValue().isRemoved(version)) {
                result.add(entry.getValue().getObject(version));
            }
        }
        return result;
    }

    @Override
    public Collection values() {
        return values(currentVersion);
    }

    public Set<Entry> entrySet(int version) {
        if (version < 0 || version > currentVersion)
            throw new NoSuchElementException(PersistentExceptionsMessege.NO_SUCH_VERSION);
        Set<Entry> result = new HashSet<>();

        for (Entry<K, PersistentMapNode<V>> entry : versionedData.entrySet()) {
            if (!entry.getValue().isRemoved(version)) {
                result.add(new PersistentMapEntry<>(entry.getKey(), entry.getValue().getObject(version)));
            }
        }
        return result;
    }

    @Override
    public Set<Entry> entrySet() {
        return entrySet(currentVersion);
    }

    public Object getOrDefault(Object key, Object defaultValue, int version) {
        if (version < 0 || version > currentVersion)
            throw new NoSuchElementException(PersistentExceptionsMessege.NO_SUCH_VERSION);

        if (versionedData.get(key) != null && !versionedData.get(key).isRemoved(version)) {
            return versionedData.get(key).getObject(version);
        }
        return defaultValue;
    }

    @Override
    public Object getOrDefault(Object key, Object defaultValue) {
        return getOrDefault(key, defaultValue, currentVersion);
    }

    @Override
    public void forEach(BiConsumer action) {
        for (Entry<K, PersistentMapNode<V>> entry : versionedData.entrySet()) {
            if (!entry.getValue().isRemoved(currentVersion)) {
                action.accept(entry.getKey(), entry.getValue().getObject(currentVersion));
            }
        }
    }

    @Override
    public void replaceAll(BiFunction function) {
        for (Entry<K, PersistentMapNode<V>> entry : versionedData.entrySet()) {
            if (!entry.getValue().isRemoved(currentVersion)) {
                entry.getValue().setObject(currentVersion + 1, (V)function.apply(entry.getKey(), entry.getValue().getObject(currentVersion)));
            }
        }
        currentVersion++;
    }

    @Override
    public Object putIfAbsent(Object key, Object value) {
        Object oldValue = null;
        PersistentMapNode node = versionedData.get(key);
        currentVersion++;
        if (null == node) {
            versionedData.put((K)key, new PersistentMapNode<V>((V)value, currentVersion));
        } else {
            oldValue = node.getObject(currentVersion - 1);
            if (null == oldValue) {
                node.setObject(currentVersion, value);
            }
        }
        return oldValue;
    }

    @Override
    public boolean remove(Object key, Object value)
    {
        PersistentMapNode node = versionedData.get(key);
        int curSize = size();

        if (null != node && node.getObject(currentVersion).equals(value) && !node.isRemoved(currentVersion)) {
            currentVersion++;
            node.removeObject(currentVersion);
            versionsLengths.put(currentVersion, curSize - 1);
            return true;
        }
        return false;
    }

    @Override
    public boolean replace(Object key, Object oldValue, Object newValue) {
        PersistentMapNode node = versionedData.get(key);

        if (null != node && null != node.getObject(currentVersion) &&
                !node.isRemoved(currentVersion) && node.getObject(currentVersion).equals(oldValue)) {
            currentVersion++;
            node.setObject(currentVersion, newValue);
            return true;
        }
        return false;
    }

    @Override
    public Object replace(Object key, Object value) {
        Object oldValue = null;
        PersistentMapNode node = versionedData.get(key);

        if (null != node && !node.isRemoved(currentVersion)) {
            oldValue = node.getObject(currentVersion);
            currentVersion++;
            node.setObject(currentVersion, value);
        }
        return oldValue;
    }

    @Override
    public Object computeIfAbsent(Object key, Function mappingFunction) {
        PersistentMapNode node = versionedData.get(key);

        if (null != node && !node.isRemoved(currentVersion) && node.getObject(currentVersion) != null) {
            return node.getObject(currentVersion);
        }

        Object value = mappingFunction.apply(key);
        if (null != value) {
            currentVersion++;
            if (null == node) {
                versionedData.put((K)key, new PersistentMapNode<V>((V)value, currentVersion));
            } else {
                node.setObject(currentVersion, value);
            }
        }

        return value;
    }

    @Override
    public Object computeIfPresent(Object key, BiFunction remappingFunction) {
        PersistentMapNode node = versionedData.get(key);

        if (null == node || node.isRemoved(currentVersion) || node.getObject(currentVersion) == null) {
            return null;
        }

        Object oldValue = node.getObject(currentVersion);
        Object value = remappingFunction.apply(key, oldValue);
        currentVersion++;
        if (null != value) {
            node.setObject(currentVersion, value);
        } else {
            node.removeObject(currentVersion);
        }

        return value;
    }

    @Override
    public Object compute(Object key, BiFunction remappingFunction) {
        PersistentMapNode node = versionedData.get(key);

        Object oldValue = null;
        if (null != node && !node.isRemoved(currentVersion)) {
            oldValue = node.getObject(currentVersion);
        }

        Object value = remappingFunction.apply(key, oldValue);
        currentVersion++;
        if (null != value) {
            node.setObject(currentVersion, value);
        } else {
            if (null != oldValue) {
                node.removeObject(currentVersion);
            }
        }

        return value;
    }

    @Override
    public Object merge(Object key, Object value, BiFunction remappingFunction) {
        PersistentMapNode node = versionedData.get(key);
        if (null == node || node.isRemoved(currentVersion) || node.getObject(currentVersion) == null) {
            currentVersion++;
            node.setObject(currentVersion, value);
            return value;
        }

        Object oldValue = node.getObject(currentVersion);
        Object newValue = remappingFunction.apply(key, oldValue);
        currentVersion++;

        if (null != newValue) {
            node.setObject(currentVersion, newValue);
        } else {
            node.removeObject(currentVersion);
        }

        return newValue;
    }
}
