package com.eclipseware.imnotcheatingyouare.client.clickgui.comp;

import com.eclipseware.imnotcheatingyouare.client.clickgui.Clickgui;
import com.eclipseware.imnotcheatingyouare.client.module.Module;
import com.eclipseware.imnotcheatingyouare.client.setting.Setting;
import com.eclipseware.imnotcheatingyouare.client.utils.FontUtils;
import net.minecraft.client.gui.GuiGraphics;
import java.awt.Color;

public class Combo extends Comp {
    public Combo(double x, double y, Clickgui parent, Module module, Setting setting) {
        this.x = x; this.y = y; this.parent = parent; this.module = module; this.setting = setting;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        FontUtils.drawString(guiGraphics, setting.getName() + ": ", (int)(parent.posX + x), (int)(parent.posY + y), new Color(200, 200, 200).getRGB(), false);
        FontUtils.drawString(guiGraphics, setting.getValString(), (int)(parent.posX + x) + FontUtils.width(setting.getName() + ": "), (int)(parent.posY + y), new Color(155, 60, 255).getRGB(), false);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (isInside(mouseX, mouseY, parent.posX + x, parent.posY + y, parent.posX + x + FontUtils.width(setting.getName() + ": " + setting.getValString()), parent.posY + y + 10) && mouseButton == 0) {
            int index = setting.getOptions().indexOf(setting.getValString());
            if (index + 1 >= setting.getOptions().size()) {
                setting.setValString(setting.getOptions().get(0));
            } else {
                setting.setValString(setting.getOptions().get(index + 1));
            }
            Clickgui.playSound();
        }
    }
}