package snownee.minieffects.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.BossInfoClient;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
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
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "minieffects", value = Side.CLIENT)
public class BossBarOverlayRenderer {

    public static final String CUSTOM_BOSSBAR_TEXTURE_FOLDER = "textures/bossbars";
    private static final Map<String, ResourceLocation> customBossBarTextures = new HashMap<>();
    private static final Map<String, Boolean> textureLoadAttempted = new HashMap<>();

    private static final int CUSTOM_TEXTURE_WIDTH = 218;
    private static final int CUSTOM_TEXTURE_HEIGHT = 19;

    @SubscribeEvent
    public static void onRenderBossInfo(RenderGameOverlayEvent.BossInfo event) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc);
        int screenWidth = sr.getScaledWidth();

        BossInfoClient bossInfo = event.getBossInfo();

        String bossDisplayName = bossInfo.getName().getUnformattedText();
        String textureIdentifier = createFileNameFromText(bossDisplayName);

        ResourceLocation customTexture = getCustomBossBarTexture(textureIdentifier);

        if (customTexture == null) {
            return;
        }

        int bossBarX = (screenWidth - CUSTOM_TEXTURE_WIDTH) / 2;
        int textureY = event.getY() - 9;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        try {
            mc.getTextureManager().bindTexture(customTexture);

            Gui.drawModalRectWithCustomSizedTexture(
                    bossBarX,
                    textureY,
                    0, 0,
                    CUSTOM_TEXTURE_WIDTH,
                    CUSTOM_TEXTURE_HEIGHT,
                    CUSTOM_TEXTURE_WIDTH,
                    CUSTOM_TEXTURE_HEIGHT
            );

        } finally {
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }

    private static ResourceLocation getCustomBossBarTexture(String textureIdentifier) {
        if (MiniEffects.modConfigDirectory == null) {
            return null;
        }

        if (textureLoadAttempted.containsKey(textureIdentifier)) {
            return customBossBarTextures.get(textureIdentifier);
        }

        File textureFile = new File(
                new File(MiniEffects.modConfigDirectory, CUSTOM_BOSSBAR_TEXTURE_FOLDER),
                textureIdentifier + ".png"
        );

        if (textureFile.exists() && textureFile.isFile()) {
            try (FileInputStream fis = new FileInputStream(textureFile)) {
                BufferedImage image = ImageIO.read(fis);
                if (image != null) {
                    DynamicTexture dynamicTexture = new DynamicTexture(image);
                    ResourceLocation resourceLocation = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(
                            "minieffects_custom_bossbar_" + textureIdentifier, dynamicTexture);

                    customBossBarTextures.put(textureIdentifier, resourceLocation);
                    textureLoadAttempted.put(textureIdentifier, true);
                    MiniEffects.LOGGER.info("Loaded custom boss bar texture for identifier: " + textureIdentifier + " from file: " + textureFile.getName());
                    return resourceLocation;
                }
            } catch (IOException e) {
                MiniEffects.LOGGER.error("Failed to load custom boss bar texture for identifier: " + textureIdentifier + " from file: " + textureFile.getName(), e);
            }
        }

        textureLoadAttempted.put(textureIdentifier, false);
        return null;
    }

    private static String createFileNameFromText(String rawText) {
        return rawText.replace(" ", "_");
    }
}