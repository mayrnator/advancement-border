package net.reimaden.advancementborder;


import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.SavedDataStorage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashSet;

public final class StateSaverAndLoader extends SavedData {
    private static final String ADVANCEMENTS_KEY = "completedAdvancements";
    private static final String FRESH_WORLD_KEY = "isFreshWorld";
    public HashSet<Identifier> completedAdvancements = new HashSet<>();
    public boolean isFreshWorld = true;

    public StateSaverAndLoader() {
    }

    private StateSaverAndLoader(List<Identifier> advancements, boolean freshWorld) {
        this.completedAdvancements = new HashSet<>(advancements);
        this.isFreshWorld = freshWorld;
    }

    private static final Codec<StateSaverAndLoader> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Identifier.CODEC.listOf()
                            .fieldOf(ADVANCEMENTS_KEY)
                            .forGetter(state -> List.copyOf(state.completedAdvancements)),
                    Codec.BOOL
                            .fieldOf(FRESH_WORLD_KEY)
                            .forGetter(state -> state.isFreshWorld)
            ).apply(instance, StateSaverAndLoader::new));



    private static final SavedDataType<StateSaverAndLoader> TYPE =
            new SavedDataType<>(
                    Identifier.fromNamespaceAndPath(AdvancementBorder.MOD_ID, "state"),
                    StateSaverAndLoader::new,
                    CODEC,
                    null
            );

    public static StateSaverAndLoader getServerState(MinecraftServer server) {
        ServerLevel world = server.overworld();
        SavedDataStorage manager = world.getDataStorage();
        return manager.computeIfAbsent(TYPE);
    }
}
