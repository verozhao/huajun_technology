
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * EnhancedMultiThreadedWebServer class simulates a multithreaded HTTP server
 * capable of handling concurrent client requests and basic routing.
 */
public class EnhancedMultiThreadedWebServer {

    private final int port;
    private final ExecutorService threadPool;
    private final AtomicInteger requestCount = new AtomicInteger(); // Counts total requests handled

    public EnhancedMultiThreadedWebServer(int port, int threadPoolSize) {
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(threadPoolSize);
    }

    /**
     * Starts the server and accepts incoming client connections.
     */
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port: " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(new ClientHandler(clientSocket, requestCount));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }

    /**
     * ClientHandler class handles each client connection in a separate thread.
     */
    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final AtomicInteger requestCount;

        public ClientHandler(Socket clientSocket, AtomicInteger requestCount) {
            this.clientSocket = clientSocket;
            this.requestCount = requestCount;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String requestLine = in.readLine();
                System.out.println("Request: " + requestLine);

                int currentCount = requestCount.incrementAndGet();
                System.out.println("Total Requests: " + currentCount);

                String responseBody;
                if (requestLine.contains("GET /hello")) {
                    responseBody = "Hello, welcome to the multi-threaded server!";
                } else if (requestLine.contains("GET /status")) {
                    responseBody = "Server has handled " + currentCount + " requests.";
                } else if (requestLine.contains("GET /time")) {
                    responseBody = "Current server time: " + new java.util.Date();
                } else {
                    responseBody = "404 Not Found";
                }

                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: text/plain");
                out.println();
                out.println(responseBody);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        EnhancedMultiThreadedWebServer server = new EnhancedMultiThreadedWebServer(8080, 4);
        server.start();
    }
}
