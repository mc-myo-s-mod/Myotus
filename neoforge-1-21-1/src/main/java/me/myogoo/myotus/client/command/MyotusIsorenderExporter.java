package me.myogoo.myotus.client.command;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.math.Axis;
import me.myogoo.myotus.Myotus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

public final class MyotusIsorenderExporter {
    private MyotusIsorenderExporter() {
    }

    public static Path exportBlock(BlockState state, ResourceLocation blockId, int size) throws IOException {
        Minecraft minecraft = Minecraft.getInstance();
        String fileName = blockId.getNamespace() + "_" + blockId.getPath().replace('/', '_') + blockStateSuffix(state) + "_" + size + ".png";
        Path output = minecraft.gameDirectory.toPath()
                .resolve("screenshots")
                .resolve(Myotus.MODID)
                .resolve("isorender")
                .resolve("blocks")
                .resolve(fileName);
        Files.createDirectories(output.getParent());

        RenderSystem.assertOnRenderThread();
        int framebuffer = GL30.glGenFramebuffers();
        int texture = TextureUtil.generateTextureId();
        int depth = GL11.glGenTextures();

        int previousFramebuffer = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
        int[] previousViewport = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, previousViewport);

        try {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, size, size, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, 0L);

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, depth);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_DEPTH_COMPONENT24, size, size, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_UNSIGNED_INT, 0L);

            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);
            GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, texture, 0);
            GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, depth, 0);
            GL11.glViewport(0, 0, size, size);
            GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_BLEND);

            RenderSystem.backupProjectionMatrix();
            RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0.0F, size, size, 0.0F, -1000.0F, 1000.0F), VertexSorting.ORTHOGRAPHIC_Z);
            Lighting.setupFor3DItems();

            PoseStack poseStack = new PoseStack();
            poseStack.translate(size / 2.0F, size * 0.52F, 120.0F);
            poseStack.scale(size * 0.54F, -size * 0.54F, size * 0.54F);
            poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(225.0F));
            poseStack.translate(-0.5F, -0.5F, -0.5F);

            BlockRenderDispatcher dispatcher = minecraft.getBlockRenderer();
            MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
            dispatcher.renderSingleBlock(state, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            bufferSource.endBatch();

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
            NativeImage image = new NativeImage(size, size, true);
            image.downloadTexture(0, false);
            image.flipY();
            image.writeToFile(output);
            image.close();
        } finally {
            RenderSystem.restoreProjectionMatrix();
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, previousFramebuffer);
            GL11.glViewport(previousViewport[0], previousViewport[1], previousViewport[2], previousViewport[3]);
            TextureUtil.releaseTextureId(texture);
            GL11.glDeleteTextures(depth);
            GL30.glDeleteFramebuffers(framebuffer);
        }

        return output;
    }

    private static String blockStateSuffix(BlockState state) {
        if (state.getValues().isEmpty()) {
            return "";
        }
        return state.getValues().entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().getName()))
                .map(MyotusIsorenderExporter::formatProperty)
                .collect(Collectors.joining("_", "_", ""));
    }

    private static String formatProperty(Map.Entry<Property<?>, Comparable<?>> entry) {
        return entry.getKey().getName() + "_" + entry.getValue();
    }
}
