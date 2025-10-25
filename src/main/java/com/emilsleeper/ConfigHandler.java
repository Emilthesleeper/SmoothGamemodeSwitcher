package com.emilsleeper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.GameMode;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigHandler {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("smoothgamemodeswitcher.json");

    private static final List<Integer> DEFAULT_GAMEMODE_ORDER = Arrays.asList(
            GameMode.SURVIVAL.ordinal(),
            GameMode.SPECTATOR.ordinal(),
            GameMode.CREATIVE.ordinal()
    );
    private static List<Integer> gamemodeOrder = DEFAULT_GAMEMODE_ORDER;
    private static double defaultdisableFlyingBlockTolerance = 0.2;
    private static double disableFlyingBlockTolerance = defaultdisableFlyingBlockTolerance;

    public static List<Integer> getGamemodeOrder() {
        return gamemodeOrder;
    }

    public static void setGamemodeOrder(List<Integer> newOrder) {
        List<Integer> processedOrder = new ArrayList<>();
        if (newOrder.size() > 4) {
            newOrder = newOrder.subList(0, Math.min(4, newOrder.size()));
        }
        
        for (int i = 0; i < newOrder.size(); i++) {
            int value = newOrder.get(i);
            // Ensure value is within valid range (0-3 for gamemodes)
            value = Math.max(0, Math.min(3, value));
            
            // If value is already in the list, find the next available gamemode
            while (processedOrder.contains(value)) {
                value = (value + 1) % 4; // Cycle through 0-3
            }
            
            processedOrder.add(value);
        }
        
        gamemodeOrder = processedOrder;
        saveConfig();
    }

    public static List<Integer> getDefaultGamemodeOrder() {
        return new ArrayList<>(DEFAULT_GAMEMODE_ORDER);
    }

    public static double getDisableFlyingBlockTolerance() {
        return disableFlyingBlockTolerance;
    }

    public static double getDefaultDisableFlyingBlockTolerance() {
        return defaultdisableFlyingBlockTolerance;
    }

    public static void setDisableFlyingBlockTolerance(double tolerance) {
        disableFlyingBlockTolerance = tolerance;
        saveConfig();
    }

    static void saveConfig() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_FILE, StandardCharsets.UTF_8)) {
            ConfigData configData = new ConfigData(gamemodeOrder, disableFlyingBlockTolerance);
            GSON.toJson(configData, writer);
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }

    public static void loadConfig() {
        if (!Files.exists(CONFIG_FILE)) {
            saveConfig();
            return;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_FILE, StandardCharsets.UTF_8)) {
            ConfigData configData = GSON.fromJson(reader, ConfigData.class);
            if (configData != null) {
                gamemodeOrder = configData.gamemodeOrder != null ? 
                    new ArrayList<>(configData.gamemodeOrder) : new ArrayList<>(DEFAULT_GAMEMODE_ORDER);
                disableFlyingBlockTolerance = configData.disableFlyingBlockTolerance;
            }
        } catch (IOException e) {
            System.err.println("Failed to load config: " + e.getMessage());
        }
    }

    private static class ConfigData {
        private final List<Integer> gamemodeOrder;
        private final double disableFlyingBlockTolerance;

        private ConfigData(List<Integer> gamemodeOrder, double disableFlyingBlockTolerance) {
            this.gamemodeOrder = gamemodeOrder;
            this.disableFlyingBlockTolerance = disableFlyingBlockTolerance;
        }
    }
}
