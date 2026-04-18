package com.eclipseware.imnotcheatingyouare.client.clickgui.components;

import com.eclipseware.imnotcheatingyouare.client.clickgui.Clickgui;
import com.eclipseware.imnotcheatingyouare.client.module.Module;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;

public class BindButton extends Button {
    private final Module module;
    public boolean isListening;

    public BindButton(Module module) {
        super("Bind");
        this.module = module;
        this.width = 15;
    }

    private String getKeyName(int key) {
        if (key == -1) return "NONE";
        String str = GLFW.glfwGetKeyName(key, 0);
        if (str == null) {
            str = "UNKNOWN";
        }
        return str.toUpperCase();
    }

    @Override
    public void drawScreen(GuiGraphics context, int mouseX, int mouseY, float partialTicks) {
        int dark = 0x22000000;
        int hoverDark = 0x44222222;
        int fill = this.isHovering(mouseX, mouseY) ? hoverDark : dark;

        context.fill((int)this.x, (int)this.y, (int)(this.x + this.width + 7.4f), (int)(this.y + this.height), fill);
        
        if (this.isListening) {
            drawString("Listening...", this.x + 2.3f, this.y - 1.7f + 6, -1);
        } else {
            drawString("Bind " + ChatFormatting.GRAY + getKeyName(this.module.getKeyBind()), this.x + 2.3f, this.y - 1.7f + 6, -1);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.isListening) {
            if (mouseButton != 0 && mouseButton != 1) { // Mouse buttons as binds are skipped for simplicity in classic modes right now
                this.isListening = false;
            }
        } else if (this.isHovering(mouseX, mouseY)) {
            Clickgui.playSound();
        }
    }

    @Override
    public void onKeyPressed(int key) {
        if (this.isListening) {
            int targetKey = key;
            if (key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_BACKSPACE || key == GLFW.GLFW_KEY_ESCAPE) {
                targetKey = -1;
            }
            this.module.setKeyBind(targetKey);
            this.isListening = false;
        }
    }

    @Override
    public void toggle() {
        this.isListening = !this.isListening;
    }

    @Override
    public boolean getState() {
        return !this.isListening;
    }
}
