package com.eclipseware.imnotcheatingyouare.client.module.impl;

import com.eclipseware.imnotcheatingyouare.client.ImnotcheatingyouareClient;
import com.eclipseware.imnotcheatingyouare.client.module.Category;
import com.eclipseware.imnotcheatingyouare.client.module.Module;
import com.eclipseware.imnotcheatingyouare.client.setting.Setting;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.world.entity.ai.attributes.Attributes;
import java.util.Objects;

public class Reach extends Module {

    public Reach() {
        super("Reach", Category.Combat, "Slightly increases your interaction range.");
        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        Setting reachSetting = ImnotcheatingyouareClient.INSTANCE.settingsManager.getSettingByName(this, "Distance");
        double reachAdd = reachSetting != null ? reachSetting.getValDouble() : 0.5;

        if (isToggled()) {
            // Vanilla Entity reach is 3.0. Max slider is 1.0. Max total reach = 4.0!
            Objects.requireNonNull(mc.player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE)).setBaseValue(4.5 + reachAdd);
            Objects.requireNonNull(mc.player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE)).setBaseValue(3.0 + reachAdd);
        } else {
            Objects.requireNonNull(mc.player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE)).setBaseValue(4.5);
            Objects.requireNonNull(mc.player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE)).setBaseValue(3.0);
        }
    }
    
    @Override
    public void onDisable() {
        if (mc.player != null) {
            Objects.requireNonNull(mc.player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE)).setBaseValue(4.5);
            Objects.requireNonNull(mc.player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE)).setBaseValue(3.0);
        }
    }
}