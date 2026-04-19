package com.eclipseware.imnotcheatingyouare.client.module.impl;

import com.eclipseware.imnotcheatingyouare.client.ImnotcheatingyouareClient;
import com.eclipseware.imnotcheatingyouare.client.clickgui.Clickgui;
import com.eclipseware.imnotcheatingyouare.client.module.Category;
import com.eclipseware.imnotcheatingyouare.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class Menu extends Module {
    public Menu() {
        super("Menu", Category.Client, "Opens the ClickGUI.");
        this.setKeyBind(GLFW.GLFW_KEY_RIGHT_SHIFT); // Default bind
    }

    @Override
    public void onEnable() {
        if (mc.player == null) {
            setToggled(false);
            return;
        }
        
        if (ImnotcheatingyouareClient.INSTANCE.clickGui == null) {
            ImnotcheatingyouareClient.INSTANCE.clickGui = new Clickgui();
        }
        
        if (!(mc.screen instanceof Clickgui)) {
            mc.setScreen(ImnotcheatingyouareClient.INSTANCE.clickGui);
        }
        
        setToggled(false);
    }
}
