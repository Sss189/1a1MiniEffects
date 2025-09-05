package snownee.minieffects.handlers;

import com.abyess.api.AbyessAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import snownee.minieffects.MiniEffects;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "minieffects", value = Side.CLIENT)
public class PotionHudRenderer {

    private static final ResourceLocation HUD_BG =
            new ResourceLocation("minieffects", "textures/gui/minipotion_hud_bg.png");
    private static final ResourceLocation HUD_BG_REPEATING =
            new ResourceLocation("minieffects", "textures/gui/minipotion_hud_bg_repeating.png");
    private static final ResourceLocation HUD_NEGATIVE_BG =
            new ResourceLocation("minieffects", "textures/gui/minipotion_hud_negative_bg.png");
    private static final ResourceLocation HUD_NEGATIVE_REPEATING_BG =
            new ResourceLocation("minieffects", "textures/gui/minipotion_hud_negative_repeating_bg.png");

    private static final ResourceLocation HUD_BG_OVERLAY =
            new ResourceLocation("minieffects", "textures/gui/minipotion_hud_bg2.png");
    private static final ResourceLocation HUD_BG_REPEATING_OVERLAY =
            new ResourceLocation("minieffects", "textures/gui/minipotion_hud_bg_repeating2.png");
    private static final ResourceLocation HUD_NEGATIVE_BG_OVERLAY =
            new ResourceLocation("minieffects", "textures/gui/minipotion_hud_negative_bg2.png");
    private static final ResourceLocation HUD_NEGATIVE_REPEATING_BG_OVERLAY =
            new ResourceLocation("minieffects", "textures/gui/minipotion_hud_negative_repeating_bg2.png");

    private static final ResourceLocation HUD_TIMER =
            new ResourceLocation("minieffects", "textures/gui/minipotion_hud_timer.png");
    private static final ResourceLocation HUD_TIMER_REPEATING =
            new ResourceLocation("minieffects", "textures/gui/minipotion_hud_timer_repeating.png");
    private static final ResourceLocation HUD_NEGATIVE_TIMER =
            new ResourceLocation("minieffects", "textures/gui/minipotion_hud_negative_timer.png");
    private static final ResourceLocation HUD_NEGATIVE_REPEATING_TIMER =
            new ResourceLocation("minieffects", "textures/gui/minipotion_hud_negative_repeating_timer.png");
    private static final ResourceLocation INVENTORY_GUI =
            new ResourceLocation("textures/gui/container/inventory.png");

    private static final ResourceLocation HUD_NEGATIVE_DIMENSIONCURSE_BG =
            new ResourceLocation("minieffects", "textures/gui/minipotion_hud_negative_dimensioncurse_bg.png");
    private static final ResourceLocation EFFECT_DIMENSIONCURSE =
            new ResourceLocation("minieffects", "textures/gui/effect_dimensioncurse.png");
    private static final ResourceLocation HUD_NEGATIVE_DIMENSIONCURSE_TIMER =
            new ResourceLocation("minieffects", "textures/gui/minipotion_hud_negative_dimensioncurse_timer.png");
    private static final ResourceLocation HUD_NEGATIVE_DIMENSIONCURSE_BG2_OVERLAY =
            new ResourceLocation("minieffects", "textures/gui/minipotion_hud_negative_dimensioncurse_bg2.png");
    private static final String DIMENSION_CURSE_ID = "abyess:dimension_curse";


    private static final int BG_WIDTH = 24;
    private static final int BG_HEIGHT = 24;
    private static final int TIMER_WIDTH = 26;
    private static final int TIMER_HEIGHT = 14;
    private static final int ICON_SIZE = 18;
    private static final int TIMER_BG_GAP = 1;
    private static final float RENDER_Z = 100F;
    private static final int H_SPACING = 3;
    private static final int V_SPACING = 2;

    private static final int NEGATIVE_TIMER_COLOR = 0xFFFD5505;

    private static final int INFINITE_DURATION_TICKS_THRESHOLD = 1500 * 20;
    private static final int FLASH_TICKS_THRESHOLD = 15 * 20;
    private static final int FADE_TICKS_THRESHOLD = 5 * 20;
    private static final int FINAL_DISAPPEAR_TICKS = 3;
    private static final float FLASH_FREQUENCY = 1f;
    private static final float LEVEL_SCALE = 1f;

    private static final Map<String, ResourceLocation> customPotionTextures = new HashMap<>();
    private static final Map<String, Boolean> textureLoadAttempted = new HashMap<>();

    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return;

        if (mc.currentScreen != null) {
            if (mc.currentScreen instanceof GuiChat) {
            } else {
                boolean isOptiFineChat = false;
                try {
                    Class<?> guiChatOFClass = Class.forName("net.optifine.gui.GuiChatOF");
                    if (guiChatOFClass.isInstance(mc.currentScreen)) {
                        isOptiFineChat = true;
                    }
                } catch (Throwable e) {
                }

                if (!isOptiFineChat) {
                    return;
                }
            }
        }

        Collection<PotionEffect> effectsToRender = mc.player.getActivePotionEffects();

        try {
            boolean isInCursedLayerWithPositiveValue = AbyessAPI.isInCursedLayerWithValue(mc.player);
            boolean hasScapegoat = AbyessAPI.hasScapegoatItem(mc.player);

            if (isInCursedLayerWithPositiveValue && !hasScapegoat) {
                PotionEffect dimensionCurseEffect = new PotionEffect(null, INFINITE_DURATION_TICKS_THRESHOLD, 0, false, false) {
                    @Override
                    public String getEffectName() {
                        return DIMENSION_CURSE_ID;
                    }
                };

                List<PotionEffect> tempEffects = new ArrayList<>(effectsToRender);
                tempEffects.add(dimensionCurseEffect);
                effectsToRender = tempEffects;
            }
        } catch (Throwable e) {
        }

        renderAllPotionHuds(mc, effectsToRender);
    }

    public static void renderAllPotionHuds(Minecraft mc, Collection<PotionEffect> effects) {
        if (mc.player == null) return;

        List<PotionEffect> good = new ArrayList<>();
        List<PotionEffect> bad = new ArrayList<>();
        for (PotionEffect e : effects) {
            if (e.getPotion() != null && !e.getPotion().shouldRender(e)) continue;
            if (e.getPotion() != null && e.getPotion().isBeneficial()) good.add(e);
            else bad.add(e);
        }

        Comparator<PotionEffect> cmp = (a, b) -> {
            boolean ia = a.getIsAmbient() || a.getDuration() >= INFINITE_DURATION_TICKS_THRESHOLD;
            boolean ib = b.getIsAmbient() || b.getDuration() >= INFINITE_DURATION_TICKS_THRESHOLD;
            if (ia ^ ib) return ia ? -1 : 1;
            String registryNameA = (a.getPotion() != null) ? a.getPotion().getRegistryName().toString() : a.getEffectName();
            String registryNameB = (b.getPotion() != null) ? b.getPotion().getRegistryName().toString() : b.getEffectName();
            return registryNameA.compareTo(registryNameB);
        };
        good.sort(cmp);
        bad.sort(cmp);
        if (good.isEmpty() && bad.isEmpty()) return;

        ScaledResolution sr = new ScaledResolution(mc);
        int w = sr.getScaledWidth();
        int y = 2;

        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.translate(0, 0, RENDER_Z);

        if (!good.isEmpty()) {
            int x = w - 2 - BG_WIDTH;
            for (PotionEffect e : good) {
                renderSingle(mc, e, x, y);
                x -= BG_WIDTH + H_SPACING;
            }
        }
        if (!good.isEmpty() && !bad.isEmpty()) {
            y += BG_HEIGHT + TIMER_HEIGHT + TIMER_BG_GAP + V_SPACING;
        }
        if (!bad.isEmpty()) {
            int x = w - 2 - BG_WIDTH;
            if (good.isEmpty()) y = 2;
            for (PotionEffect e : bad) {
                renderSingle(mc, e, x, y);
                x -= BG_WIDTH + H_SPACING;
            }
        }

        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    private static void renderSingle(Minecraft mc, PotionEffect effect, int x, int y) {
        boolean good = effect.getPotion() != null && effect.getPotion().isBeneficial();
        boolean inf = effect.getIsAmbient() || effect.getDuration() >= INFINITE_DURATION_TICKS_THRESHOLD;
        int duration = effect.getDuration();

        boolean isDimensionCurse = DIMENSION_CURSE_ID.equals(effect.getEffectName());

        if (!inf && duration <= FINAL_DISAPPEAR_TICKS && !isDimensionCurse) {
            return;
        }
        if (!inf && duration <= 0 && !isDimensionCurse) return;

        float baseAlpha = 1.0f;
        if (!inf && duration < FADE_TICKS_THRESHOLD && !isDimensionCurse) {
            baseAlpha = (float) duration / FADE_TICKS_THRESHOLD;
        }

        float flashAlpha = baseAlpha;
        if (!inf && duration < FLASH_TICKS_THRESHOLD && !isDimensionCurse) {
            long worldTime = mc.world.getTotalWorldTime();
            float phase = worldTime / (20f / FLASH_FREQUENCY) * (float) Math.PI * 2f;
            float flashFactor = (float) (Math.cos(phase) * 0.5 + 0.5);
            flashAlpha = baseAlpha * (0.3f + 0.7f * flashFactor);
        }

        float finalAlpha = MathHelper.clamp(flashAlpha, 0f, 1f);
        int alphaByte = Math.round(finalAlpha * 255) & 0xFF;

        ResourceLocation bg;
        ResourceLocation bgOverlay = null;
        ResourceLocation tm;

        if (isDimensionCurse) {
            bg = HUD_NEGATIVE_DIMENSIONCURSE_BG;

            tm = HUD_NEGATIVE_DIMENSIONCURSE_TIMER;
        } else {
            bg = good
                    ? (inf ? HUD_BG_REPEATING : HUD_BG)
                    : (inf ? HUD_NEGATIVE_REPEATING_BG : HUD_NEGATIVE_BG);

            int level = effect.getAmplifier() + 1;
            if (level > 1) {
                bgOverlay = good
                        ? (inf ? HUD_BG_REPEATING_OVERLAY : HUD_BG_OVERLAY)
                        : (inf ? HUD_NEGATIVE_REPEATING_BG_OVERLAY : HUD_NEGATIVE_REPEATING_BG_OVERLAY);
            }

            tm = good
                    ? (inf ? HUD_TIMER_REPEATING : HUD_TIMER)
                    : (inf ? HUD_NEGATIVE_REPEATING_TIMER : HUD_NEGATIVE_TIMER);
        }

        GlStateManager.pushMatrix();
        mc.getTextureManager().bindTexture(bg);
        GlStateManager.color(1, 1, 1, finalAlpha);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, BG_WIDTH, BG_HEIGHT, BG_WIDTH, BG_HEIGHT);

        ResourceLocation potionIconResource;
        if (isDimensionCurse) {
            potionIconResource = EFFECT_DIMENSIONCURSE;
        } else {
            if (effect.getPotion() != null) {
                potionIconResource = getCustomPotionTexture(effect.getPotion().getRegistryName().toString());
            } else {
                potionIconResource = null;
            }
        }

        if (potionIconResource != null) {
            mc.getTextureManager().bindTexture(potionIconResource);
            int ix = x + (BG_WIDTH - ICON_SIZE) / 2;
            int iy = y + (BG_HEIGHT - ICON_SIZE) / 2;
            GlStateManager.color(1, 1, 1, finalAlpha);
            Gui.drawModalRectWithCustomSizedTexture(ix, iy, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        } else if (!isDimensionCurse) {
            if (effect.getPotion() != null) {
                int icon = effect.getPotion().getStatusIconIndex();
                if (icon >= 0) {
                    mc.getTextureManager().bindTexture(INVENTORY_GUI);
                    int u = (icon % 8) * ICON_SIZE;
                    int v = 198 + (icon / 8) * ICON_SIZE;
                    int ix = x + (BG_WIDTH - ICON_SIZE) / 2;
                    int iy = y + (BG_HEIGHT - ICON_SIZE) / 2;
                    GlStateManager.color(1, 1, 1, finalAlpha);
                    Gui.drawModalRectWithCustomSizedTexture(ix, iy, u, v, ICON_SIZE, ICON_SIZE, 256, 256);
                }
            }
        }


        if (bgOverlay != null) {
            mc.getTextureManager().bindTexture(bgOverlay);
            GlStateManager.color(1, 1, 1, finalAlpha);
            Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, BG_WIDTH, BG_HEIGHT, BG_WIDTH, BG_HEIGHT);
        }

        int level = effect.getAmplifier() + 1;
        if (level > 1 && !isDimensionCurse) {
            String levelStr = toRoman(level);
            int baseColor = good ? 0xFFFFFF : NEGATIVE_TIMER_COLOR;
            int colorWithAlpha = (alphaByte << 24) | (baseColor & 0x00FFFFFF);
            GlStateManager.pushMatrix();
            GlStateManager.scale(LEVEL_SCALE, LEVEL_SCALE, 1f);
            int sw = mc.fontRenderer.getStringWidth(levelStr);
            float sx = (x + BG_WIDTH / 2f - sw * LEVEL_SCALE / 2f) / LEVEL_SCALE;
            float sy = (float) ((y + BG_HEIGHT - 1.5 - mc.fontRenderer.FONT_HEIGHT) / LEVEL_SCALE);
            mc.fontRenderer.drawStringWithShadow(levelStr, (int) sx, (int) sy, colorWithAlpha);
            GlStateManager.popMatrix();
        }

        int tx = x - (TIMER_WIDTH - BG_WIDTH) / 2;
        int ty = y + BG_HEIGHT + TIMER_BG_GAP;
        mc.getTextureManager().bindTexture(tm);
        GlStateManager.color(1, 1, 1, finalAlpha);
        Gui.drawModalRectWithCustomSizedTexture(tx, ty, 0, 0, TIMER_WIDTH, TIMER_HEIGHT, TIMER_WIDTH, TIMER_HEIGHT);

        if (!inf && !isDimensionCurse) {
            String time = formatTime(duration);
            int baseColor = good ? 0xFFFFFF : NEGATIVE_TIMER_COLOR;
            int colorWithAlpha = (alphaByte << 24) | (baseColor & 0x00FFFFFF);
            int textX = tx + (TIMER_WIDTH - mc.fontRenderer.getStringWidth(time)) / 2;
            int textY = ty + (TIMER_HEIGHT - mc.fontRenderer.FONT_HEIGHT) / 2 + 1;
            mc.fontRenderer.drawStringWithShadow(time, textX, textY, colorWithAlpha);
        }

        GlStateManager.popMatrix();
    }

    private static ResourceLocation getCustomPotionTexture(String potionIdentifier) {
        if (MiniEffects.modConfigDirectory == null) {
            return null;
        }

        if (textureLoadAttempted.containsKey(potionIdentifier)) {
            return customPotionTextures.get(potionIdentifier);
        }

        File textureFile = new File(
                new File(MiniEffects.modConfigDirectory, MiniEffects.CUSTOM_POTION_TEXTURE_FOLDER),
                potionIdentifier.replace(":", "__") + ".png"
        );

        if (textureFile.exists() && textureFile.isFile()) {
            try (FileInputStream fis = new FileInputStream(textureFile)) {
                BufferedImage image = ImageIO.read(fis);
                if (image != null) {
                    DynamicTexture dynamicTexture = new DynamicTexture(image);
                    ResourceLocation resourceLocation = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(
                            "minieffects_custom_potion_" + potionIdentifier.replace(":", "_"), dynamicTexture);

                    customPotionTextures.put(potionIdentifier, resourceLocation);
                    textureLoadAttempted.put(potionIdentifier, true);
                    return resourceLocation;
                }
            } catch (IOException e) {
            }
        }

        textureLoadAttempted.put(potionIdentifier, false);
        return null;
    }

    private static String formatTime(int ticks) {
        int totalSeconds = ticks / 20;
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        if (hours > 0) return String.format("%dh", hours);
        else if (minutes >= 10) return String.format("%dm", minutes);
        else return String.format("%d:%02d", minutes, seconds);
    }

    private static String toRoman(int num) {
        if (num < 1 || num > 3999) return String.valueOf(num);
        String[] thousands = {"", "M", "MM", "MMM"};
        String[] hundreds = {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCC", "CM"};
        String[] tens = {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
        String[] units = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};
        return thousands[num / 1000] + hundreds[(num % 1000) / 100] + tens[(num % 100) / 10] + units[num % 10];
    }
}