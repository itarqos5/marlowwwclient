package com.eclipseware.imnotcheatingyouare.client.module.impl;

import com.eclipseware.imnotcheatingyouare.client.ImnotcheatingyouareClient;
import com.eclipseware.imnotcheatingyouare.client.module.Category;
import com.eclipseware.imnotcheatingyouare.client.module.Module;
import com.eclipseware.imnotcheatingyouare.client.setting.Setting;
import com.eclipseware.imnotcheatingyouare.client.utils.FontUtils;
import com.eclipseware.imnotcheatingyouare.client.utils.RenderUtils;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3d;
import java.awt.Color;

public class ESP extends Module {

    public ESP() {
        super("ESP", Category.Render);
        HudRenderCallback.EVENT.register((guiGraphics, tickCounter) -> onRenderHUD(guiGraphics, tickCounter));
    }

    private void onRenderHUD(GuiGraphics guiGraphics, Object tickCounterObj) {
        if (!isToggled() || mc.player == null || mc.level == null) return;
        
        float deltaTicks = getDelta(tickCounterObj);

        Setting modeSetting = ImnotcheatingyouareClient.INSTANCE.settingsManager.getSettingByName(this, "Mode");
        Setting mobsSetting = ImnotcheatingyouareClient.INSTANCE.settingsManager.getSettingByName(this, "Show Mobs");
        
        String mode = modeSetting != null ? modeSetting.getValString() : "3D";
        boolean showMobs = mobsSetting != null && mobsSetting.getValBoolean();

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity == mc.player) continue;

            if (entity instanceof Player || (entity instanceof Mob && showMobs)) {
                
                double x = entity.xOld + (entity.getX() - entity.xOld) * deltaTicks;
                double y = entity.yOld + (entity.getY() - entity.yOld) * deltaTicks;
                double z = entity.zOld + (entity.getZ() - entity.zOld) * deltaTicks;
                
                float width = entity.getBbWidth() / 2.0f;
                float height = entity.getBbHeight();

                double[][] corners = {
                    {x - width, y, z - width}, {x + width, y, z - width}, {x - width, y + height, z - width}, {x + width, y + height, z - width},
                    {x - width, y, z + width}, {x + width, y, z + width}, {x - width, y + height, z + width}, {x + width, y + height, z + width}
                };

                double screenMinX = Double.MAX_VALUE; double screenMinY = Double.MAX_VALUE;
                double screenMaxX = Double.MIN_VALUE; double screenMaxY = Double.MIN_VALUE;
                boolean isBehind = true;

                Vector3d[] projs = new Vector3d[8];
for (int i = 0; i < 8; i++) {
projs[i] = RenderUtils.project2D(corners[i][0], corners[i][1], corners[i][2], deltaTicks);
if (projs[i] != null) {
                        isBehind = false;
                        screenMinX = Math.min(screenMinX, projs[i].x); screenMinY = Math.min(screenMinY, projs[i].y);
                        screenMaxX = Math.max(screenMaxX, projs[i].x); screenMaxY = Math.max(screenMaxY, projs[i].y);
                    }
                }

                if (isBehind) continue;
                Color themeColor = (entity instanceof Player) ? new Color(155, 60, 255) : new Color(255, 100, 50);

                if (mode.equals("2D") || mode.equals("Hybrid")) {
                    guiGraphics.fill((int)screenMinX, (int)screenMinY, (int)screenMaxX, (int)screenMinY + 1, themeColor.getRGB()); 
                    guiGraphics.fill((int)screenMinX, (int)screenMaxY, (int)screenMaxX, (int)screenMaxY + 1, themeColor.getRGB()); 
                    guiGraphics.fill((int)screenMinX, (int)screenMinY, (int)screenMinX + 1, (int)screenMaxY, themeColor.getRGB()); 
                    guiGraphics.fill((int)screenMaxX, (int)screenMinY, (int)screenMaxX + 1, (int)screenMaxY + 1, themeColor.getRGB()); 

                    if (entity instanceof LivingEntity living) {
                        float hp = living.getHealth();
                        float maxHp = living.getMaxHealth();
                        float pct = Math.min(1.0f, Math.max(0.0f, hp / maxHp));
                        int barHeight = (int) ((screenMaxY - screenMinY) * pct);
                        
                        Color hpColor = pct > 0.6f ? Color.GREEN : (pct > 0.3f ? Color.YELLOW : Color.RED);
                        guiGraphics.fill((int)screenMinX - 5, (int)screenMinY, (int)screenMinX - 3, (int)screenMaxY, new Color(40,40,40, 200).getRGB());
                        guiGraphics.fill((int)screenMinX - 5, (int)screenMaxY - barHeight, (int)screenMinX - 3, (int)screenMaxY, hpColor.getRGB());
                    }
                }
                
                if (mode.equals("3D") || mode.equals("Hybrid")) {
                    if (projs[0] != null && projs[1] != null) RenderUtils.drawLine2D(guiGraphics, projs[0].x, projs[0].y, projs[1].x, projs[1].y, themeColor);
                    if (projs[1] != null && projs[5] != null) RenderUtils.drawLine2D(guiGraphics, projs[1].x, projs[1].y, projs[5].x, projs[5].y, themeColor);
                    if (projs[5] != null && projs[4] != null) RenderUtils.drawLine2D(guiGraphics, projs[5].x, projs[5].y, projs[4].x, projs[4].y, themeColor);
                    if (projs[4] != null && projs[0] != null) RenderUtils.drawLine2D(guiGraphics, projs[4].x, projs[4].y, projs[0].x, projs[0].y, themeColor);
                    
                    if (projs[2] != null && projs[3] != null) RenderUtils.drawLine2D(guiGraphics, projs[2].x, projs[2].y, projs[3].x, projs[3].y, themeColor);
                    if (projs[3] != null && projs[7] != null) RenderUtils.drawLine2D(guiGraphics, projs[3].x, projs[3].y, projs[7].x, projs[7].y, themeColor);
                    if (projs[7] != null && projs[6] != null) RenderUtils.drawLine2D(guiGraphics, projs[7].x, projs[7].y, projs[6].x, projs[6].y, themeColor);
                    if (projs[6] != null && projs[2] != null) RenderUtils.drawLine2D(guiGraphics, projs[6].x, projs[6].y, projs[2].x, projs[2].y, themeColor);
                    
                    if (projs[0] != null && projs[2] != null) RenderUtils.drawLine2D(guiGraphics, projs[0].x, projs[0].y, projs[2].x, projs[2].y, themeColor);
                    if (projs[1] != null && projs[3] != null) RenderUtils.drawLine2D(guiGraphics, projs[1].x, projs[1].y, projs[3].x, projs[3].y, themeColor);
                    if (projs[4] != null && projs[6] != null) RenderUtils.drawLine2D(guiGraphics, projs[4].x, projs[4].y, projs[6].x, projs[6].y, themeColor);
                    if (projs[5] != null && projs[7] != null) RenderUtils.drawLine2D(guiGraphics, projs[5].x, projs[5].y, projs[7].x, projs[7].y, themeColor);
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