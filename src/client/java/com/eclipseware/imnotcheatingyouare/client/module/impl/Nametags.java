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
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3d;
import java.awt.Color;

public class Nametags extends Module {

    public Nametags() {
        super("Nametags", Category.Render);
        HudRenderCallback.EVENT.register((guiGraphics, tickCounter) -> onRenderHUD(guiGraphics, tickCounter));
    }

    private void onRenderHUD(GuiGraphics guiGraphics, Object tickCounterObj) {
if (!isToggled() || mc.player == null || mc.level == null) return;

    float deltaTicks = getDelta(tickCounterObj);

    Setting playersSetting = ImnotcheatingyouareClient.INSTANCE.settingsManager.getSettingByName(this, "Players");
    boolean showPlayers = playersSetting == null || playersSetting.getValBoolean();
    
    Setting mobsSetting = ImnotcheatingyouareClient.INSTANCE.settingsManager.getSettingByName(this, "Show Mobs");
    boolean showMobs = mobsSetting != null && mobsSetting.getValBoolean();

    for (Entity entity : mc.level.entitiesForRendering()) {
        if (entity == mc.player) continue;

        boolean isPlayer = entity instanceof Player;
        boolean isMob = entity instanceof net.minecraft.world.entity.Mob;

        if ((isPlayer && showPlayers) || (isMob && showMobs)) {
            
            double x = entity.xOld + (entity.getX() - entity.xOld) * deltaTicks;
                double y = entity.yOld + (entity.getY() - entity.yOld) * deltaTicks;
                double z = entity.zOld + (entity.getZ() - entity.zOld) * deltaTicks;
float height = entity.getBbHeight();

            Vector3d tagProj = RenderUtils.project2D(x, y + height + 0.4, z, deltaTicks);
            if (tagProj != null) {
                    double dist = Math.round(mc.player.distanceTo(entity) * 10.0) / 10.0;
                    String hpStr = (entity instanceof LivingEntity l) ? " [" + (int)Math.ceil(l.getHealth()) + " HP]" : "";
                    String tagText = entity.getName().getString() + hpStr + " | " + dist + "m";

                    int textColor = new Color(155, 60, 255).getRGB(); // Theme Accent
                    int textWidth = FontUtils.width(tagText);
                    
                    guiGraphics.fill((int)tagProj.x - (textWidth/2) - 2, (int)tagProj.y - 12, (int)tagProj.x + (textWidth/2) + 2, (int)tagProj.y + 1, new Color(15, 15, 15, 180).getRGB());
                    FontUtils.drawCenteredString(guiGraphics, tagText, (int)tagProj.x, (int)tagProj.y - 10, textColor);

                    if (entity instanceof LivingEntity living) {
                        ItemStack mainHand = living.getMainHandItem();
                        if (!mainHand.isEmpty()) {
                            guiGraphics.renderItem(mainHand, (int)tagProj.x - 8, (int)tagProj.y - 32);
                        }
                    }
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