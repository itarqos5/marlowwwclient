package com.eclipseware.imnotcheatingyouare.client.module.impl;

import com.eclipseware.imnotcheatingyouare.client.ImnotcheatingyouareClient;
import com.eclipseware.imnotcheatingyouare.client.module.Category;
import com.eclipseware.imnotcheatingyouare.client.module.Module;
import com.eclipseware.imnotcheatingyouare.client.setting.Setting;

public class JumpReset extends Module {
private boolean shouldJump = false;
private int jumpDelayTicks = 0;
public JumpReset() {
super("JumpReset", Category.Combat, "Jumps when you take knockback to stay in combo range");
}
@Override
public void onTick() {
if (!isToggled() || mc.player == null || mc.options == null) return;
if (shouldJump && mc.player.onGround()) {
Setting delaySetting = ImnotcheatingyouareClient.INSTANCE.settingsManager.getSettingByName(this, "Delay (Ticks)");
int delay = delaySetting != null ? (int) delaySetting.getValDouble() : 0;
if (jumpDelayTicks >= delay) {
mc.options.keyJump.setDown(true);
shouldJump = false;
jumpDelayTicks = 0;
} else {
jumpDelayTicks++;
}
}
}
public void onKnockback() {
if (!isToggled() || mc.player == null) return;
Setting chanceSetting = ImnotcheatingyouareClient.INSTANCE.settingsManager.getSettingByName(this, "Chance (%)");
double chance = chanceSetting != null ? chanceSetting.getValDouble() : 100.0;
if (Math.random() * 100.0 > chance) return;
Setting modeSetting = ImnotcheatingyouareClient.INSTANCE.settingsManager.getSettingByName(this, "Mode");
String mode = modeSetting != null ? modeSetting.getValString() : "Legit";
if (mode.equals("Blatant")) {
if (mc.player.onGround()) {
mc.player.jumpFromGround();
}
} else {
shouldJump = true;
jumpDelayTicks = 0;
}
}
@Override
public void onDisable() {
shouldJump = false;
jumpDelayTicks = 0;
if (mc.options != null && mc.options.keyJump != null) {
mc.options.keyJump.setDown(false);
}
}
}