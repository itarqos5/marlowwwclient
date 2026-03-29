package com.eclipseware.imnotcheatingyouare.client.utils;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector4f;

import java.awt.Color;

public class RenderUtils {

    public static Vector3d project2D(double x, double y, double z, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();
        
        // FIX 1: Safely get the Camera position regardless of Yarn/Mojmap mappings
        Vec3 camPos = getCameraPos(camera); 
        
        Vector4f vec = new Vector4f((float)(x - camPos.x), (float)(y - camPos.y), (float)(z - camPos.z), 1.0f);
        
        // FIX 2: Reconstruct the camera view
        Matrix4f viewMatrix = new Matrix4f().rotation(camera.rotation()).invert();
        viewMatrix.transform(vec);
        
        // FIX 3: Rip the Dynamic Sprint FOV and strictly cast it to a float to prevent compiler errors!
        float dynamicFov = getDynamicFov(camera, partialTicks);
        Matrix4f projMatrix = mc.gameRenderer.getProjectionMatrix(dynamicFov); 
        projMatrix.transform(vec);
        
        if (vec.w <= 0.0f) return null; 
        
        vec.div(vec.w); 
        
        double screenWidth = mc.getWindow().getGuiScaledWidth();
        double screenHeight = mc.getWindow().getGuiScaledHeight();
        
        double screenX = (screenWidth / 2.0) * (vec.x() + 1.0);
        double screenY = (screenHeight / 2.0) * (1.0 - vec.y());
        
        return new Vector3d(screenX, screenY, vec.z());
    }

    // Mapping-Independent Reflection to unlock the hidden Sprint FOV!
    private static float getDynamicFov(Camera camera, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        try {
            for (java.lang.reflect.Method m : mc.gameRenderer.getClass().getDeclaredMethods()) {
                if (m.getReturnType() == double.class && m.getParameterCount() == 3) {
                    m.setAccessible(true); 
                    // Strictly cast the double to a float so your compiler doesn't panic
                    return ((Double) m.invoke(mc.gameRenderer, camera, partialTicks, true)).floatValue();
                }
            }
        } catch (Exception e) {}
        return mc.options.fov().get().floatValue();
    }

    // Fallback handler for Camera Pos
    private static Vec3 getCameraPos(Camera camera) {
        try {
            return (Vec3) camera.getClass().getMethod("getPosition").invoke(camera); // Mojmap
        } catch (Exception e) {
            try {
                return (Vec3) camera.getClass().getMethod("getPos").invoke(camera); // Yarn
            } catch (Exception ex) {
                return Minecraft.getInstance().player.getEyePosition(1.0f);
            }
        }
    }

    public static void drawLine2D(GuiGraphics graphics, double x1, double y1, double x2, double y2, Color color) {
        double length = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
        float angle = (float) Math.atan2(y2 - y1, x2 - x1);
        
        graphics.pose().pushMatrix();
        graphics.pose().translate((float)x1, (float)y1);
        graphics.pose().rotate(angle); 
        graphics.fill(0, 0, (int)Math.ceil(length), 1, color.getRGB());
        graphics.pose().popMatrix();
    }
}