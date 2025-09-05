package snownee.minieffects.handlers;

import lombok.val;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;


public class MiniEffectsRenderer {


    private static final ResourceLocation MINIPOTION_BG = new ResourceLocation("minieffects", "textures/gui/minipotion_bg.png");
    private static final ResourceLocation MINIPOTION_POTION = new ResourceLocation("minieffects", "textures/gui/minipotion_potion.png");
    private static final ResourceLocation MINIPOTION_POTION_TINT = new ResourceLocation("minieffects", "textures/gui/minipotion_potion_tint.png");
    private static final ResourceLocation MINIPOTION_POTIONS = new ResourceLocation("minieffects", "textures/gui/minipotion_potions.png");
    private static final ResourceLocation MINIPOTION_POTIONS_TINT_POSITIVE = new ResourceLocation("minieffects", "textures/gui/minipotion_potions_tint_positive.png");
    private static final ResourceLocation MINIPOTION_POTIONS_TINT_NEGATIVE = new ResourceLocation("minieffects", "textures/gui/minipotion_potions_tint_negative.png");

    private static final int TEXTURE_WIDTH = 29;
    private static final int TEXTURE_HEIGHT = 24;
    private static final int ICON_LOGICAL_SIZE = 29;


    public static void renderMini(GuiContainer screen, int x, int y, int positiveColor, int negativeColor, int positiveCount, int negativeCount) {
        val mc = screen.mc;
        val hasPositive = positiveCount > 0;
        val hasNegative = negativeCount > 0;


        GlStateManager.pushMatrix();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(true);



        int drawYOffset = (ICON_LOGICAL_SIZE - TEXTURE_HEIGHT) / 2;


        mc.getTextureManager().bindTexture(MINIPOTION_BG);
        Gui.drawModalRectWithCustomSizedTexture(x, y + drawYOffset, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);


        if (hasPositive && hasNegative) {

            mc.getTextureManager().bindTexture(MINIPOTION_POTIONS);
            Gui.drawModalRectWithCustomSizedTexture(x, y + drawYOffset, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);


            setColor(positiveColor);
            mc.getTextureManager().bindTexture(MINIPOTION_POTIONS_TINT_POSITIVE);
            Gui.drawModalRectWithCustomSizedTexture(x, y + drawYOffset, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F); // 绘制完增益染色后重置颜色


            setColor(negativeColor);
            mc.getTextureManager().bindTexture(MINIPOTION_POTIONS_TINT_NEGATIVE);
            Gui.drawModalRectWithCustomSizedTexture(x, y + drawYOffset, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F); // 绘制完减益染色后重置颜色

        } else if (hasPositive || hasNegative) {

            mc.getTextureManager().bindTexture(MINIPOTION_POTION);
            Gui.drawModalRectWithCustomSizedTexture(x, y + drawYOffset, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);


            int activeColor = hasPositive ? positiveColor : negativeColor;
            setColor(activeColor);
            mc.getTextureManager().bindTexture(MINIPOTION_POTION_TINT);
            Gui.drawModalRectWithCustomSizedTexture(x, y + drawYOffset, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F); // 绘制完单瓶子染色后重置颜色
        }


        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);


        int textX = x + 27;
        int textY = y + 14;


        if (hasPositive) {
            val s = Integer.toString(positiveCount);
            mc.fontRenderer.drawStringWithShadow(s, textX - mc.fontRenderer.getStringWidth(s), textY + 1, 16777215); // 纯白色
            textY -= 10;
        }


        if (hasNegative) {
            if (!hasPositive) {
                textY = y + 14;
            }
            val s = Integer.toString(negativeCount);
            mc.fontRenderer.drawStringWithShadow(s, textX - mc.fontRenderer.getStringWidth(s), textY + 1, 16733525); // 红色
        }


        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F); // 再次重置颜色
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();

        GlStateManager.enableDepth();
        GlStateManager.enableLighting();


        GlStateManager.popMatrix();
    }


    private static void setColor(int hex) {
        if (hex == -1) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            return;
        }
        float alpha = 1.0F;
        float red = (float)(hex >> 16 & 255) / 255.0F;
        float green = (float)(hex >> 8 & 255) / 255.0F;
        float blue = (float)(hex & 255) / 255.0F;
        GlStateManager.color(red, green, blue, alpha);
    }
}