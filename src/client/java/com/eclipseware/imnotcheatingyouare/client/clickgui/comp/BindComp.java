package com.eclipseware.imnotcheatingyouare.client.clickgui.comp;

import com.eclipseware.imnotcheatingyouare.client.clickgui.Clickgui;
import com.eclipseware.imnotcheatingyouare.client.module.Module;
import com.eclipseware.imnotcheatingyouare.client.utils.FontUtils;
import net.minecraft.client.gui.GuiGraphics;
import java.awt.Color;

public class BindComp extends Comp {
    private boolean isBinding;

    public BindComp(double x, double y, Clickgui parent, Module module) {
        this.x = x; this.y = y; this.parent = parent; this.module = module;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        String bindName = module.getKeyBind() == -1 ? "None" : org.lwjgl.glfw.GLFW.glfwGetKeyName(module.getKeyBind(), org.lwjgl.glfw.GLFW.glfwGetKeyScancode(module.getKeyBind()));
        if (bindName == null) bindName = "Unknown";
        bindName = bindName.toUpperCase();

        FontUtils.drawString(guiGraphics, isBinding ? "Listening..." : "Keybind: ", (int)(parent.posX + x), (int)(parent.posY + y), new Color(200, 200, 200).getRGB(), false);
        if (!isBinding) {
            FontUtils.drawString(guiGraphics, bindName, (int)(parent.posX + x) + FontUtils.width("Keybind: "), (int)(parent.posY + y), new Color(155, 60, 255).getRGB(), false);
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (isInside(mouseX, mouseY, parent.posX + x, parent.posY + y, parent.posX + x + 100, parent.posY + y + 10) && mouseButton == 0) {
            isBinding = !isBinding;
            Clickgui.playSound();
        }
    }

    // This handles mapping the actual key presses
    public void keyPressed(int key, int scanCode, int modifiers) {
        if (isBinding) {
            if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_DELETE || key == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE || key == org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE) {
                module.setKeyBind(-1);
            } else {
                module.setKeyBind(key);
            }
            isBinding = false;
        }
    }
}