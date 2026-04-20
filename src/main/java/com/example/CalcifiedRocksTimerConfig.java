package com.calcifiedrockstimer;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("calcifiedrockstimer")
public interface CalcifiedRocksTimerConfig extends Config
{
    @ConfigItem(
            keyName = "showTicks",
            name = "Show Ticks",
            description = "Also show the remaining tick count (e.g. '42s (70t)')",
            position = 0
    )
    default boolean showTicks()
    {
        return false;
    }

    @ConfigItem(
            keyName = "warningSeconds",
            name = "Warning Threshold (seconds)",
            description = "Timer turns red when this many seconds or fewer remain",
            position = 1
    )
    default int warningSeconds()
    {
        return 15;
    }
}