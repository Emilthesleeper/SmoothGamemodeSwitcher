package com.emilsleeper.smoothgamemodeswitcher.client;

import com.emilsleeper.smoothgamemodeswitcher.ConfigHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.world.level.GameType;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class SmoothgamemodeswitcherClient implements ClientModInitializer {
    private static KeyMapping switchGamemodeKey;

    private void updateAbilitiesWithDelay(LocalPlayer player, boolean flying) {
        new Thread(() -> {
            try {
                Thread.sleep(41);
                player.getAbilities().flying = flying;
                player.onUpdateAbilities();
                player.connection.send(new ServerboundPlayerAbilitiesPacket(player.getAbilities()));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    @Override
    public void onInitializeClient() {
        ConfigHandler.loadConfig();

        switchGamemodeKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "smoothgamemodeswitcher.keybind.switch_gamemode",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_X,
                KeyMapping.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (switchGamemodeKey.consumeClick()) {
                if (client.player == null || client.gameMode == null || client.level == null) {
                    continue;
                }

                List<Integer> gamemodeOrder = ConfigHandler.getGamemodeOrder();
                GameType currentMode = client.gameMode.getPlayerMode();
                int currentIndex = gamemodeOrder.indexOf(currentMode.getId());

                if (currentIndex == -1) {
                    currentIndex = 0;
                }

                int nextIndex = (currentIndex + 1) % gamemodeOrder.size();
                GameType nextMode = GameType.byId(gamemodeOrder.get(nextIndex));

                if (nextMode == GameType.ADVENTURE) {
                    client.player.connection.sendCommand("gamemode adventure");
                    client.player.onGameModeChanged(GameType.ADVENTURE);
                    updateAbilitiesWithDelay(client.player, false);
                } else if (nextMode == GameType.SURVIVAL) {
                    client.player.connection.sendCommand("gamemode survival");
                    client.player.onGameModeChanged(GameType.SURVIVAL);
                    updateAbilitiesWithDelay(client.player, false);
                } else if (nextMode == GameType.CREATIVE) {
                    client.player.connection.sendCommand("gamemode creative");
                    client.player.onGameModeChanged(GameType.CREATIVE);

                    BlockPos block;
                    double calculatedTolerance;

                    try {
                        block = client.player.blockPosition();
                        calculatedTolerance = block.getY()
                                + client.level.getBlockState(block).getCollisionShape(client.level, block).bounds().maxY
                                + ConfigHandler.getDisableFlyingBlockTolerance();
                    } catch (Exception e) {
                        try {
                            block = client.player.blockPosition().below();
                            calculatedTolerance = block.getY()
                                    + client.level.getBlockState(block).getCollisionShape(client.level, block).bounds().maxY
                                    + ConfigHandler.getDisableFlyingBlockTolerance();
                        } catch (Exception e2) {
                            continue;
                        }
                    }

                    if (client.player.getY() <= calculatedTolerance) {
                        updateAbilitiesWithDelay(client.player, false);
                    } else {
                        updateAbilitiesWithDelay(client.player, true);
                    }
                } else if (nextMode == GameType.SPECTATOR) {
                    client.player.connection.sendCommand("gamemode spectator");
                    client.player.onGameModeChanged(GameType.SPECTATOR);
                    updateAbilitiesWithDelay(client.player, true);
                }
            }
        });
    }
}