
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * DistributedFileStorageSystem class simulates a distributed file system
 * with chunking, caching, and replication features.
 */
public class DistributedFileStorageSystem {

    private static final String STORAGE_DIR = "storage/";
    private static final int CACHE_SIZE = 5; // Maximum cache size
    private static final int FILE_CHUNK_SIZE = 1024; // Chunk size: 1KB

    private final Map<String, String> cache = new LinkedHashMap<>(CACHE_SIZE, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return size() > CACHE_SIZE;
        }
    };

    private final ScheduledExecutorService cacheCleaner = Executors.newSingleThreadScheduledExecutor();

    public DistributedFileStorageSystem() {
        File storageDir = new File(STORAGE_DIR);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        // Schedule periodic cache cleanup
        cacheCleaner.scheduleAtFixedRate(() -> {
            synchronized (cache) {
                cache.clear();
                System.out.println("Cache cleared.");
            }
        }, 10, 10, TimeUnit.MINUTES);
    }

    /**
     * Writes a file to the storage with chunking.
     * @param fileName Name of the file
     * @param data Content of the file
     * @throws IOException If an I/O error occurs
     */
    public synchronized void writeFile(String fileName, String data) throws IOException {
        int chunkCount = (int) Math.ceil((double) data.length() / FILE_CHUNK_SIZE);
        for (int i = 0; i < chunkCount; i++) {
            String chunkData = data.substring(i * FILE_CHUNK_SIZE, Math.min((i + 1) * FILE_CHUNK_SIZE, data.length()));
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(STORAGE_DIR + fileName + "_chunk" + i))) {
                writer.write(chunkData);
            }
        }

        // Update cache with the full file content
        synchronized (cache) {
            cache.put(fileName, data);
        }
        System.out.println("File written: " + fileName);
    }

    /**
     * Reads a file from storage, assembling chunks into a single content string.
     * @param fileName Name of the file
     * @return The full content of the file
     * @throws IOException If an I/O error occurs
     */
    public synchronized String readFile(String fileName) throws IOException {
        synchronized (cache) {
            if (cache.containsKey(fileName)) {
                System.out.println("Cache hit for file: " + fileName);
                return cache.get(fileName);
            }
        }

        // Assemble file chunks into full content
        StringBuilder data = new StringBuilder();
        int chunkIndex = 0;
        while (true) {
            File file = new File(STORAGE_DIR + fileName + "_chunk" + chunkIndex);
            if (!file.exists()) {
                break;
            }
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    data.append(line);
                }
            }
            chunkIndex++;
        }

        // Cache the assembled content
        synchronized (cache) {
            cache.put(fileName, data.toString());
        }
        System.out.println("File read from storage: " + fileName);
        return data.toString();
    }

    /**
     * Deletes a file by removing all its chunks from storage.
     * @param fileName Name of the file to be deleted
     */
    public synchronized void deleteFile(String fileName) {
        int chunkIndex = 0;
        while (true) {
            File file = new File(STORAGE_DIR + fileName + "_chunk" + chunkIndex);
            if (!file.exists()) {
                break;
            }
            if (file.delete()) {
                System.out.println("Deleted chunk " + chunkIndex + " of file: " + fileName);
            }
            chunkIndex++;
        }

        // Remove file from cache
        synchronized (cache) {
            cache.remove(fileName);
        }
    }

    /**
     * Simulates replication of a file to multiple nodes in a cluster.
     * @param fileName Name of the file to replicate
     * @param nodeAddresses List of node addresses to replicate the file
     */
    public void replicateFile(String fileName, List<String> nodeAddresses) {
        nodeAddresses.forEach(node -> System.out.println("Replicating " + fileName + " to node " + node));
    }

    /**
     * Displays the current cache status, showing cached file names.
     */
    public void displayCacheStatus() {
        synchronized (cache) {
            System.out.println("Cache Status:");
            cache.keySet().forEach(file -> System.out.println(" - " + file));
        }
    }

    public static void main(String[] args) {
        try {
            DistributedFileStorageSystem storage = new DistributedFileStorageSystem();

            // Test file write operation
            storage.writeFile("file1.txt", "This is a test file. Its content will be split into multiple chunks.");
            storage.writeFile("file2.txt", "This is another file for testing purposes.");

            // Test file read operation
            System.out.println(storage.readFile("file1.txt"));
            System.out.println(storage.readFile("file2.txt"));

            // Test replication
            storage.replicateFile("file1.txt", Arrays.asList("Node1", "Node2", "Node3"));

            // Display cache status
            storage.displayCacheStatus();

            // Test delete operation
            storage.deleteFile("file1.txt");
            storage.displayCacheStatus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
