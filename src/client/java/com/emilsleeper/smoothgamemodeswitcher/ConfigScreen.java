package com.emilsleeper.smoothgamemodeswitcher;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class ConfigScreen {
    private static final Logger LOGGER = LogManager.getLogger("SmoothGamemodeSwitcher/ConfigScreen");
    
    public static Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("options.smoothgamemodeswitcher.title"))
                .setSavingRunnable(ConfigHandler::saveConfig);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        
        List<Integer> gamemodeOrder = ConfigHandler.getGamemodeOrder();

        builder.getOrCreateCategory(Component.literal(""))
            .addEntry(
                entryBuilder.startDoubleField(
                        Component.translatable("options.smoothgamemodeswitcher.disableflyingBlockTolerance"),
                        ConfigHandler.getDisableFlyingBlockTolerance()
                    )
                    .setDefaultValue(ConfigHandler.getDefaultDisableFlyingBlockTolerance())
                    .setMin(0.0)
                    .setMax(1.0)
                    .setSaveConsumer(ConfigHandler::setDisableFlyingBlockTolerance)
                    .setTooltip(Component.translatable("options.smoothgamemodeswitcher.disableflyingBlockTolerance.tooltip"))
                    .build()
            )
            .addEntry(
                entryBuilder.startIntList(
                        Component.translatable("options.smoothgamemodeswitcher.gamemodeOrder"),
                        gamemodeOrder
                    )
                    .setDefaultValue(ConfigHandler.getDefaultGamemodeOrder())
                    .setSaveConsumer(ConfigHandler::setGamemodeOrder)
                    .setMax(4)
                    .setMin(0)
                    .setTooltip(Component.translatable("options.smoothgamemodeswitcher.gamemodeOrder.tooltip"))
                    .build()
            );
            
        return builder.build();
    }
}