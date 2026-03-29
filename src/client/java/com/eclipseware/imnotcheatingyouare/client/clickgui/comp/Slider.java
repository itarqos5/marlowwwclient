package com.eclipseware.imnotcheatingyouare.client.clickgui.comp;

import com.eclipseware.imnotcheatingyouare.client.clickgui.Clickgui;
import com.eclipseware.imnotcheatingyouare.client.module.Module;
import com.eclipseware.imnotcheatingyouare.client.setting.Setting;
import com.eclipseware.imnotcheatingyouare.client.utils.FontUtils;
import net.minecraft.client.gui.GuiGraphics;
import java.awt.Color;

public class Slider extends Comp {
    private boolean dragging = false;
    private int renderWidth = 140;

    public Slider(double x, double y, Clickgui parent, Module module, Setting setting) {
        this.x = x; this.y = y; this.parent = parent; this.module = module; this.setting = setting;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        double diff = Math.min(renderWidth, Math.max(0, mouseX - (parent.posX + x)));
        if (dragging) {
            if (diff == 0) setting.setValDouble(setting.getMin());
            else {
                double newValue = roundToPlace(((diff / renderWidth) * (setting.getMax() - setting.getMin()) + setting.getMin()), 2);
                setting.setValDouble(newValue);
            }
        }
        double renderWidth2 = (renderWidth) * (setting.getValDouble() - setting.getMin()) / (setting.getMax() - setting.getMin());

        FontUtils.drawString(guiGraphics, setting.getName() + ": " + setting.getValDouble(), (int)(parent.posX + x), (int)(parent.posY + y), new Color(200, 200, 200).getRGB(), false);
        
        guiGraphics.fill((int)(parent.posX + x), (int)(parent.posY + y + 12), (int)(parent.posX + x + renderWidth), (int)(parent.posY + y + 14), new Color(40, 40, 45).getRGB());
        guiGraphics.fill((int)(parent.posX + x), (int)(parent.posY + y + 12), (int)(parent.posX + x + renderWidth2), (int)(parent.posY + y + 14), new Color(155, 60, 255).getRGB());
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (isInside(mouseX, mouseY, parent.posX + x, parent.posY + y + 10, parent.posX + x + renderWidth, parent.posY + y + 16) && mouseButton == 0) {
            dragging = true;
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int mouseButton) {
        dragging = false;
    }

    private double roundToPlace(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}