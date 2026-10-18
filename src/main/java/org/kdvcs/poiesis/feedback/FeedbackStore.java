package org.kdvcs.poiesis.feedback;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import org.kdvcs.poiesis.Poiesis;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Offline-first feedback storage. Network delivery is deliberately separate. */
public final class FeedbackStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int MAX_TITLE = 120;
    private static final int MAX_CONTENT = 4000;

    private FeedbackStore() { }

    public static Path save(String title, String content, boolean anonymous) throws IOException {
        return save(title, content, anonymous, null);
    }

    /** Saves feedback together with optional client-side player context. */
    public static Path save(
            String title,
            String content,
            boolean anonymous,
            JsonObject playerContext
    ) throws IOException {
        String cleanTitle = limit(title.trim(), MAX_TITLE);
        String cleanContent = limit(content.trim(), MAX_CONTENT);
        if (cleanTitle.isEmpty() && cleanContent.isEmpty()) {
            throw new IllegalArgumentException("Feedback cannot be empty");
        }

        Path directory = FMLPaths.CONFIGDIR.get().resolve(Poiesis.MOD_ID).resolve("feedback");
        Files.createDirectories(directory);
        JsonObject root = new JsonObject();
        root.addProperty("id", UUID.randomUUID().toString());
        root.addProperty("title", cleanTitle);
        root.addProperty("content", cleanContent);
        root.addProperty("anonymous", anonymous);
        root.addProperty("created_at", OffsetDateTime.now().toString());
        root.addProperty("status", "pending");
        JsonObject metadata = new JsonObject();
        metadata.addProperty("poiesis_version", ModList.get().getModContainerById(Poiesis.MOD_ID)
                .map(container -> container.getModInfo().getVersion().toString()).orElse("unknown"));
        metadata.addProperty("minecraft_version", "1.20.1");
        metadata.addProperty("language", "client");
        if (playerContext != null && !playerContext.entrySet().isEmpty()) {
            metadata.add("player", playerContext.deepCopy());
        }
        root.add("metadata", metadata);

        Path file = directory.resolve("feedback-" + UUID.randomUUID() + ".json");
        Files.writeString(file, GSON.toJson(root), StandardCharsets.UTF_8);
        return file;
    }

    public static void updateStatus(Path file, String status) throws IOException {
        JsonObject root = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8))
                .getAsJsonObject();
        root.addProperty("status", status);
        Files.writeString(file, GSON.toJson(root), StandardCharsets.UTF_8);
    }

    public static Path directory() {
        return FMLPaths.CONFIGDIR.get().resolve(Poiesis.MOD_ID).resolve("feedback");
    }

    private static String limit(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max);
    }
}
