package snownee.minieffects.handlers;

import lombok.val;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.potion.PotionEffect;
import org.lwjgl.input.Mouse;
import snownee.minieffects.api.Vec2i;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author ZZZank (modified by assistance)
 */
public class InjectedMiniEffects {

    // --- MODIFIED ---: Updated to reflect the new logical square icon dimensions
    public static final int ICON_WIDTH = 29;
    public static final int ICON_HEIGHT = 29; // 现在图标的逻辑高度也是29

    public final Rectangle iconArea = new Rectangle();
    public final Rectangle expandedArea = new Rectangle();
    public final GuiContainer screen;

    public boolean expanded;
    public int effectsTotal;
    public int effectsBad;
    public int positiveColor;
    public int negativeColor;

    private Vec2i cachedOffset = null;
    private int offsetTimeStamp = -1;

    public InjectedMiniEffects(GuiContainer screen) {
        this.screen = screen;
    }

    public Vec2i getOffset() {
        if (offsetTimeStamp != MiniEffectsOffsets.timeStamp()) {
            offsetTimeStamp = MiniEffectsOffsets.timeStamp();
            cachedOffset = MiniEffectsOffsets.getOrDefault(screen.getClass());
        }
        return cachedOffset;
    }

    public boolean defaultAction(int capturedLeft, int capturedTop) {
        val effectsTotalOld = this.effectsTotal;
        val mc = this.screen.mc;

        val updated = this.updateEffectCounter(mc.player.getActivePotionEffects());
        if (!updated) {
            this.effectsTotal = 0;
            return false;
        }

        if (this.effectsTotal != effectsTotalOld) {
            if (this.effectsTotal == 0 || this.expanded) {
                this.updateArea(capturedLeft, capturedTop);
            }
        }

        val shouldExpand = this.shouldExpand(mc, Mouse.getX(), Mouse.getY());
        if (this.expanded != shouldExpand) {
            this.expanded = shouldExpand;
            this.updateArea(capturedLeft, capturedTop);
        }

        if (this.effectsTotal <= 0 || shouldExpand) {
            return false;
        }
        this.renderMini();
        return true;
    }

    public boolean updateEffectCounter(Collection<PotionEffect> effects) {
        if (effects.isEmpty()) {
            return false;
        }

        this.effectsTotal = 0;
        this.effectsBad = 0;
        List<Integer> positiveColors = new ArrayList<>();
        List<Integer> negativeColors = new ArrayList<>();

        for (val effect : effects) {
            val potion = effect.getPotion();
            if (potion.shouldRender(effect)) {
                ++effectsTotal;
                if (potion.isBeneficial()) {
                    positiveColors.add(potion.getLiquidColor());
                } else {
                    ++effectsBad;
                    negativeColors.add(potion.getLiquidColor());
                }
            }
        }

        this.positiveColor = averageColor(positiveColors);
        this.negativeColor = averageColor(negativeColors);

        return true;
    }

    private int averageColor(List<Integer> colors) {
        if (colors.isEmpty()) {
            return -1;
        }
        float r = 0, g = 0, b = 0;
        for (int color : colors) {
            r += (float)(color >> 16 & 255) / 255.0F;
            g += (float)(color >> 8 & 255) / 255.0F;
            b += (float)(color & 255) / 255.0F;
        }
        r /= colors.size();
        g /= colors.size();
        b /= colors.size();
        return ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);
    }

    public boolean shouldExpand(int x, int y) {
        val shouldExpand = iconArea.contains(x, y);
        if (this.expanded) {
            return shouldExpand || expandedArea.contains(x, y);
        }
        return shouldExpand;
    }

    public boolean shouldExpand(Minecraft mc, int mouseX, int mouseY) {
        val scaledResolution = new ScaledResolution(mc);
        val scaledWidth = scaledResolution.getScaledWidth();
        val scaledHeight = scaledResolution.getScaledHeight();
        val x = mouseX * scaledWidth / mc.displayWidth;
        val y = scaledHeight - mouseY * scaledHeight / mc.displayHeight - 1;
        return shouldExpand(x, y);
    }

    public void renderMini() {
        if (expanded) {
            return;
        }
        // ICON_WIDTH and ICON_HEIGHT here are the *logical* dimensions for placement and click detection.
        // MiniEffectsRenderer now handles drawing the 29x24 texture centered within this 29x29 logical space.
        MiniEffectsRenderer.renderMini(
                screen,
                iconArea.x,
                iconArea.y,
                this.positiveColor,
                this.negativeColor,
                this.effectsTotal - this.effectsBad,
                this.effectsBad
        );
    }

    public void updateArea(int capturedLeft, int capturedTop) {
        if (expanded) {
            updateExpanded(capturedLeft, capturedTop);
        } else {
            val offset = getOffset();
            // --- MODIFIED ---: Use ICON_WIDTH for correct positioning based on new logical size
            updateFolded(screen.getGuiLeft() - ICON_WIDTH + offset.x, screen.getGuiTop() + offset.y);
        }
    }

    protected void updateFolded(int x, int y) {
        // --- MODIFIED ---: Use new width and height constants for the clickable area
        iconArea.setBounds(x, y, ICON_WIDTH, ICON_HEIGHT); // Now 29x29
    }

    protected void updateExpanded(int x, int y) {
        expandedArea.setBounds(x, y, 119, 33 * Math.min(5, effectsTotal));
    }

    public List<Rectangle> miniEff$getAreas() {
        return effectsTotal == 0
                ? Collections.emptyList()
                : Collections.singletonList(expanded ? expandedArea : iconArea);
    }

    public static class RightPin extends InjectedMiniEffects {
        public RightPin(GuiContainer screen) {
            super(screen);
        }

        @Override
        public void updateArea(int capturedLeft, int capturedTop) {
            if (expanded) {
                updateExpanded(capturedLeft, capturedTop);
            } else {
                val offset = getOffset();
                updateFolded(capturedLeft + offset.x, capturedTop + offset.y);
            }
        }
    }
}