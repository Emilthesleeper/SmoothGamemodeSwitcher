package com.emilsleeper;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigScreen {
    private static final Logger LOGGER = LogManager.getLogger("SmoothGamemodeSwitcher/ConfigScreen");
    
    public static Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("options.smoothgamemodeswitcher.title"))
                .setSavingRunnable(ConfigHandler::saveConfig);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        
        List<Integer> gamemodeOrder = ConfigHandler.getGamemodeOrder();

        builder.getOrCreateCategory(Text.of(""))
            .addEntry(
                entryBuilder.startDoubleField(
                        Text.translatable("options.smoothgamemodeswitcher.disableflyingBlockTolerance"),
                        ConfigHandler.getDisableFlyingBlockTolerance()
                    )
                    .setDefaultValue(ConfigHandler.getDefaultDisableFlyingBlockTolerance())
                    .setMin(0.0)
                    .setMax(1.0)
                    .setSaveConsumer(ConfigHandler::setDisableFlyingBlockTolerance)
                    .setTooltip(Text.translatable("options.smoothgamemodeswitcher.disableflyingBlockTolerance.tooltip"))
                    .build()
            )
            .addEntry(
                entryBuilder.startIntList(
                        Text.translatable("options.smoothgamemodeswitcher.gamemodeOrder"),
                        gamemodeOrder
                    )
                    .setDefaultValue(ConfigHandler.getDefaultGamemodeOrder())
                    .setSaveConsumer(ConfigHandler::setGamemodeOrder)
                    .setMax(4)
                    .setMin(0)
                    .setTooltip(Text.translatable("options.smoothgamemodeswitcher.gamemodeOrder.tooltip"))
                    .build()
            );
            
        return builder.build();
    }
}