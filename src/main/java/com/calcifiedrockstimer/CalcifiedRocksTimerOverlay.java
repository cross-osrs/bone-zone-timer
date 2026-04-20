package com.calcifiedrockstimer;

import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.*;

import javax.inject.Inject;
import java.awt.*;
import java.util.Map;

public class CalcifiedRocksTimerOverlay extends Overlay
{
    private static final Color COLOR_NORMAL = Color.WHITE;
    private static final Color COLOR_WARNING = Color.RED;
    private static final int Z_OFFSET = 150;

    private final Client client;
    private final CalcifiedRocksTimerPlugin plugin;
    private final CalcifiedRocksTimerConfig config;

    @Inject
    public CalcifiedRocksTimerOverlay(Client client,
                                       CalcifiedRocksTimerPlugin plugin,
                                       CalcifiedRocksTimerConfig config)
    {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(OverlayPriority.MED);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        for (Map.Entry<WorldPoint, GameObject> entry : plugin.activeRocks.entrySet())
        {
            WorldPoint worldPoint = entry.getKey();
            int secondsRemaining = plugin.getSecondsRemaining(worldPoint);
            int ticksRemaining = plugin.getTicksRemaining(worldPoint);

            if (secondsRemaining < 0) continue;

            String displayText = config.showTicks()
                    ? secondsRemaining + "s (" + ticksRemaining + "t)"
                    : secondsRemaining + "s";

            Color textColor = secondsRemaining <= config.warningSeconds()
                    ? COLOR_WARNING
                    : COLOR_NORMAL;

            LocalPoint localPoint = LocalPoint.fromWorld(client, worldPoint);
            if (localPoint == null) continue;

            net.runelite.api.Point canvasPoint = Perspective.getCanvasTextLocation(
                    client, graphics, localPoint, displayText, Z_OFFSET);
            if (canvasPoint == null) continue;

            graphics.setColor(Color.BLACK);
            graphics.drawString(displayText, canvasPoint.getX() + 1, canvasPoint.getY() + 1);
            graphics.setColor(textColor);
            graphics.drawString(displayText, canvasPoint.getX(), canvasPoint.getY());
        }
        return null;
    }
}