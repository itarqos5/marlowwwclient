package com.eclipseware.imnotcheatingyouare.client.module.impl;

import com.eclipseware.imnotcheatingyouare.client.ImnotcheatingyouareClient;
import com.eclipseware.imnotcheatingyouare.client.module.Category;
import com.eclipseware.imnotcheatingyouare.client.module.Module;
import com.eclipseware.imnotcheatingyouare.client.setting.Setting;
import com.eclipseware.imnotcheatingyouare.client.utils.RenderUtils;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3d;
import java.awt.Color;

public class Tracers extends Module {

    public Tracers() {
        super("Tracers", Category.Render);
        HudRenderCallback.EVENT.register((guiGraphics, tickCounter) -> onRenderHUD(guiGraphics, tickCounter));
    }

    private void onRenderHUD(GuiGraphics guiGraphics, Object tickCounterObj) {
        if (!isToggled() || mc.player == null || mc.level == null) return;
        
        float deltaTicks = getDelta(tickCounterObj);

        Setting crosshairSetting = ImnotcheatingyouareClient.INSTANCE.settingsManager.getSettingByName(this, "Crosshair Attach");
        boolean attachCrosshair = crosshairSetting != null && crosshairSetting.getValBoolean();
        
        Setting mobsSetting = ImnotcheatingyouareClient.INSTANCE.settingsManager.getSettingByName(this, "Show Mobs");
        boolean showMobs = mobsSetting != null && mobsSetting.getValBoolean();

        double startX = mc.getWindow().getGuiScaledWidth() / 2.0;
        double startY = mc.getWindow().getGuiScaledHeight() / 2.0;
        
        if (!attachCrosshair) {
            double px = mc.player.xOld + (mc.player.getX() - mc.player.xOld) * deltaTicks;
            double py = mc.player.yOld + (mc.player.getY() - mc.player.yOld) * deltaTicks;
            double pz = mc.player.zOld + (mc.player.getZ() - mc.player.zOld) * deltaTicks;
            
            Vector3d pPos = RenderUtils.project2D(px, py, pz, deltaTicks);
if (pPos != null) { startX = pPos.x; startY = pPos.y; }
else return;
}

    for (Entity entity : mc.level.entitiesForRendering()) {
        if (entity == mc.player) continue;

        if (entity instanceof Player || (entity instanceof Mob && showMobs)) {
            double x = entity.xOld + (entity.getX() - entity.xOld) * deltaTicks;
            double y = entity.yOld + (entity.getY() - entity.yOld) * deltaTicks + (entity.getBbHeight() / 2.0);
            double z = entity.zOld + (entity.getZ() - entity.zOld) * deltaTicks;

            Vector3d endProj = RenderUtils.project2D(x, y, z, deltaTicks);
            if (endProj != null) {
                    RenderUtils.drawLine2D(guiGraphics, startX, startY, endProj.x, endProj.y, new Color(155, 60, 255, 200));
                }
            }
        }
    }

    private float getDelta(Object tickCounter) {
        try {
            for (java.lang.reflect.Method m : tickCounter.getClass().getMethods()) {
                if (m.getReturnType() == float.class && m.getParameterCount() == 0) return (float) m.invoke(tickCounter);
            }
        } catch (Exception e) {}
        return 1.0f;
    }
}