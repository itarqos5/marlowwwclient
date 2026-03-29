package com.eclipseware.imnotcheatingyouare.client.clickgui;

import com.eclipseware.imnotcheatingyouare.client.ImnotcheatingyouareClient;
import com.eclipseware.imnotcheatingyouare.client.clickgui.comp.*;
import com.eclipseware.imnotcheatingyouare.client.module.Category;
import com.eclipseware.imnotcheatingyouare.client.module.Module;
import com.eclipseware.imnotcheatingyouare.client.setting.Setting;
import com.eclipseware.imnotcheatingyouare.client.utils.FontUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.eclipseware.imnotcheatingyouare.client.ui.ArrayListHud;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

public class Clickgui extends Screen {
    public double posX, posY, windowWidth, windowHeight, dragX, dragY;
    public boolean dragging;
    public Category selectedCategory;
    public Module selectedModule;

    public float openAnim = 0f;
    public boolean closing = false;
    public float settingsSlideAnim = 0f;
    private final Map<Module, Float> moduleToggleAnims = new HashMap<>();

    public double scrollOffset = 0;       
    public double moduleScrollOffset = 0; 

    public ArrayList<Comp> comps = new ArrayList<>();

    public Clickgui() {
        super(Component.literal("ClickGUI"));
        dragging = false;
        selectedCategory = Category.values()[0]; 
        posX = -1; 
        posY = -1;
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Intentionally left blank to disable Minecraft's static black gradient overlay
    }

    public static void playSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    protected void init() {
        super.init();
        windowWidth = 520;
        windowHeight = 350;
        openAnim = 0f; 
        closing = false;
        
        if (posX == -1 && posY == -1) {
            posX = (this.width - windowWidth) / 2.0;
            posY = (this.height - windowHeight) / 2.0;
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        com.eclipseware.imnotcheatingyouare.client.setting.ConfigManager.save();
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        if (event.key() == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
            closing = true;
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        if (closing) {
            openAnim += (0f - openAnim) * 0.25f; 
            if (openAnim < 0.05f) {
                super.onClose(); 
                return;
            }
        } else {
            openAnim += (1.0f - openAnim) * 0.15f; 
        }

        float targetSlide = (selectedModule != null) ? 1f : 0f;
        settingsSlideAnim += (targetSlide - settingsSlideAnim) * 0.2f;

        if (dragging) {
            posX = mouseX - dragX;
            posY = mouseY - dragY;
        }

        // Animated Fullscreen Backdrop (Renders BEFORE the matrix scales the UI)
        int screenDim = new Color(5, 5, 5, (int)(140 * openAnim)).getRGB();
        guiGraphics.fill(0, 0, this.width, this.height, screenDim);

        guiGraphics.pose().pushMatrix();
        float centerX = (float) (posX + windowWidth / 2.0);
        float centerY = (float) (posY + windowHeight / 2.0);
        
        // STRICTLY 2D TRANSLATIONS!
        guiGraphics.pose().translate(centerX, centerY);
        guiGraphics.pose().scale(openAnim, openAnim);
        guiGraphics.pose().translate(-centerX, -centerY);

        int bgDark = new Color(14, 14, 16, 240).getRGB();
        int sidebarColor = new Color(20, 20, 22, 255).getRGB();
        int accent = new Color(155, 60, 255).getRGB(); 

        guiGraphics.fill((int)posX, (int)posY, (int)(posX + windowWidth), (int)(posY + windowHeight), bgDark);
        guiGraphics.fill((int)posX, (int)posY, (int)(posX + 120), (int)(posY + windowHeight), sidebarColor);

        // USING FONT UTILS FOR MARLOWWW CLIENT
        FontUtils.drawString(guiGraphics, "MARLOWWW", (int)posX + 15, (int)posY + 15, -1, true);
        FontUtils.drawString(guiGraphics, "CLIENT", (int)posX + 15 + FontUtils.width("MARLOWWW "), (int)posY + 15, accent, true);

        int catY = (int)posY + 50;
        for (Category cat : Category.values()) {
            boolean sel = cat == selectedCategory;
            if (sel) {
                guiGraphics.fill((int)posX + 10, catY, (int)posX + 110, catY + 24, new Color(255, 255, 255, 15).getRGB());
                guiGraphics.fill((int)posX + 10, catY + 4, (int)posX + 12, catY + 20, accent);
            }
            FontUtils.drawString(guiGraphics, cat.name(), (int)posX + 25, catY + 8, sel ? -1 : new Color(140, 140, 140).getRGB(), false);
            catY += 28;
        }

        guiGraphics.enableScissor((int)posX + 120, (int)posY, (int)(posX + windowWidth), (int)(posY + windowHeight));

        if (settingsSlideAnim < 0.99f) {
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate(-(settingsSlideAnim * 400), 0f); 

            FontUtils.drawString(guiGraphics, selectedCategory.name().toUpperCase(), (int)posX + 140, (int)posY + 18, accent, false);
            FontUtils.drawString(guiGraphics, "Select a module to configure", (int)posX + 140, (int)posY + 30, new Color(140, 140, 140).getRGB(), false);
            guiGraphics.fill((int)posX + 140, (int)posY + 45, (int)(posX + windowWidth - 20), (int)posY + 46, new Color(255, 255, 255, 20).getRGB());

            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate(0f, (float) moduleScrollOffset);

            int modY = (int)posY + 55;
            for (Module m : ImnotcheatingyouareClient.INSTANCE.moduleManager.getModules(selectedCategory)) {
                
                float currentModAnim = moduleToggleAnims.getOrDefault(m, m.isToggled() ? 1f : 0f);
                currentModAnim += ((m.isToggled() ? 1f : 0f) - currentModAnim) * 0.15f;
                moduleToggleAnims.put(m, currentModAnim);

                int cardColor = interpolateColor(new Color(30, 30, 32), new Color(45, 25, 65), currentModAnim).getRGB();
                int textColor = interpolateColor(new Color(140, 140, 140), new Color(255, 255, 255), currentModAnim).getRGB();

                guiGraphics.fill((int)posX + 140, modY, (int)(posX + windowWidth - 20), modY + 30, cardColor);
guiGraphics.fill((int)posX + 140, modY, (int)posX + 140 + (int)(3 * currentModAnim), modY + 30, accent);

            FontUtils.drawString(guiGraphics, m.getName(), (int)posX + 155, modY + 11, textColor, false);
            FontUtils.drawString(guiGraphics, "Right-Click", (int)(posX + windowWidth - 80), modY + 11, new Color(100, 100, 100).getRGB(), false);

            modY += 36;
            }
            guiGraphics.pose().popMatrix();
            guiGraphics.pose().popMatrix();
        }

        if (settingsSlideAnim > 0.01f) {
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate((1f - settingsSlideAnim) * 400, 0f);

            boolean backHover = isInside(mouseX, mouseY, posX + 140, posY + 15, posX + 190, posY + 25);
FontUtils.drawString(guiGraphics, "< Back", (int)posX + 140, (int)posY + 15, backHover ? -1 : new Color(140, 140, 140).getRGB(), false);

        if (selectedModule != null) {
                FontUtils.drawString(guiGraphics, selectedModule.getName() + " Settings", (int)posX + 140, (int)posY + 32, accent, false);
            }
            
            guiGraphics.fill((int)posX + 140, (int)posY + 45, (int)(posX + windowWidth - 20), (int)posY + 46, new Color(255, 255, 255, 20).getRGB());

            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate(0f, (float) scrollOffset);

            for (Comp comp : comps) {
                comp.render(guiGraphics, mouseX, (int)(mouseY - scrollOffset));
            }

            guiGraphics.pose().popMatrix();
            guiGraphics.pose().popMatrix();
        }

        guiGraphics.disableScissor();

    // --- DRAW HOVER DESCRIPTIONS ---
    if (selectedModule == null) {
        int modY = (int)posY + 55 + (int)moduleScrollOffset;
        for (Module m : ImnotcheatingyouareClient.INSTANCE.moduleManager.getModules(selectedCategory)) {
            if (isInside(mouseX, mouseY, posX + 140, modY, posX + windowWidth - 20, modY + 30)) {
                String desc = m.getDescription();
                if (desc != null && !desc.isEmpty()) {
int tw = FontUtils.width(desc);
guiGraphics.pose().pushMatrix();
// Removed the Z-translation! It renders last naturally, so it will always be on top.
guiGraphics.fill(mouseX + 10, mouseY, mouseX + 14 + tw, mouseY + 14, new Color(15, 15, 15, 240).getRGB());
guiGraphics.fill(mouseX + 10, mouseY, mouseX + 11, mouseY + 14, accent); // Accent line
                    FontUtils.drawString(guiGraphics, desc, mouseX + 14, mouseY + 3, -1, false);
                    guiGraphics.pose().popMatrix();
                }
                break;
            }
            modY += 36;
        }
    }

    guiGraphics.pose().popMatrix(); 
}

private Color interpolateColor(Color color1, Color color2, float fraction) {
        int red = (int) (color1.getRed() + (color2.getRed() - color1.getRed()) * fraction);
        int green = (int) (color1.getGreen() + (color2.getGreen() - color1.getGreen()) * fraction);
        int blue = (int) (color1.getBlue() + (color2.getBlue() - color1.getBlue()) * fraction);
        int alpha = (int) (color1.getAlpha() + (color2.getAlpha() - color1.getAlpha()) * fraction);
        return new Color(red, green, blue, alpha);
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        if (closing) return false;

        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();

        if (isInside(mouseX, mouseY, posX, posY, posX + windowWidth, posY + 20) && button == 0) {
            dragging = true;
            dragX = mouseX - posX;
            dragY = mouseY - posY;
        }

        int catY = (int)posY + 50;
        for (Category cat : Category.values()) {
            if (isInside(mouseX, mouseY, posX + 10, catY, posX + 110, catY + 24) && button == 0) {
                selectedCategory = cat;
                selectedModule = null; 
                moduleScrollOffset = 0;
                playSound();
                return true;
            }
            catY += 28;
        }

        if (selectedModule == null) {
            if (mouseY > posY + 46) {
                int modY = (int)posY + 55 + (int)moduleScrollOffset;
                for (Module m : ImnotcheatingyouareClient.INSTANCE.moduleManager.getModules(selectedCategory)) {
                    if (isInside(mouseX, mouseY, posX + 140, modY, posX + windowWidth - 20, modY + 30)) {
                        playSound();
                        if (button == 0) m.toggle(); 
                        else if (button == 1) {      
                            selectedModule = m; 
                            loadComponents(m);
                        }
                        return true;
                    }
                    modY += 36;
                }
            }
        } else {
            if (isInside(mouseX, mouseY, posX + 140, posY + 15, posX + 190, posY + 25) && button == 0) {
                selectedModule = null; 
                playSound();
                return true;
            }

            if (isInside(mouseX, mouseY, posX + 120, posY + 46, posX + windowWidth, posY + windowHeight)) {
                ArrayList<Comp> compsCopy = new ArrayList<>(comps);
                for (Comp comp : compsCopy) {
                    comp.mouseClicked(mouseX, mouseY - scrollOffset, button);
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) {
        double mouseX = event.x();
        double mouseY = event.y();
        int state = event.button();

        dragging = false;

        if (selectedModule != null) {
            ArrayList<Comp> compsCopy = new ArrayList<>(comps);
            for (Comp comp : compsCopy) {
                comp.mouseReleased(mouseX, mouseY - scrollOffset, state);
            }
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return handleScroll(scrollY);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        return handleScroll(scrollDelta);
    }

    private boolean handleScroll(double scrollDelta) {
        if (selectedModule == null) {
            moduleScrollOffset += scrollDelta * 20;
            if (moduleScrollOffset > 0) moduleScrollOffset = 0; 
            
            int totalMods = ImnotcheatingyouareClient.INSTANCE.moduleManager.getModules(selectedCategory).size();
            double maxScroll = -((totalMods * 36) + 20 - (windowHeight - 55));
            if (maxScroll > 0) maxScroll = 0;
            if (moduleScrollOffset < maxScroll) moduleScrollOffset = maxScroll;
        } else {
            scrollOffset += scrollDelta * 20;
            if (scrollOffset > 0) scrollOffset = 0; 
            
            double maxH = 0;
            for (Comp c : comps) {
                if (c.y > maxH) maxH = c.y;
            }
            double maxScroll = -((maxH + 35) - (windowHeight - 46));
            if (maxScroll > 0) maxScroll = 0;
            if (scrollOffset < maxScroll) scrollOffset = maxScroll;
        }
        return true;
    }

    public void loadComponents(Module m) {
        comps.clear();
        scrollOffset = 0; 
        int sY = 60; 
        
        comps.add(new com.eclipseware.imnotcheatingyouare.client.clickgui.comp.BindComp(140, sY, this, m));
        sY += 25;

        if (ImnotcheatingyouareClient.INSTANCE.settingsManager.getSettingsByMod(m) != null) {
            for (Setting setting : ImnotcheatingyouareClient.INSTANCE.settingsManager.getSettingsByMod(m)) {
                if (setting.isCombo()) { comps.add(new Combo(140, sY, this, m, setting)); sY += 22; }
                if (setting.isCheck()) { comps.add(new CheckBox(140, sY, this, m, setting)); sY += 22; }
                if (setting.isSlider()) { comps.add(new Slider(140, sY, this, m, setting)); sY += 28; }
            }
        }
    }

    public boolean isInside(double mouseX, double mouseY, double x, double y, double x2, double y2) {
        return (mouseX >= x && mouseX <= x2) && (mouseY >= y && mouseY <= y2);
    }
}