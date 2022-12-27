import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;


public class PersistentArrayTest {
    private PersistentArray<Integer> array = null;
    private int initialCapacity = 10;

    @Rule
    public ExpectedException ex = ExpectedException.none();


    @Test
    public void checkInitialCapacity() {
        array = new PersistentArray<>();
        assertEquals(array.getLength(), initialCapacity);
    }

    @Test
    public void getVersioned() throws Exception {
        array = new PersistentArray<>(5);
        array.replace(3, 40);
        array.replace(3, 42);
        array.replace(3, 36);
        assertEquals(array.get(3, 0), null);
        assertEquals((long) array.get(3, 1), (long) 40);
        assertEquals((long) array.get(3, 2), (long) 42);
        assertEquals((long) array.get(3, 3), (long) 36);
    }

    @Test
    public void getVersionedBadVersion() throws Exception {
        array = new PersistentArray<>();
        ex.expect(NoSuchElementException.class);
        ex.expectMessage(PersistentExceptionsMessege.NO_SUCH_VERSION);
        array.get(0, 2018);
    }

    @Test
    public void getVersionedBadVIndex() throws Exception {
        array = new PersistentArray<>();
        ex.expect(ArrayIndexOutOfBoundsException.class);
        ex.expectMessage(PersistentExceptionsMessege.ARRAY_INDEX_OUT_OF_BOUNDS);
        array.get(2018, 0);
    }

    @Test
    public void get() throws Exception {
        int obj = 6;
        array = new PersistentArray<>();
        array.add(obj);
        assertEquals((int) array.get(PersistentArray.DEFAULT_CAPACITY), (int) obj);
    }

    @Test
    public void replace() throws Exception {
        array = new PersistentArray<>();
        assertEquals(array.replace(0, 1), 1);
        assertEquals(array.replace(1, 2), 2);
        ex.expect(ArrayIndexOutOfBoundsException.class);
        ex.expectMessage(PersistentExceptionsMessege.ARRAY_INDEX_OUT_OF_BOUNDS);
        array.replace(initialCapacity + 5, 1);
    }

    @Test
    public void getLength() throws Exception {
        array = new PersistentArray<>();
        assertEquals(array.getLength(), initialCapacity);
        array.add(1);
        assertEquals(array.getLength(), initialCapacity + 1);
    }

    @Test
    public void getLengthVersioned() throws Exception {
        array = new PersistentArray<>();
        assertEquals(array.getLength(0), initialCapacity);
        array.add(1);
        assertEquals(array.getLength(1), initialCapacity + 1);
        ex.expect(NoSuchElementException.class);
        ex.expectMessage(PersistentExceptionsMessege.NO_SUCH_VERSION);
        array.getLength(10);
    }

    @Test
    public void remove() throws Exception {
        array = new PersistentArray<>(0);
        array.add(1);
        array.removeLast();
        assertEquals(array.getLength(), 0);
    }
    @Test
    public void remove1() throws Exception {
        array = new PersistentArray<>(3);
        array.add(1);
        array.add(2);
        array.add(3);
        array.removeLast();
        assertEquals(array.getLength(), 5);
    }

    @Test
    public void removeEmptyArray() throws Exception {
        array = new PersistentArray<>(0);
        ex.expect(ArrayIndexOutOfBoundsException.class);
        ex.expectMessage(PersistentExceptionsMessege.NOTHING_TO_REMOVE);
        array.removeLast();
    }

}