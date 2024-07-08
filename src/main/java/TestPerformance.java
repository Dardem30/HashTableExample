import java.util.*;

public class TestPerformance {
    HashTableStorage<String, String> hashTableStorage = new HashTableStorage<>();
    Map<String, String> hashMap = new HashMap<>();
    final List<String> testData = new ArrayList<>();

    {
        for (int index = 0; index < 10000; index++) {
            testData.add(UUID.randomUUID().toString());
        }
    }

    public static void main(String[] args) {
        final TestPerformance testPerformance = new TestPerformance();
        testPerformance.testPut();
        System.out.println("----------------------");
        testPerformance.testSearch();
    }

    private void fillTestData() {
        hashTableStorage = new HashTableStorage<>();
        for (String str : testData) {
            hashTableStorage.put(str, str);
            hashMap.put(str, str);
        }

    }

    private void testPut() {
        Long startTime = System.nanoTime();
        for (String str : testData) {
            hashMap.put(str, str);
        }
        System.out.println("hashMap.put - " + trackAndReset(startTime, true));
        for (String str : testData) {
            hashTableStorage.put(str, str);
        }
        System.out.println("hashTableStorage.put - " + trackAndReset(startTime, true));
    }

    private void testSearch() {
        fillTestData();
        Long startTime = System.nanoTime();
        for (String str : testData) {
            hashMap.get(str);
        }
        System.out.println("hashMap.get - " + trackAndReset(startTime, true));
        for (String str : testData) {
            hashTableStorage.get(str);
        }
        System.out.println("hashTableStorage.get - " + trackAndReset(startTime, true));
    }

    private static Long trackAndReset(Long startTime) {
        final long result = System.nanoTime() - startTime;
        startTime = System.nanoTime();
        return result;
    }

    private static Long trackAndReset(Long startTime, boolean convertToMs) {
        final long result = System.nanoTime() - startTime;
        startTime = System.nanoTime();
        return convertToMs ? nanoToMillis(result) : result;
    }

    public static long nanoToMillis(long nanoseconds) {
        return nanoseconds / 1000000;
    }
}
