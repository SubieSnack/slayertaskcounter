package net.runelite.client.plugins.mobkcoverlay;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("mobkcoverlay")
public interface MobKCOverlayConfig extends Config
{
    @ConfigItem(
            keyName = "trackedNpcName",
            name = "NPC name to track",
            description = "Exact or partial NPC name whose kill count you want to track."
    )
    default String trackedNpcName()
    {
        return "";
    }

    @ConfigItem(
            keyName = "automaticTracking",
            name = "Auto-switch to attacked NPC",
            description = "When enabled, automatically switch tracking to whatever NPC you attack."
    )
    default boolean automaticTracking()
    {
        return true;
    }

    @ConfigItem(
            keyName = "showNpcName",
            name = "Show NPC name",
            description = "Display the NPC name in the overlay."
    )
    default boolean showNpcName()
    {
        return true;
    }

    @ConfigItem(
            keyName = "textColor",
            name = "Text color",
            description = "Color of the overlay text."
    )
    default Color textColor()
    {
        return Color.WHITE;
    }

    @ConfigItem(
            keyName = "showPlusOne",
            name = "Show +1 animation",
            description = "Show a fading +1 when the kill count increases."
    )
    default boolean showPlusOne()
    {
        return true;
    }

    @ConfigItem(
            keyName = "animationDuration",
            name = "Animation duration (ms)",
            description = "How long the +1 animation stays visible (in milliseconds)."
    )
    default int animationDuration()
    {
        return 800;
    }

    @ConfigItem(
            keyName = "manualAddKc",
            name = "Add to KC",
            description = "Enter a (positive or negative) number to adjust the current NPC's kill count."
    )
    default int manualAddKc()
    {
        return 0;
    }
}
