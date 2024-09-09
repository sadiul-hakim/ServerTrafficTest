package xyz.sadiulhakim;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class Main {
    private static final Scanner input = new Scanner(System.in);
    private static final ConcurrentHashMap<String, Integer> result = new ConcurrentHashMap<>();
    private static final String BASE_PATH = "F:\\TrafficTest";
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        String url;
        int calls;
        int batchSize;
        int batchDelay;

        System.out.println("+-------------------------Traffic Tester----------------------+");
        System.out.println("Enter the url: ");
        url = input.nextLine();
        System.out.println("Enter number of calls: ");
        calls = input.nextInt();
        System.out.println("Enter batch size: ");
        batchSize = input.nextInt();
        System.out.println("Enter Delay (in millis) between two batches: ");
        batchDelay = input.nextInt();

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .build();

        long start = System.currentTimeMillis();
        try (HttpClient client = HttpClient.newBuilder().executor(Executors.newVirtualThreadPerTaskExecutor()).build()) {

            int count = 0;
            for (int i = 1; i <= calls; i++) {
                if (count == batchSize && batchDelay > 0) {
                    Thread.sleep(Duration.ofMillis(batchDelay));
                    count = 0;
                }

                sendGetRequest(client, request);
                count++;
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        long end = System.currentTimeMillis();

        writeToFile();

        System.out.printf("Total time taken: %s s\n", (end - start) / 1000.0);
        System.out.println("Result Size: " + result.size());
    }

    private static void writeToFile() throws IOException {
        String filePath = BASE_PATH + File.separator + UUID.randomUUID() + ".json";
        Path path = Path.of(filePath);
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        Files.writeString(path, mapper.writeValueAsString(result));
    }

    private static void sendGetRequest(HttpClient client, HttpRequest request) {
        HttpResponse<String> send;
        try {
            send = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        result.put(UUID.randomUUID().toString(), send.statusCode());
    }
}