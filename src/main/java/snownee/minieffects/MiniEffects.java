package snownee.minieffects;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

import static snownee.minieffects.handlers.BossBarOverlayRenderer.CUSTOM_BOSSBAR_TEXTURE_FOLDER;

@Mod(
        modid = MiniEffectsInfo.MOD_ID,
        clientSideOnly = true
)
public class MiniEffects {
    public static final String ID = MiniEffectsInfo.MOD_ID;
    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static final String CUSTOM_POTION_TEXTURE_FOLDER = "textures/potions";

    // 新增：用于存储 Mod 配置文件夹的引用
    public static File modConfigDirectory;

    public MiniEffects() {
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {


        // 获取 Mod 的配置目录并存储起来
        modConfigDirectory = new File(event.getModConfigurationDirectory(), ID);
        if (!modConfigDirectory.exists()) {
            modConfigDirectory.mkdirs();
        }

        // 创建自定义药水纹理文件夹
        File customTexturesDir = new File(modConfigDirectory, CUSTOM_POTION_TEXTURE_FOLDER);
        if (!customTexturesDir.exists()) {
            customTexturesDir.mkdirs();
          //  LOGGER.info("Created custom potion texture folder: " + customTexturesDir.getAbsolutePath());
        }

        File customBossBarTexturesDir = new File(modConfigDirectory, CUSTOM_BOSSBAR_TEXTURE_FOLDER);
        if (!customBossBarTexturesDir.exists()) {
            customBossBarTexturesDir.mkdirs();
           // LOGGER.info("Created custom boss bar texture folder: " + customBossBarTexturesDir.getAbsolutePath());
        }


    }
}