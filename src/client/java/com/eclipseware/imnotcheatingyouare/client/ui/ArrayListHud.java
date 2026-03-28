package com.eclipseware.imnotcheatingyouare.client.ui;

import com.eclipseware.imnotcheatingyouare.client.ImnotcheatingyouareClient;
import com.eclipseware.imnotcheatingyouare.client.module.Module;
import com.eclipseware.imnotcheatingyouare.client.utils.FontUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArrayListHud {
    public static final ArrayListHud INSTANCE = new ArrayListHud();
    
    public double x = 5;
    public double y = 5;
    public boolean dragging;
    public double dragX, dragY;

    private final Map<Module, Float> animMap = new HashMap<>();

    public void render(GuiGraphics guiGraphics, float partialTick) {
        Module arrayListMod = ImnotcheatingyouareClient.INSTANCE.moduleManager.getModule("ArrayList");
        if (arrayListMod == null || !arrayListMod.isToggled()) return;

        int accent = new Color(155, 60, 255).getRGB(); 
        
        List<Module> activeMods = new ArrayList<>();
        
        for (Module m : ImnotcheatingyouareClient.INSTANCE.moduleManager.modules) {
            float currentAnim = animMap.getOrDefault(m, 0f);
            float target = m.isToggled() ? 1f : 0f;
            currentAnim += (target - currentAnim) * 0.15f;
            animMap.put(m, currentAnim);
            
            if (currentAnim > 0.01f) {
                activeMods.add(m);
            }
        }

        // USING FONT UTILS TO SORT WIDTH
        activeMods.sort(Comparator.comparingInt(m -> -FontUtils.width(m.getName())));

        double currentY = y;

        for (Module m : activeMods) {
            float anim = animMap.get(m);
            String name = m.getName();
            
            // USING FONT UTILS FOR SIZING
            int textWidth = FontUtils.width(name);
            double xOffset = (1.0f - anim) * -30f; 
            
            int alpha = (int)(255 * anim);
            int currentBg = new Color(14, 14, 16, (int)(200 * anim)).getRGB();
            int currentAccent = new Color(155, 60, 255, alpha).getRGB();
            int textColor = new Color(255, 255, 255, alpha).getRGB();

            int drawX = (int)(x + xOffset);
            int drawY = (int)currentY;

            guiGraphics.fill(drawX, drawY, drawX + textWidth + 8, drawY + 14, currentBg);
            guiGraphics.fill(drawX, drawY, drawX + 2, drawY + 14, currentAccent);
            
            // USING FONT UTILS TO RENDER
            FontUtils.drawString(guiGraphics, name, drawX + 5, drawY + 3, textColor, true);

            currentY += 14 * anim; 
        }
    }
}