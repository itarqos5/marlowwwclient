package com.eclipseware.imnotcheatingyouare.client.module.impl;

import com.eclipseware.imnotcheatingyouare.client.module.Category;
import com.eclipseware.imnotcheatingyouare.client.module.Module;
import com.eclipseware.imnotcheatingyouare.mixin.client.MinecraftAccessor;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class LungeAssist extends Module {
    private boolean needsSwapBack = false;
    private int originalSlot = -1;
    private int swapDelayTicks = -1;

    public LungeAssist() {
        super("LungeAssist", Category.Combat);
    }

    @Override
    public void onKeybind() {
        if (mc.player == null || mc.getConnection() == null) return;
        
        int spearSlot = findLungeSpear(mc.player);
        if (spearSlot == -1) {
            super.onKeybind();
            return;
        }
        
        int oldSlot = mc.player.getInventory().getSelectedSlot();
        if (oldSlot == spearSlot) {
            ((MinecraftAccessor) mc).invokeStartAttack();
            return;
        }
        
        // Physically swap the slot to force client-side attributes to apply
        mc.player.getInventory().setSelectedSlot(spearSlot);
        mc.getConnection().send(new ServerboundSetCarriedItemPacket(spearSlot));
        
        // Execute the native left-click logic exactly like the game does (triggers enchants!)
        ((MinecraftAccessor) mc).invokeStartAttack();
        
        // Delay the swap back by 1 tick so the game resolves the attack while holding the spear
        needsSwapBack = true;
        originalSlot = oldSlot;
        swapDelayTicks = 1;
    }

    @Override
    public void onTick() {
        if (needsSwapBack && mc.player != null && mc.getConnection() != null) {
            swapDelayTicks--;
            if (swapDelayTicks <= 0) {
                // Revert slot and sync with server
                mc.player.getInventory().setSelectedSlot(originalSlot);
                mc.getConnection().send(new ServerboundSetCarriedItemPacket(originalSlot));
                needsSwapBack = false;
            }
        }
    }

    @Override
    public void onDisable() {
        needsSwapBack = false;
    }

    private int findLungeSpear(Player player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            String itemName = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
            
            if (itemName.contains("spear")) {
                for (var enchant : stack.getEnchantments().keySet()) {
                    if (enchant.unwrapKey().isPresent() && enchant.unwrapKey().get().toString().contains("lunge")) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }
}