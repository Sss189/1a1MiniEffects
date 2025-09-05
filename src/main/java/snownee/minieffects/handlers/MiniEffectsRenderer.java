package snownee.minieffects.handlers;

import lombok.val;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11; // 确保导入 GL11

/**
 * @author ZZZank (modified by assistance for robust OpenGL state handling)
 */
public class MiniEffectsRenderer {

    // 纹理资源路径
    private static final ResourceLocation MINIPOTION_BG = new ResourceLocation("minieffects", "textures/gui/minipotion_bg.png");
    private static final ResourceLocation MINIPOTION_POTION = new ResourceLocation("minieffects", "textures/gui/minipotion_potion.png");
    private static final ResourceLocation MINIPOTION_POTION_TINT = new ResourceLocation("minieffects", "textures/gui/minipotion_potion_tint.png");
    private static final ResourceLocation MINIPOTION_POTIONS = new ResourceLocation("minieffects", "textures/gui/minipotion_potions.png");
    private static final ResourceLocation MINIPOTION_POTIONS_TINT_POSITIVE = new ResourceLocation("minieffects", "textures/gui/minipotion_potions_tint_positive.png");
    private static final ResourceLocation MINIPOTION_POTIONS_TINT_NEGATIVE = new ResourceLocation("minieffects", "textures/gui/minipotion_potions_tint_negative.png");

    // 纹理尺寸常量
    private static final int TEXTURE_WIDTH = 29;
    private static final int TEXTURE_HEIGHT = 24;
    private static final int ICON_LOGICAL_SIZE = 29; // 假设逻辑上的图标占位是 29x29

    /**
     * 渲染迷你药水效果UI。
     *
     * @param screen 所在的 GuiContainer 屏幕
     * @param x UI 绘制的起始 X 坐标
     * @param y UI 绘制的起始 Y 坐标
     * @param positiveColor 增益效果的平均颜色 (ARGB 整数)
     * @param negativeColor 减益效果的平均颜色 (ARGB 整数)
     * @param positiveCount 增益效果的数量
     * @param negativeCount 减益效果的数量
     */
    public static void renderMini(GuiContainer screen, int x, int y, int positiveColor, int negativeColor, int positiveCount, int negativeCount) {
        val mc = screen.mc;
        val hasPositive = positiveCount > 0;
        val hasNegative = negativeCount > 0;

        // --- 绘制前：强制重置所有可能被污染的OpenGL状态 ---
        GlStateManager.pushMatrix(); // 保存当前矩阵

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F); // 强制颜色为完全不透明的白色
        GlStateManager.enableTexture2D(); // 确保纹理启用
        GlStateManager.enableAlpha(); // 启用 Alpha 测试
        GlStateManager.enableBlend(); // 启用混合
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); // 设置标准透明混合函数
        GlStateManager.disableLighting(); // 禁用光照（UI通常不需要光照）
        GlStateManager.disableDepth(); // 禁用深度测试（2D UI通常在最上层，不需要深度测试）
        GlStateManager.depthMask(true); // 但确保可以写入深度，防止被奇怪跳过


        // 计算 UI 纹理在逻辑空间内的垂直偏移，使其居中
        int drawYOffset = (ICON_LOGICAL_SIZE - TEXTURE_HEIGHT) / 2;

        // 绘制背景
        mc.getTextureManager().bindTexture(MINIPOTION_BG);
        Gui.drawModalRectWithCustomSizedTexture(x, y + drawYOffset, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        // 根据药水数量绘制不同的瓶子图标和染色层
        if (hasPositive && hasNegative) {
            // 绘制双瓶子的基础纹理
            mc.getTextureManager().bindTexture(MINIPOTION_POTIONS);
            Gui.drawModalRectWithCustomSizedTexture(x, y + drawYOffset, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);

            // 绘制增益瓶子的染色层
            setColor(positiveColor); // 设置染色颜色 (内部会确保alpha=1.0F)
            mc.getTextureManager().bindTexture(MINIPOTION_POTIONS_TINT_POSITIVE);
            Gui.drawModalRectWithCustomSizedTexture(x, y + drawYOffset, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F); // 绘制完增益染色后重置颜色

            // 绘制减益瓶子的染色层
            setColor(negativeColor); // 设置染色颜色 (内部会确保alpha=1.0F)
            mc.getTextureManager().bindTexture(MINIPOTION_POTIONS_TINT_NEGATIVE);
            Gui.drawModalRectWithCustomSizedTexture(x, y + drawYOffset, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F); // 绘制完减益染色后重置颜色

        } else if (hasPositive || hasNegative) {
            // 绘制单瓶子的基础纹理
            mc.getTextureManager().bindTexture(MINIPOTION_POTION);
            Gui.drawModalRectWithCustomSizedTexture(x, y + drawYOffset, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);

            // 绘制单瓶子的染色层
            int activeColor = hasPositive ? positiveColor : negativeColor;
            setColor(activeColor); // 设置染色颜色 (内部会确保alpha=1.0F)
            mc.getTextureManager().bindTexture(MINIPOTION_POTION_TINT);
            Gui.drawModalRectWithCustomSizedTexture(x, y + drawYOffset, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F); // 绘制完单瓶子染色后重置颜色
        }

        // 确保在绘制文本之前，颜色状态再次被重置
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        // 计算文本绘制位置
        int textX = x + 27;
        int textY = y + 14;

        // 绘制增益效果数量
        if (hasPositive) {
            val s = Integer.toString(positiveCount);
            mc.fontRenderer.drawStringWithShadow(s, textX - mc.fontRenderer.getStringWidth(s), textY + 1, 16777215); // 纯白色
            textY -= 10; // 如果有增益和减益，减益数量会上移
        }

        // 绘制减益效果数量
        if (hasNegative) {
            if (!hasPositive) {
                textY = y + 14; // 如果只有减益，则在常规位置绘制
            }
            val s = Integer.toString(negativeCount);
            mc.fontRenderer.drawStringWithShadow(s, textX - mc.fontRenderer.getStringWidth(s), textY + 1, 16733525); // 红色
        }

        // --- 绘制后：恢复到游戏可能需要的默认状态（或至少不干扰的状态） ---
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F); // 再次重置颜色
        GlStateManager.disableAlpha(); // 禁用 Alpha 测试
        GlStateManager.disableBlend(); // 禁用混合

        GlStateManager.enableDepth(); // 恢复深度测试
        GlStateManager.enableLighting(); // 恢复光照
        // 通常不需要再次调用 glUseProgram(0)，因为下一个渲染器会绑定它自己的着色器

        GlStateManager.popMatrix(); // 恢复之前保存的矩阵
    }

    /**
     * 将十六进制颜色值转换为 RGB 和 Alpha 并设置给 GlStateManager。
     * Alpha 值强制为 1.0F (不透明)。
     * @param hex 十六进制颜色值 (例如 0xFF00FF)
     */
    private static void setColor(int hex) {
        if (hex == -1) { // 如果传入 -1，则默认为纯白色
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            return;
        }
        float alpha = 1.0F; // 确保阿尔法值是 1.0 (不透明)
        float red = (float)(hex >> 16 & 255) / 255.0F;
        float green = (float)(hex >> 8 & 255) / 255.0F;
        float blue = (float)(hex & 255) / 255.0F;
        GlStateManager.color(red, green, blue, alpha);
    }
}