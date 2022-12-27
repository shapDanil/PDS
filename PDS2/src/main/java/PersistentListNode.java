import java.util.TreeMap;

public class PersistentListNode<T> {

    private TreeMap<Integer, T> versionedData;
    private TreeMap<Integer, PersistentListNode<T>> versionedPrev;
    private TreeMap<Integer, PersistentListNode<T>> versionedNext;

    public PersistentListNode(T object, int version, PersistentListNode<T> prev, PersistentListNode<T> next) {
        versionedData = new TreeMap<>();
        versionedPrev = new TreeMap<>();
        versionedNext = new TreeMap<>();

        versionedData.put(version, object);
        versionedPrev.put(version, prev);
        versionedNext.put(version, next);
    }
    public T getObject(int version) { return versionedData.floorEntry(version).getValue(); }

    public void setObject(int version, T obj) {
        versionedData.put(version, obj);
    }
    
    public PersistentListNode<T> getNext(int version) {
        return versionedNext.floorEntry(version).getValue();
    }

    public void setNext(int version, PersistentListNode<T> next) {
        versionedNext.put(version, next);
    }

    public PersistentListNode<T> getPrev(int version) {
        return versionedPrev.floorEntry(version).getValue();
    }

    public void setPrev(int version, PersistentListNode<T> prev) {
        versionedPrev.put(version, prev);
    }

    
}
