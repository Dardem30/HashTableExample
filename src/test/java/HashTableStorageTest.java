import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class HashTableStorageTest {

    private final HashTableStorage<String, String> hashTable = new HashTableStorage<>();

    @Test
    public void testPutAndGet() {
        hashTable.put("key1", "value1");
        assertEquals("value1", hashTable.get("key1"));
    }

    @Test
    public void testPutOverwrite() {
        hashTable.put("key1", "value1");
        hashTable.put("key1", "value2");
        assertEquals("value2", hashTable.get("key1"));
    }

    @Test
    public void testGetNonExistentKey() {
        assertNull(hashTable.get("nonExistentKey"));
    }

    @Test
    public void testRemove() {
        hashTable.put("key1", "value1");
        hashTable.remove("key1");
        assertNull(hashTable.get("key1"));
    }

    @Test
    public void testRemoveNonExistentKey() {
        hashTable.put("key1", "value1");
        hashTable.remove("nonExistentKey");
        assertEquals("value1", hashTable.get("key1"));
    }

    @Test
    public void testMultipleTables() {
        for (int index = 0; index < 100; index++) {
            hashTable.put("key" + index, "value" + index);
        }
        for (int index = 0; index < 100; index++) {
            assertEquals("value" + index, hashTable.get("key" + index));
        }
    }
}