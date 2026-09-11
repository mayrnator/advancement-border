package net.reimaden.advancementborder;

import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.Configurable;
import org.jetbrains.annotations.NotNull;

@Config(id = AdvancementBorder.MOD_ID)
public final class AdvancementBorderConfig {
    @Configurable
    @Configurable.Comment(value = "How much the world border should expand with each task advancement", localize = true)
    @Configurable.DecimalRange(min = 0.0, max = 1000.0)
    public double increaseAmountTask = 2.0;

    @Configurable
    @Configurable.Comment(value = "How much the world border should expand with each goal advancement", localize = true)
    @Configurable.DecimalRange(min = 0.0, max = 1000.0)
    public double increaseAmountGoal = 2.0;

    @Configurable
    @Configurable.Comment(value = "How much the world border should expand with each challenge advancement", localize = true)
    @Configurable.DecimalRange(min = 0.0, max = 1000.0)
    public double increaseAmountChallenge = 2.0;

    @Configurable
    @Configurable.Comment("Border expansion duration in seconds. 0 makes expansion instant")
    @Configurable.DecimalRange(min = 0.0, max = 60.0)
    public double expansionDurationSeconds = 1.0;

    public enum NotificationStyle {
        CHAT,
        ACTION_BAR,
        NONE
    }

    @Configurable
    @Configurable.Comment(value = "Whether to send world border notifications to the chat, action bar, or not at all", localize = true)
    public NotificationStyle notificationStyle = NotificationStyle.CHAT;

    @Configurable
    @Configurable.Comment(value = "Whether to display the amount of blocks the world border expands", localize = true)
    public boolean detailedNotifications = false;

    @Configurable
    @Configurable.Comment(value = "Color of world border notifications in hexadecimal", localize = true)
    @Configurable.Gui.ColorValue
    @Configurable.StringPattern(value = "^#[A-Fa-f0-9]{6}$", errorDescriptor = "config.advancementborder.option.notificationColor.error")
    public String notificationColor = "#55FFFF"; // Minecraft Aqua

    @Configurable
    @Configurable.Comment(value = "Whether each player contributes separately instead of using a global list of advancements", localize = true)
    public boolean perPlayerAdvancements = false;

    @Configurable
    @Configurable.Comment(value = {"A list of advancements that should expand the world border", "Leave empty to allow all advancements to expand the world border"}, localize = true)
    @Configurable.StringPattern(value = "^[a-z0-9_\\-.]+:[a-z0-9_\\-./]+$", defaultValue = "minecraft:story/root", errorDescriptor = "config.advancementborder.option.advancementWhitelist.error")
    @NotNull
    public String[] advancementWhitelist = new String[0];

    @Configurable
    @Configurable.Comment(value = "Make the whitelist act as a blacklist, preventing the advancements in it from expanding the world border", localize = true)
    public boolean invertList = false;

    @Configurable
    public WorldBorderSetup worldBorderSetup = new WorldBorderSetup();

    @Configurable
    public Dimensions dimensions = new Dimensions();

    public static class Dimensions {
        @Configurable
        @Configurable.Comment("Manage the Overworld border")
        public boolean overworld = true;

        @Configurable
        @Configurable.Comment("Manage the Nether border")
        public boolean nether = true;

        @Configurable
        @Configurable.Comment("Manage the End border")
        public boolean end = true;
    }

    public static class WorldBorderSetup {
        @Configurable(key = Configurable.LocalizationKey.FULL)
        @Configurable.Comment(value = {"Let " + AdvancementBorder.MOD_NAME + " handle setting up the world border for new worlds", "This sets the size of the world border and centers it on the world spawn"}, localize = true)
        public boolean automate = false;

        @Configurable(key = Configurable.LocalizationKey.FULL)
        @Configurable.Comment(value = "The initial size of the world border", localize = true)
        @Configurable.DecimalRange(min = 1.0, max = 59999968.0)
        public double initialSize = 10.0;
    }
}
