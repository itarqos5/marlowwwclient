package com.eclipseware.imnotcheatingyouare.client.clickgui.comp;

import com.eclipseware.imnotcheatingyouare.client.clickgui.Clickgui;
import com.eclipseware.imnotcheatingyouare.client.module.Module;
import com.eclipseware.imnotcheatingyouare.client.setting.Setting;
import com.eclipseware.imnotcheatingyouare.client.utils.FontUtils;
import net.minecraft.client.gui.GuiGraphics;
import java.awt.Color;

public class CheckBox extends Comp {
    private float slideAnim = 0f;

    public CheckBox(double x, double y, Clickgui parent, Module module, Setting setting) {
        this.x = x; this.y = y; this.parent = parent; this.module = module; this.setting = setting;
        this.slideAnim = setting.getValBoolean() ? 1f : 0f;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        float target = setting.getValBoolean() ? 1f : 0f;
        slideAnim += (target - slideAnim) * 0.2f;

        int bgOff = new Color(40, 40, 45).getRGB();
        int bgOn = new Color(155, 60, 255).getRGB(); 
        int currentBg = interpolateColor(new Color(bgOff), new Color(bgOn), slideAnim).getRGB();

        guiGraphics.fill((int)(parent.posX + x), (int)(parent.posY + y), (int)(parent.posX + x + 24), (int)(parent.posY + y + 12), currentBg);
        int knobX = (int)(parent.posX + x + 2 + (slideAnim * 12));
        guiGraphics.fill(knobX, (int)(parent.posY + y + 2), knobX + 8, (int)(parent.posY + y + 10), -1);

        FontUtils.drawString(guiGraphics, setting.getName(), (int)(parent.posX + x + 30), (int)(parent.posY + y + 2), new Color(200, 200, 200).getRGB(), false);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (isInside(mouseX, mouseY, parent.posX + x, parent.posY + y, parent.posX + x + 24 + FontUtils.width(setting.getName()), parent.posY + y + 12) && mouseButton == 0) {
            setting.setValBoolean(!setting.getValBoolean());
            Clickgui.playSound();
        }
    }

    private Color interpolateColor(Color color1, Color color2, float fraction) {
        int red = (int) (color1.getRed() + (color2.getRed() - color1.getRed()) * fraction);
        int green = (int) (color1.getGreen() + (color2.getGreen() - color1.getGreen()) * fraction);
        int blue = (int) (color1.getBlue() + (color2.getBlue() - color1.getBlue()) * fraction);
        int alpha = (int) (color1.getAlpha() + (color2.getAlpha() - color1.getAlpha()) * fraction);
        return new Color(red, green, blue, alpha);
    }
}