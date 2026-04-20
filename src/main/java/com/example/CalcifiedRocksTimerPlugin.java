package com.calcifiedrockstimer;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@PluginDescriptor(
        name = "Calcified Rocks Timer",
        description = "Shows a countdown timer on Calcified Rocks, turning red when less than 15 seconds remain",
        tags = {"calcified", "rocks", "timer", "fossil island", "wyvern cave"}
)
public class CalcifiedRocksTimerPlugin extends Plugin
{
    static final int[] CALCIFIED_ROCK_IDS = {51485, 51487, 51489, 51491};
    static final int TIMER_TICKS = 117;
    static final int TIMER_SECONDS = 70;
    static final int WARNING_SECONDS = 15;
    static final int WARNING_TICKS = 25;

    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private CalcifiedRocksTimerOverlay overlay;

    @Inject
    private CalcifiedRocksTimerConfig config;

    final Map<WorldPoint, Integer> rockSpawnTicks = new HashMap<>();
    final Map<WorldPoint, GameObject> activeRocks = new HashMap<>();

    @Provides
    CalcifiedRocksTimerConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(CalcifiedRocksTimerConfig.class);
    }

    @Override
    protected void startUp()
    {
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown()
    {
        overlayManager.remove(overlay);
        rockSpawnTicks.clear();
        activeRocks.clear();
    }

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event)
    {
        GameObject gameObject = event.getGameObject();
        if (isCalcifiedRock(gameObject.getId()))
        {
            WorldPoint location = gameObject.getWorldLocation();
            rockSpawnTicks.put(location, client.getTickCount());
            activeRocks.put(location, gameObject);
        }
    }

    @Subscribe
    public void onGameObjectDespawned(GameObjectDespawned event)
    {
        GameObject gameObject = event.getGameObject();
        if (isCalcifiedRock(gameObject.getId()))
        {
            WorldPoint location = gameObject.getWorldLocation();
            rockSpawnTicks.remove(location);
            activeRocks.remove(location);
        }
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        int currentTick = client.getTickCount();
        rockSpawnTicks.entrySet().removeIf(entry ->
        {
            int ticksElapsed = currentTick - entry.getValue();
            if (ticksElapsed > TIMER_TICKS)
            {
                activeRocks.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }

    static boolean isCalcifiedRock(int objectId)
    {
        for (int id : CALCIFIED_ROCK_IDS)
        {
            if (id == objectId)
            {
                return true;
            }
        }
        return false;
    }

    int getTicksRemaining(WorldPoint location)
    {
        Integer spawnTick = rockSpawnTicks.get(location);
        if (spawnTick == null) return -1;
        int elapsed = client.getTickCount() - spawnTick;
        return Math.max(0, TIMER_TICKS - elapsed);
    }

    int getSecondsRemaining(WorldPoint location)
    {
        int ticksRemaining = getTicksRemaining(location);
        if (ticksRemaining < 0) return -1;
        return (int) Math.ceil(ticksRemaining * 0.6);
    }
}