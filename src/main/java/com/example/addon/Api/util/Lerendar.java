package com.example.addon.Api.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public class Lerendar {
    public static void drawGradientRect(MatrixStack matrix, float left, float top, float right, float bottom, boolean sideways, int startColor, int endColor) {
         float f = (float) (startColor >> 24 & 255) / 255.0F;
            float f1 = (float) (startColor >> 16 & 255) / 255.0F;
            float f2 = (float) (startColor >> 8 & 255) / 255.0F;
            float f3 = (float) (startColor & 255) / 255.0F;
            float f4 = (float) (endColor >> 24 & 255) / 255.0F;
            float f5 = (float) (endColor >> 16 & 255) / 255.0F;
            float f6 = (float) (endColor >> 8 & 255) / 255.0F;
            float f7 = (float) (endColor & 255) / 255.0F;
            Matrix4f posMatrix = matrix.peek().getPositionMatrix();

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            if (sideways) {
                bufferBuilder.vertex(posMatrix, left, top, 0.0F).color(f1, f2, f3, f);
                bufferBuilder.vertex(posMatrix, left, bottom, 0.0F).color(f1, f2, f3, f);
                bufferBuilder.vertex(posMatrix, right, bottom, 0.0F).color(f5, f6, f7, f4);
                bufferBuilder.vertex(posMatrix, right, top, 0.0F).color(f5, f6, f7, f4);
            } else {
                bufferBuilder.vertex(posMatrix, right, top, 0.0F).color(f1, f2, f3, f);
                bufferBuilder.vertex(posMatrix, left, top, 0.0F).color(f1, f2, f3, f);
                bufferBuilder.vertex(posMatrix, left, bottom, 0.0F).color(f5, f6, f7, f4);
                bufferBuilder.vertex(posMatrix, right, bottom, 0.0F).color(f5, f6, f7, f4);
            }
            BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
            RenderSystem.disableBlend();
        }
    }
