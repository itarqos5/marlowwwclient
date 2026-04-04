package com.eclipseware.imnotcheatingyouare.client.module.impl;

import com.eclipseware.imnotcheatingyouare.client.ImnotcheatingyouareClient;
import com.eclipseware.imnotcheatingyouare.client.module.Category;
import com.eclipseware.imnotcheatingyouare.client.module.Module;
import com.eclipseware.imnotcheatingyouare.client.setting.Setting;
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
    private int clickGraceTicks = 0; // Fixes jitter-clicking tracking drops
    
    private float yawVelocity = 0f;
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

        // Jitter-click protection: Keep AimAssist alive for 5 ticks after lifting the mouse button
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

        chooseTarget(); // Wurst's solid target selection
        if (target == null) {
            resetPhysics();
            return;
        }

        // Aim at the exact center of the bounding box
        AABB box = target.getBoundingBox();
        Vec3 aimPoint = new Vec3(box.getCenter().x, box.getCenter().y, box.getCenter().z);

        Vec3 eyes = mc.player.getEyePosition();
        double diffX = aimPoint.x - eyes.x;
        double diffY = aimPoint.y - eyes.y;
        double diffZ = aimPoint.z - eyes.z;
        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float neededYaw = (float) (Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F);
        float neededPitch = (float) -Math.toDegrees(Math.atan2(diffY, dist));

        float currentYaw = mc.player.getYRot();
        float currentPitch = mc.player.getXRot();

        float yawDiff = Mth.wrapDegrees(neededYaw - currentYaw);
        float pitchDiff = Mth.wrapDegrees(neededPitch - currentPitch);

        // Fetch user configs
        Setting modeSetting = ImnotcheatingyouareClient.INSTANCE.settingsManager.getSettingByName(this, "Mode");
        Setting speedSetting = ImnotcheatingyouareClient.INSTANCE.settingsManager.getSettingByName(this, "Speed");
        Setting smoothnessSetting = ImnotcheatingyouareClient.INSTANCE.settingsManager.getSettingByName(this, "Smoothness");

        String mode = modeSetting != null ? modeSetting.getValString() : "Smooth";
        float speed = speedSetting != null ? (float) speedSetting.getValDouble() : 3.0f;
        float smoothness = smoothnessSetting != null ? (float) smoothnessSetting.getValDouble() : 7.0f;

        float stepYaw = 0f;
        float stepPitch = 0f;

        // Exponential Easing: Higher smoothness = larger divisor = slower, smoother camera movement
        float ease = Math.max(1.0f, smoothness * 1.5f);
        float maxTurn = speed * 2.5f;

        if (mode.equals("Smooth")) {
            // Buttery exponential tracking. Slows down naturally as it approaches the target.
            stepYaw = yawDiff / ease;
            stepPitch = pitchDiff / ease;

            stepYaw = Mth.clamp(stepYaw, -maxTurn, maxTurn);
            stepPitch = Mth.clamp(stepPitch, -maxTurn, maxTurn);

        } else if (mode.equals("Human")) {
            // Adds slight simulated momentum to "Smooth" so it feels like wrist movement
            float rawStepYaw = yawDiff / ease;
            float rawStepPitch = pitchDiff / ease;

            float swayX = (float) (Math.sin(System.currentTimeMillis() / 200.0) * 0.4);
            float swayY = (float) (Math.cos(System.currentTimeMillis() / 250.0) * 0.4);

            yawVelocity = (yawVelocity * 0.6f) + (rawStepYaw + swayX) * 0.4f;
            pitchVelocity = (pitchVelocity * 0.6f) + (rawStepPitch + swayY) * 0.4f;

            stepYaw = Mth.clamp(yawVelocity, -maxTurn, maxTurn);
            stepPitch = Mth.clamp(pitchVelocity, -maxTurn, maxTurn);

        } else { 
            // Linear (The old Wurst method) - Constant speed, can feel snappy
            stepYaw = Mth.clamp(yawDiff, -maxTurn, maxTurn);
            stepPitch = Mth.clamp(pitchDiff, -maxTurn, maxTurn);
        }

        // Apply movement if it's large enough to matter
        if (Math.abs(stepYaw) > 0.05f || Math.abs(stepPitch) > 0.05f) {
            mc.player.setYRot(currentYaw + stepYaw);
            mc.player.setXRot(currentPitch + stepPitch);
        }
    }

    private void resetPhysics() {
        target = null;
        yawVelocity = 0f;
        pitchVelocity = 0f;
    }

    /**
     * Wurst's highly accurate target filtering
     */
    private void chooseTarget() {
        Setting rangeSetting = ImnotcheatingyouareClient.INSTANCE.settingsManager.getSettingByName(this, "Range");
        Setting fovSetting = ImnotcheatingyouareClient.INSTANCE.settingsManager.getSettingByName(this, "FOV");

        double range = rangeSetting != null ? rangeSetting.getValDouble() : 4.5;
        double maxFov = fovSetting != null ? fovSetting.getValDouble() : 120.0;

        Entity bestTarget = null;
        double bestAngle = maxFov / 2.0; 

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity == mc.player || !entity.isAlive() || !(entity instanceof LivingEntity)) continue;
            if (mc.player.distanceTo(entity) > range) continue;
            if (!isValidTarget(entity)) continue;

            AABB box = entity.getBoundingBox();
            Vec3 aimPoint = new Vec3(box.getCenter().x, box.getCenter().y, box.getCenter().z);
            double angle = getAngleToLookVec(aimPoint);
            
            if (angle <= bestAngle) {
                bestAngle = angle;
                bestTarget = entity;
            }
        }
        target = bestTarget;
    }

    /**
     * Wurst's dot-product logic to find the true angle to target
     */
    private double getAngleToLookVec(Vec3 targetVec) {
        Vec3 lookVec = mc.player.getViewVector(1.0F);
        Vec3 diffVec = targetVec.subtract(mc.player.getEyePosition()).normalize();
        double dot = lookVec.dot(diffVec);
        dot = Mth.clamp(dot, -1.0, 1.0);
        return Math.toDegrees(Math.acos(dot));
    }

    private boolean isValidTarget(Entity entity) {
        Setting playersSetting = ImnotcheatingyouareClient.INSTANCE.settingsManager.getSettingByName(this, "Players");
        Setting hostileSetting = ImnotcheatingyouareClient.INSTANCE.settingsManager.getSettingByName(this, "Hostile Mobs");
        Setting passiveSetting = ImnotcheatingyouareClient.INSTANCE.settingsManager.getSettingByName(this, "Passive Mobs");

        if (entity instanceof Player) {
            return playersSetting != null && playersSetting.getValBoolean();
        }
        if (entity instanceof Enemy) {
            return hostileSetting != null && hostileSetting.getValBoolean();
        }
        if (entity instanceof Animal || entity instanceof LivingEntity) {
            return passiveSetting != null && passiveSetting.getValBoolean();
        }
        return false;
    }
}