package com.emilsleeper.client;

import com.emilsleeper.ConfigHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.c2s.play.UpdatePlayerAbilitiesC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class SmoothgamemodeswitcherClient implements ClientModInitializer {
    private static KeyBinding switchGamemodeKey;
    private void updateAbilitiesWithDelay(ClientPlayerEntity player, boolean flying) {
        new Thread(() -> {
            try {
                Thread.sleep(41);
                player.getAbilities().flying = flying;
                player.sendAbilitiesUpdate();
                player.networkHandler.sendPacket(new UpdatePlayerAbilitiesC2SPacket(player.getAbilities()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void onInitializeClient() {
        ConfigHandler.loadConfig();
        switchGamemodeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "smoothgamemodeswitcher.keybind.switch_gamemode",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            "smoothgamemodeswitcher.category"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (switchGamemodeKey.wasPressed()) {
                if (client.player != null) {
                    List<Integer> gamemodeOrder = ConfigHandler.getGamemodeOrder();
                    GameMode currentMode = client.getInstance().interactionManager.getCurrentGameMode();
                    int currentIndex = gamemodeOrder.indexOf(currentMode.ordinal());
                    
                    // If current mode not found in order, default to first in order
                    if (currentIndex == -1) {
                        currentIndex = 0;
                    }
                    
                    // Get next index, wrapping around if needed
                    int nextIndex = (currentIndex + 1) % gamemodeOrder.size();
                    GameMode nextMode = GameMode.byId(gamemodeOrder.get(nextIndex));
                    if (nextMode == GameMode.ADVENTURE) {
                        client.player.networkHandler.sendChatCommand("gamemode adventure");
                        client.player.onGameModeChanged(GameMode.ADVENTURE);
                        updateAbilitiesWithDelay(client.player, false);
                    } if (nextMode == GameMode.SURVIVAL) {
                        client.player.networkHandler.sendChatCommand("gamemode survival");
                        client.player.onGameModeChanged(GameMode.SURVIVAL);
                        updateAbilitiesWithDelay(client.player, false);
                    } if (nextMode == GameMode.CREATIVE) {
                        client.player.networkHandler.sendChatCommand("gamemode creative");
                        client.player.onGameModeChanged(GameMode.CREATIVE);
                        BlockPos block;
                        double calculatedTolerance;
                        try {
                            block = client.player.getBlockPos();
                            calculatedTolerance = block.getY() + client.world.getBlockState(block).getOutlineShape(client.world, block).getBoundingBox().maxY + ConfigHandler.getDisableFlyingBlockTolerance();
                        } catch (Exception e) {
                            try {
                                block = client.player.getBlockPos().down();
                                calculatedTolerance = block.getY() + client.world.getBlockState(block).getOutlineShape(client.world, block).getBoundingBox().maxY + ConfigHandler.getDisableFlyingBlockTolerance();
                            } catch (Exception e2) {
                                break;
                            }
                        }
                        if (client.player.getY() <= calculatedTolerance) {
                            updateAbilitiesWithDelay(client.player, false);
                        } else {
                            updateAbilitiesWithDelay(client.player, true);
                        }
                    } if (nextMode == GameMode.SPECTATOR) {
                        client.player.networkHandler.sendChatCommand("gamemode spectator");
                        client.player.onGameModeChanged(GameMode.SPECTATOR);
                        updateAbilitiesWithDelay(client.player, true);
                    }
                }
            }
        });
    }
}
