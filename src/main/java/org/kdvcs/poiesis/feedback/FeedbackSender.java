package org.kdvcs.poiesis.feedback;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraftforge.fml.loading.FMLPaths;
import org.kdvcs.poiesis.Poiesis;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/** Sends locally queued feedback without blocking the Minecraft thread. */
public final class FeedbackSender {
    private static final String DEFAULT_ENDPOINT =
            "http://118.178.137.83:3000/poiesis/feedback";
    private static final Path ENDPOINT_FILE = FMLPaths.CONFIGDIR.get()
            .resolve(Poiesis.MOD_ID).resolve("feedback_endpoint.txt");
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private FeedbackSender() { }

    public static void enqueue(Path file) {
        CompletableFuture.runAsync(() -> send(file));
    }

    public static void retryPending() {
        Path directory = FeedbackStore.directory();
        if (!Files.isDirectory(directory)) return;
        try (var files = Files.list(directory)) {
            files.filter(path -> path.getFileName().toString().endsWith(".json"))
                    .filter(FeedbackSender::isPending)
                    .limit(3)
                    .forEach(FeedbackSender::enqueue);
        } catch (IOException ignored) {
            // Offline storage remains usable when scanning fails.
        }
    }

    private static boolean isPending(Path file) {
        try {
            JsonObject root = JsonParser.parseString(
                    Files.readString(file, StandardCharsets.UTF_8)
            ).getAsJsonObject();
            String status = root.has("status") ? root.get("status").getAsString() : "pending";
            return "pending".equals(status) || "failed".equals(status);
        } catch (Exception ignored) {
            return false;
        }
    }

    private static void send(Path file) {
        try {
            String payload = Files.readString(file, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint()))
                    .timeout(Duration.ofSeconds(8))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = CLIENT.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
            FeedbackStore.updateStatus(file,
                    response.statusCode() >= 200 && response.statusCode() < 300
                            ? "sent" : "failed");
        } catch (Exception ignored) {
            try {
                FeedbackStore.updateStatus(file, "failed");
            } catch (IOException ignoredWrite) {
                // Keep the original file if even status bookkeeping fails.
            }
        }
    }

    private static String endpoint() throws IOException {
        if (!Files.exists(ENDPOINT_FILE)) {
            Files.createDirectories(ENDPOINT_FILE.getParent());
            Files.writeString(ENDPOINT_FILE, DEFAULT_ENDPOINT + System.lineSeparator(),
                    StandardCharsets.UTF_8);
            return DEFAULT_ENDPOINT;
        }
        String configured = Files.readString(ENDPOINT_FILE, StandardCharsets.UTF_8).trim();
        return configured.isEmpty() ? DEFAULT_ENDPOINT : configured;
    }
}
