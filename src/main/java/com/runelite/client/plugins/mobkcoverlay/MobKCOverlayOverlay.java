package net.runelite.client.plugins.mobkcoverlay;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.time.Instant;
import javax.inject.Inject;

import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;

public class MobKCOverlayOverlay extends Overlay
{
    private final MobKCOverlayPlugin plugin;
    private final MobKCOverlayConfig config;
    private final PanelComponent panelComponent = new PanelComponent();

    @Inject
    public MobKCOverlayOverlay(MobKCOverlayPlugin plugin, MobKCOverlayConfig config)
    {
        this.plugin = plugin;
        this.config = config;

        setLayer(OverlayLayer.ABOVE_SCENE);
        setPosition(OverlayPosition.TOP_LEFT);
        setPriority(OverlayPriority.MED);
        setMovable(true);

        // Fixed width so the +1 doesn't wrap onto the next line
        panelComponent.setPreferredSize(new Dimension(150, 0));
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        // If nothing is configured/being tracked, don't show anything
        if (!plugin.isTracking())
        {
            return null;
        }

        graphics.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );

        panelComponent.getChildren().clear();

        String npcName = plugin.getCurrentNpcName();
        if (npcName == null || npcName.trim().isEmpty())
        {
            return null;
        }

        String displayName = config.showNpcName() ? npcName.trim() : "KC";
        int kc = plugin.getCurrentKillCount();

        String leftText = String.format("%s - Total Slain: %d", displayName, kc);

        // Right side: +1 animation
        String rightText = null;
        Color rightColor = null;

        Instant lastKill = plugin.getLastKillTime();
        if (config.showPlusOne() && lastKill != null)
        {
            long elapsedMs = Instant.now().toEpochMilli() - lastKill.toEpochMilli();

            if (elapsedMs < config.animationDuration())
            {
                double progress = (double) elapsedMs / config.animationDuration();
                int alpha = (int) (255 * (1.0 - progress));
                alpha = Math.max(0, Math.min(255, alpha));

                rightText = "+1";
                rightColor = new Color(0, 255, 0, alpha);
            }
        }

        LineComponent.LineComponentBuilder lineBuilder = LineComponent.builder()
                .left(leftText)
                .leftColor(config.textColor());

        if (rightText != null)
        {
            lineBuilder
                    .right(rightText)
                    .rightColor(rightColor);
        }

        panelComponent.getChildren().add(lineBuilder.build());

        return panelComponent.render(graphics);
    }
}
