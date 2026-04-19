package com.eclipseware.imnotcheatingyouare.client.module.impl;

import com.eclipseware.imnotcheatingyouare.client.ImnotcheatingyouareClient;
import com.eclipseware.imnotcheatingyouare.client.module.Category;
import com.eclipseware.imnotcheatingyouare.client.module.Module;
import com.eclipseware.imnotcheatingyouare.client.setting.Setting;
import net.minecraft.world.item.Items;
import java.util.ArrayList;
import java.util.Arrays;

public class AutoTotem extends Module {
    private Setting mode;
    private Setting activationMode;
    private Setting healthThreshold;
    private Setting popDelay;
    private Setting targetSlot;

    private int popTickGrace = 0;
    private int antiDec = -1;
    private int fifteenTickSafety = -1;
    
    private int swapDelayCounter = -1;
    private int queuedSwapSlot = -1;

    private int mouseSequenceStage = 0;
    private int targetSlotForMouse = -1;

    public AutoTotem() {
        super("AutoTotem", Category.Combat, "Silently replaces totems automatically.");
        setSubCategory("Crystal PvP");
        
        mode = new Setting("Mode", this, "Crystal", new ArrayList<>(Arrays.asList("Crystal", "SMP", "Mouse")));
        activationMode = new Setting("Activation", this, "Always", new ArrayList<>(Arrays.asList("Always", "Low HP")));
        healthThreshold = new Setting("Health Threshold", this, 10, 1, 20, true);
        popDelay = new Setting("Pop Delay (Ticks)", this, 0, 0, 10, true);
        targetSlot = new Setting("Target", this, "Offhand", new ArrayList<>(Arrays.asList("Offhand", "First Hotbar Slot")));
        
        ImnotcheatingyouareClient.INSTANCE.settingsManager.rSetting(mode);
        ImnotcheatingyouareClient.INSTANCE.settingsManager.rSetting(activationMode);
        ImnotcheatingyouareClient.INSTANCE.settingsManager.rSetting(healthThreshold);
        ImnotcheatingyouareClient.INSTANCE.settingsManager.rSetting(popDelay);
        ImnotcheatingyouareClient.INSTANCE.settingsManager.rSetting(targetSlot);
    }

    @Override
    public void onEnable() {
        antiDec = -1;
        fifteenTickSafety = -1;
        swapDelayCounter = -1;
        queuedSwapSlot = -1;
        mouseSequenceStage = 0;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;
        
        if (mouseSequenceStage > 0) {
            handleMouseSequence();
            return;
        }

        if (swapDelayCounter > 0) {
            swapDelayCounter--;
            return;
        } else if (swapDelayCounter == 0 && queuedSwapSlot != -1) {
            performSilentSwap(queuedSwapSlot);
            swapDelayCounter = -1;
            queuedSwapSlot = -1;
            return;
        }

        boolean targetOffhand = targetSlot.getValString().equals("Offhand");
        boolean NeedsTotem = targetOffhand 
            ? mc.player.getOffhandItem().getItem() != Items.TOTEM_OF_UNDYING
            : mc.player.getInventory().getItem(0).getItem() != Items.TOTEM_OF_UNDYING;

        boolean activationActive = activationMode.getValString().equals("Always") || mc.player.getHealth() <= healthThreshold.getValDouble();

        if (mode.getValString().equals("SMP")) {
            if (mc.player.getHealth() > healthThreshold.getValDouble()) {
                if (targetOffhand && mc.player.getOffhandItem().getItem() != Items.SHIELD) {
                    queueSwap(findItem(Items.SHIELD, true));
                }
                return;
            }
        }

        if (NeedsTotem && activationActive) {
            queueSwap(findItem(Items.TOTEM_OF_UNDYING, true)); 
        }
    }

    private void handleMouseSequence() {
        if (mc.player == null) {
            mouseSequenceStage = 0;
            return;
        }
        
        switch (mouseSequenceStage) {
            case 1:
                if (!(mc.screen instanceof net.minecraft.client.gui.screens.inventory.InventoryScreen)) {
                    mc.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(mc.player));
                }
                mouseSequenceStage = 2;
                break;
            case 2:
                if (mc.screen instanceof net.minecraft.client.gui.screens.inventory.InventoryScreen invScreen) {
                    try {
                        net.minecraft.world.inventory.Slot menuSlot = invScreen.getMenu().getSlot(targetSlotForMouse);
                        
                        java.lang.reflect.Field leftPosField = net.minecraft.client.gui.screens.inventory.AbstractContainerScreen.class.getDeclaredField("leftPos");
                        java.lang.reflect.Field topPosField = net.minecraft.client.gui.screens.inventory.AbstractContainerScreen.class.getDeclaredField("topPos");
                        leftPosField.setAccessible(true);
                        topPosField.setAccessible(true);
                        
                        int leftPos = leftPosField.getInt(invScreen);
                        int topPos = topPosField.getInt(invScreen);
                        
                        double targetX = menuSlot.x + leftPos + 8;
                        double targetY = menuSlot.y + topPos + 8;
                        
                        long windowHandle = 0;
                        for (java.lang.reflect.Field field : mc.getWindow().getClass().getDeclaredFields()) {
                            if (field.getType() == long.class) {
                                field.setAccessible(true);
                                windowHandle = field.getLong(mc.getWindow());
                                break;
                            }
                        }
                        double scale = mc.getWindow().getGuiScale();
                        org.lwjgl.glfw.GLFW.glfwSetCursorPos(windowHandle, targetX * scale, targetY * scale);
                    } catch (Exception ignored) {}
                    mouseSequenceStage = 3;
                } else {
                    mouseSequenceStage = 0;
                }
                break;
            case 3:
                int containerId = mc.player.inventoryMenu.containerId;
                boolean targetOffhand = targetSlot.getValString().equals("Offhand");
                int destButton = targetOffhand ? 40 : 0; 
                
                mc.gameMode.handleInventoryMouseClick(containerId, targetSlotForMouse, destButton, net.minecraft.world.inventory.ClickType.SWAP, mc.player);
                mouseSequenceStage = 4;
                break;
            case 4:
                mc.setScreen(null);
                mouseSequenceStage = 0;
                break;
        }
    }

    private void queueSwap(int slot) {
        if (slot == -1) return;
        int delay = Math.max(3, (int) popDelay.getValDouble()); 
        swapDelayCounter = delay;
        queuedSwapSlot = slot;
    }

    private void performSilentSwap(int slot) {
        if (mc.player == null) return;
        mc.player.setSprinting(false);
        
        if (mode.getValString().equals("Mouse")) {
            targetSlotForMouse = slot;
            mouseSequenceStage = 1;
            return;
        }

        if (mc.screen == null || mc.screen instanceof net.minecraft.client.gui.screens.inventory.InventoryScreen) {
            int containerId = mc.player.inventoryMenu.containerId;
            boolean targetOffhand = targetSlot.getValString().equals("Offhand");
            int destButton = targetOffhand ? 40 : 0; // 40 = Offhand, 0 = Hotbar slot 0
            
            mc.gameMode.handleInventoryMouseClick(containerId, slot, destButton, net.minecraft.world.inventory.ClickType.SWAP, mc.player);
        }
    }

    private int findItem(net.minecraft.world.item.Item item, boolean prioritizeHotbar) {
        if (prioritizeHotbar) {
            for (int i = 0; i < 9; i++) {
                if (mc.player.getInventory().getItem(i).getItem() == item) return i + 36;
            }
            for (int i = 9; i < 36; i++) {
                if (mc.player.getInventory().getItem(i).getItem() == item) return i;
            }
        } else {
            for (int i = 9; i < 36; i++) {
                if (mc.player.getInventory().getItem(i).getItem() == item) return i;
            }
            for (int i = 0; i < 9; i++) {
                if (mc.player.getInventory().getItem(i).getItem() == item) return i + 36;
            }
        }
        return -1;
    }
}
