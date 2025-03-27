package net.firestarter03.ctlive_afktimer.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CTLiveAFKTimerConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("ctlive_afktimer.json");

    // Konfigurationsoptionen
    public int afkTimeout = 5; // Minuten
    public boolean useEnglishAFKMessage = true; // Für die AFK-Nachrichten

    public static CTLiveAFKTimerConfig load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                String json = Files.readString(CONFIG_PATH);
                return GSON.fromJson(json, CTLiveAFKTimerConfig.class);
            }
            return new CTLiveAFKTimerConfig();
        } catch (IOException e) {
            e.printStackTrace();
            return new CTLiveAFKTimerConfig();
        }
    }

    public void save() {
        try {
            String json = GSON.toJson(this);
            Files.writeString(CONFIG_PATH, json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 