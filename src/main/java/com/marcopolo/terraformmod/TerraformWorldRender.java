package com.marcopolo.terraformmod;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class TerraformWorldRender {

    public static void renderCube(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        Vec3d cameraPos = context.camera().getPos();
        BlockPos playerPos = client.player.getBlockPos();
        
        int rangeX = TerraformHandler.getRangeX();
        int rangeY = TerraformHandler.getRangeY();
        int rangeZ = TerraformHandler.getRangeZ();

        // Berechne die Box-Eckpunkte relativ zum Spieler
        double minX = playerPos.getX() - rangeX - cameraPos.x;
        double minY = playerPos.getY() - rangeY - cameraPos.y;
        double minZ = playerPos.getZ() - rangeZ - cameraPos.z;
        
        double maxX = playerPos.getX() + rangeX + 1 - cameraPos.x;
        double maxY = playerPos.getY() + rangeY + 1 - cameraPos.y;
        double maxZ = playerPos.getZ() + rangeZ + 1 - cameraPos.z;

        // Rendering setup
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(net.minecraft.client.render.GameRenderer::getPositionColorShader);

        MatrixStack matrices = context.matrixStack();
        matrices.push();

        VertexConsumer vertexConsumer = context.consumers().getBuffer(net.minecraft.client.render.RenderLayer.getLines());

        // Zeichne die 12 Kanten des Würfels (hellblau, transparent)
        float r = 0.5f;
        float g = 0.8f;
        float b = 1.0f;
        float alpha = 0.5f;

        // Untere 4 Kanten
        drawLine(vertexConsumer, matrices, minX, minY, minZ, maxX, minY, minZ, r, g, b, alpha);
        drawLine(vertexConsumer, matrices, maxX, minY, minZ, maxX, minY, maxZ, r, g, b, alpha);
        drawLine(vertexConsumer, matrices, maxX, minY, maxZ, minX, minY, maxZ, r, g, b, alpha);
        drawLine(vertexConsumer, matrices, minX, minY, maxZ, minX, minY, minZ, r, g, b, alpha);

        // Obere 4 Kanten
        drawLine(vertexConsumer, matrices, minX, maxY, minZ, maxX, maxY, minZ, r, g, b, alpha);
        drawLine(vertexConsumer, matrices, maxX, maxY, minZ, maxX, maxY, maxZ, r, g, b, alpha);
        drawLine(vertexConsumer, matrices, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, alpha);
        drawLine(vertexConsumer, matrices, minX, maxY, maxZ, minX, maxY, minZ, r, g, b, alpha);

        // 4 Vertikale Kanten
        drawLine(vertexConsumer, matrices, minX, minY, minZ, minX, maxY, minZ, r, g, b, alpha);
        drawLine(vertexConsumer, matrices, maxX, minY, minZ, maxX, maxY, minZ, r, g, b, alpha);
        drawLine(vertexConsumer, matrices, maxX, minY, maxZ, maxX, maxY, maxZ, r, g, b, alpha);
        drawLine(vertexConsumer, matrices, minX, minY, maxZ, minX, maxY, maxZ, r, g, b, alpha);

        matrices.pop();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static void drawLine(VertexConsumer vertexConsumer, MatrixStack matrices, 
                                 double x1, double y1, double z1, 
                                 double x2, double y2, double z2,
                                 float r, float g, float b, float alpha) {
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        
        vertexConsumer.vertex(positionMatrix, (float)x1, (float)y1, (float)z1)
                .color(r, g, b, alpha).next();
        vertexConsumer.vertex(positionMatrix, (float)x2, (float)y2, (float)z2)
                .color(r, g, b, alpha).next();
    }
}
