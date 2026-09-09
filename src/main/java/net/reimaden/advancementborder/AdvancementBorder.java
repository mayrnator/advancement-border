package net.reimaden.advancementborder;

import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.ConfigHolder;
import dev.toma.configuration.config.format.ConfigFormats;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.border.WorldBorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AdvancementBorder implements ModInitializer {
    public static final String MOD_ID = "advancementborder";
    public static final String MOD_NAME = "Advancement Border";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static AdvancementBorderConfig config;

    @Override
    public void onInitialize() {
        ConfigHolder<AdvancementBorderConfig> configHolder = Configuration.registerConfig(AdvancementBorderConfig.class, ConfigFormats.YAML);
        config = configHolder.getConfigInstance();

        ServerEntityEvents.ENTITY_LOAD.register(this::newWorldEvent);
    }

    // We're using ServerEntityEvents.ENTITY_LOAD for timing reasons
    // By only checking for players, this shouldn't cause any performance issues
    private void newWorldEvent(Entity entity, ServerLevel level) {
        if (!(entity instanceof ServerPlayer player)) return;

        MinecraftServer server = level.getServer();
        StateSaverAndLoader serverState = StateSaverAndLoader.getServerState(server);
        // Let's check if the world is brand new to determine if the initial border setup needs to be done
        boolean isNew = serverState.isFreshWorld;
        if (!isNew) return;
        // Set world as not new after so that the following is only done once
        serverState.isFreshWorld = false;
        serverState.setDirty();

        if (config.worldBorderSetup.automate) {
            WorldBorder border = level.getWorldBorder();
            // Set size according to the config
            double initialSize = config.worldBorderSetup.initialSize;
            border.setSize(initialSize);
            // Center on world spawn, with a 0.5 offset if the size is odd
            BlockPos pos = level.getLevelData().getRespawnData().pos();
            double centerX = pos.getX();
            double centerZ = pos.getZ();
            if (initialSize % 2 != 0) { // Check if size is odd
                centerX += 0.5;
                centerZ += 0.5;
            }
            border.setCenter(centerX, centerZ);
            // Prevent the player from spawning outside the border
            if (border.getDistanceToBorder(player) < 0.0) {
                LOGGER.debug("Player spawned outside the world border! {} {} {}",
                        player.getX(), player.getY(), player.getZ());
                player.teleportTo(centerX, pos.getY(), centerZ);
            }
            // Send a message to inform the first player who joined
            sendNotification(server.getPlayerList(), ".setup");
        }
    }

    public static void sendNotification(PlayerList playerList, String key, Object... args) {
        if (switch (config.notificationStyle) {
            case CHAT, ACTION_BAR -> true;
            case NONE -> false;
        }) {
            int color = Integer.parseInt(config.notificationColor.substring(1), 16);
            playerList.broadcastSystemMessage(
                    Component.translatable(AdvancementBorder.MOD_ID + key, args).withColor(color),
                    config.notificationStyle.equals(AdvancementBorderConfig.NotificationStyle.ACTION_BAR)
            );
        }
    }
}
