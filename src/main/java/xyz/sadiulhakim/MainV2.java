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
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class MainV2 {
    private static final Scanner input = new Scanner(System.in);
    private static final CopyOnWriteArrayList<Integer> result = new CopyOnWriteArrayList<>();
    private static final String BASE_PATH = "F:\\TrafficTest";
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {

        String url;
        int calls;

        System.out.println("+-------------------------Traffic Tester----------------------+");
        System.out.println("Enter the url: ");
        url = input.nextLine();
        System.out.println("Enter number of calls: ");
        calls = input.nextInt();

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .build();

        try (HttpClient client = HttpClient.newBuilder().executor(Executors.newVirtualThreadPerTaskExecutor()).build()) {
            long start = System.currentTimeMillis();
            IntStream.rangeClosed(1, calls)
                    .parallel()
                    .forEach(num -> sendGetRequest(client, request));
            long end = System.currentTimeMillis();
            System.out.printf("Total time taken: %s s\n", (end - start) / 1000.0);
            System.out.println("Result Size: " + result.size());

            writeToFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

        result.add(send.statusCode());
    }
}
