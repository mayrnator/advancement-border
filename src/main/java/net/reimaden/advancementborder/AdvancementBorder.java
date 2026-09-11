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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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
            ServerLevel overworld = server.overworld();
            BlockPos spawn = overworld.getLevelData().getRespawnData().pos();

            double initialSize = config.worldBorderSetup.initialSize;
            double offset = initialSize % 2 != 0 ? 0.5 : 0.0;
            double overworldCenterX = spawn.getX() + offset;
            double overworldCenterZ = spawn.getZ() + offset;

            List<ServerLevel> levels = enabledLevels(server);

            for (ServerLevel target : levels) {
                double centerX;
                double centerZ;

                if (target.dimension().equals(Level.END)) {
                    BlockPos arrival = ServerLevel.END_SPAWN_POINT;
                    centerX = arrival.getX() + offset;
                    centerZ = arrival.getZ() + offset;
                } else {
                    double scale = DimensionType.getTeleportationScale(
                            overworld.dimensionType(), target.dimensionType()
                    );
                    centerX = overworldCenterX * scale;
                    centerZ = overworldCenterZ * scale;
                }

                WorldBorder border = target.getWorldBorder();
                border.setCenter(centerX, centerZ);
                border.setSize(initialSize);
            }

            // Keep the existing correction for the first Overworld spawn.
            if (level == overworld && levels.contains(overworld)) {
                WorldBorder border = overworld.getWorldBorder();
                if (border.getDistanceToBorder(player) < 0.0) {
                    player.teleportTo(overworldCenterX, spawn.getY(), overworldCenterZ);
                }
            }

            if (!levels.isEmpty()) {
                sendNotification(server.getPlayerList(), ".setup");
            }
        }
    }

    private static List<ServerLevel> enabledLevels(MinecraftServer server) {
        List<ServerLevel> levels = new ArrayList<>();
        if (config.dimensions.overworld) {
            levels.add(server.overworld());
        }

        ServerLevel nether = server.getLevel(Level.NETHER);
        if (config.dimensions.nether && nether != null) {
            levels.add(nether);
        }

        ServerLevel end = server.getLevel(Level.END);
        if (config.dimensions.end && end != null) {
            levels.add(end);
        }
        return levels;
    }

    public static boolean expandBorders(MinecraftServer server, double increase) {
        List<ServerLevel> levels = enabledLevels(server);
        if (levels.isEmpty()) {
            return false;
        }

        // Include any growth still pending from an earlier advancement.
        WorldBorder reference = levels.getFirst().getWorldBorder();
        double newSize = reference.getLerpTarget() + increase;
        long durationTicks = Math.round(config.expansionDurationSeconds * 20.0);

        for (ServerLevel level : levels) {
            WorldBorder border = level.getWorldBorder();
            if (durationTicks <= 0) {
                border.setSize(newSize);
            } else {
                border.lerpSizeBetween(
                        border.getSize(), newSize, durationTicks, level.getGameTime()
                );
            }
        }
        return true;
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
