package com.eclipseware.imnotcheatingyouare.client.module.impl;

import com.eclipseware.imnotcheatingyouare.client.ImnotcheatingyouareClient;
import com.eclipseware.imnotcheatingyouare.client.module.Category;
import com.eclipseware.imnotcheatingyouare.client.module.Module;
import com.eclipseware.imnotcheatingyouare.client.setting.Setting;
import com.eclipseware.imnotcheatingyouare.client.utils.cheat.AntiCheatProfile;
import com.eclipseware.imnotcheatingyouare.client.utils.cheat.GCDFix;
import com.eclipseware.imnotcheatingyouare.client.utils.cheat.RotationSpoof;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AimAssist extends Module {

    private Entity target;
    private int clickGraceTicks = 0;

    private float yawVelocity   = 0f;
    private float pitchVelocity = 0f;

    public AimAssist() {
        super("AimAssist", Category.Combat);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null || mc.screen != null) {
            resetPhysics();
            return;
        }

        // Update GCD for current sensitivity every tick
        GCDFix.update(mc.options.sensitivity().get());

        // Jitter-click protection
        if (mc.options.keyAttack.isDown()) {
            clickGraceTicks = 5;
        } else if (clickGraceTicks > 0) {
            clickGraceTicks--;
        }

        Setting attackOnlySetting = ImnotcheatingyouareClient.INSTANCE.settingsManager.getSettingByName(this, "Attack Only");
        if (attackOnlySetting != null && attackOnlySetting.getValBoolean()) {
            if (clickGraceTicks == 0) {
                resetPhysics();
                return;
            }
        }

        chooseTarget();
        if (target == null) {
            resetPhysics();
            return;
        }

        AABB box      = target.getBoundingBox();
        Vec3 aimPoint = new Vec3(box.getCenter().x, box.getCenter().y, box.getCenter().z);
        Vec3 eyes     = mc.player.getEyePosition();

        double diffX = aimPoint.x - eyes.x;
        double diffY = aimPoint.y - eyes.y;
        double diffZ = aimPoint.z - eyes.z;
        double dist  = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float neededYaw   = (float) (Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F);
        float neededPitch = (float) -Math.toDegrees(Math.atan2(diffY, dist));

        float currentYaw   = mc.player.getYRot();
        float currentPitch = mc.player.getXRot();

        float yawDiff   = Mth.wrapDegrees(neededYaw   - currentYaw);
        float pitchDiff = Mth.wrapDegrees(neededPitch - currentPitch);

        Setting modeSetting      = ImnotcheatingyouareClient.INSTANCE.settingsManager.getSettingByName(this, "Mode");
        Setting speedSetting     = ImnotcheatingyouareClient.INSTANCE.settingsManager.getSettingByName(this, "Speed");
        Setting smoothnessSetting= ImnotcheatingyouareClient.INSTANCE.settingsManager.getSettingByName(this, "Smoothness");

        String mode       = modeSetting       != null ? modeSetting.getValString()         : "Smooth";
        float  speed      = speedSetting      != null ? (float) speedSetting.getValDouble() : 3.0f;
        float  smoothness = smoothnessSetting != null ? (float) smoothnessSetting.getValDouble() : 7.0f;

        // Profile-capped max turn
        float maxTurn = Math.min(speed * 2.5f, AntiCheatProfile.safeAimMaxTurn());
        float ease    = Math.max(1.0f, smoothness * 1.5f);

        float stepYaw   = 0f;
        float stepPitch = 0f;

        if (mode.equals("Smooth")) {
            stepYaw   = Mth.clamp(yawDiff   / ease, -maxTurn, maxTurn);
            stepPitch = Mth.clamp(pitchDiff / ease, -maxTurn, maxTurn);

        } else if (mode.equals("Human")) {
            float swayX = (float) (Math.sin(System.currentTimeMillis() / 200.0) * 0.4);
            float swayY = (float) (Math.cos(System.currentTimeMillis() / 250.0) * 0.4);

            yawVelocity   = (yawVelocity   * 0.6f) + (yawDiff   / ease + swayX) * 0.4f;
            pitchVelocity = (pitchVelocity * 0.6f) + (pitchDiff / ease + swayY) * 0.4f;

            stepYaw   = Mth.clamp(yawVelocity,   -maxTurn, maxTurn);
            stepPitch = Mth.clamp(pitchVelocity, -maxTurn, maxTurn);

        } else { // Linear
            stepYaw   = Mth.clamp(yawDiff,   -maxTurn, maxTurn);
            stepPitch = Mth.clamp(pitchDiff, -maxTurn, maxTurn);
        }

        // ── GCD snap — makes deltas look like real mouse input ──────────────
        stepYaw   = GCDFix.snapDelta(stepYaw);
        stepPitch = GCDFix.snapDelta(stepPitch);

        if (Math.abs(stepYaw) > 0.05f || Math.abs(stepPitch) > 0.05f) {
            float newYaw   = currentYaw   + stepYaw;
            float newPitch = currentPitch + stepPitch;

            if (AntiCheatProfile.silentRotations()) {
                // Silent aim: only inject into outgoing packets, camera stays put
                RotationSpoof.set(newYaw, newPitch, 2);
            } else {
                mc.player.setYRot(newYaw);
                mc.player.setXRot(newPitch);
            }
        }
    }

    private void resetPhysics() {
        target        = null;
        yawVelocity   = 0f;
        pitchVelocity = 0f;
    }

    private void chooseTarget() {
        Setting rangeSetting = ImnotcheatingyouareClient.INSTANCE.settingsManager.getSettingByName(this, "Range");
        Setting fovSetting   = ImnotcheatingyouareClient.INSTANCE.settingsManager.getSettingByName(this, "FOV");

        double range   = rangeSetting != null ? rangeSetting.getValDouble() : 4.5;
        double maxFov  = fovSetting   != null ? fovSetting.getValDouble()   : 120.0;

        Entity bestTarget = null;
        double bestAngle  = maxFov / 2.0;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity == mc.player || !entity.isAlive() || !(entity instanceof LivingEntity)) continue;
            if (mc.player.distanceTo(entity) > range) continue;
            if (!isValidTarget(entity)) continue;

            AABB box      = entity.getBoundingBox();
            Vec3 aimPoint = new Vec3(box.getCenter().x, box.getCenter().y, box.getCenter().z);
            double angle  = getAngleToLookVec(aimPoint);

            if (angle <= bestAngle) {
                bestAngle  = angle;
                bestTarget = entity;
            }
        }
        target = bestTarget;
    }

    private double getAngleToLookVec(Vec3 targetVec) {
        Vec3 lookVec = mc.player.getViewVector(1.0F);
        Vec3 diffVec = targetVec.subtract(mc.player.getEyePosition()).normalize();
        double dot   = Mth.clamp(lookVec.dot(diffVec), -1.0, 1.0);
        return Math.toDegrees(Math.acos(dot));
    }

    private boolean isValidTarget(Entity entity) {
        Setting playersSetting  = ImnotcheatingyouareClient.INSTANCE.settingsManager.getSettingByName(this, "Players");
        Setting hostileSetting  = ImnotcheatingyouareClient.INSTANCE.settingsManager.getSettingByName(this, "Hostile Mobs");
        Setting passiveSetting  = ImnotcheatingyouareClient.INSTANCE.settingsManager.getSettingByName(this, "Passive Mobs");

        if (entity instanceof Player)       return playersSetting  != null && playersSetting.getValBoolean();
        if (entity instanceof Enemy)        return hostileSetting  != null && hostileSetting.getValBoolean();
        if (entity instanceof Animal || entity instanceof LivingEntity)
                                            return passiveSetting  != null && passiveSetting.getValBoolean();
        return false;
    }
}
