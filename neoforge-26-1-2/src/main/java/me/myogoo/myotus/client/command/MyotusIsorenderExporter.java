package me.myogoo.myotus.client.command;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.myogoo.myotus.Myotus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class MyotusIsorenderExporter {
    private MyotusIsorenderExporter() {
    }

    public static Path exportBlock(BlockState state, Identifier blockId, int size) throws IOException {
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
        TextureTarget target = new TextureTarget("Myotus isorender", size, size, true);
        GpuTextureView previousColorTarget = RenderSystem.outputColorTextureOverride;
        GpuTextureView previousDepthTarget = RenderSystem.outputDepthTextureOverride;
        GpuBufferSlice previousLighting = RenderSystem.getShaderLights();
        Matrix4fStack modelView = RenderSystem.getModelViewStack();

        RenderSystem.backupProjectionMatrix();
        modelView.pushMatrix();
        try (ProjectionMatrixBuffer projection = new ProjectionMatrixBuffer("myotus isorender");
             FeatureRenderDispatcher featureRenderer = new FeatureRenderDispatcher(
                     new SubmitNodeStorage(),
                     minecraft.getModelManager(),
                     minecraft.renderBuffers().bufferSource(),
                     minecraft.getAtlasManager(),
                     minecraft.renderBuffers().outlineBufferSource(),
                     minecraft.renderBuffers().crumblingBufferSource(),
                     minecraft.font,
                     minecraft.gameRenderer.getGameRenderState())) {
            RenderSystem.outputColorTextureOverride = Objects.requireNonNull(target.getColorTextureView());
            RenderSystem.outputDepthTextureOverride = Objects.requireNonNull(target.getDepthTextureView());
            RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(
                    Objects.requireNonNull(target.getColorTexture()),
                    0x00000000,
                    Objects.requireNonNull(target.getDepthTexture()),
                    1.0);

            RenderSystem.setProjectionMatrix(
                    projection.getBuffer(new Matrix4f().setOrtho(
                            0.0F, size, size, 0.0F, -1000.0F, 1000.0F)),
                    ProjectionType.ORTHOGRAPHIC);
            modelView.identity();
            minecraft.gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_3D);

            PoseStack poseStack = new PoseStack();
            poseStack.translate(size / 2.0F, size * 0.52F, 120.0F);
            poseStack.scale(size * 0.54F, -size * 0.54F, size * 0.54F);
            poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(225.0F));
            poseStack.translate(-0.5F, -0.5F, -0.5F);

            BlockModelRenderState renderState = new BlockModelRenderState();
            minecraft.getBlockModelResolver().update(renderState, state, BlockDisplayContext.create());
            renderState.submitMultiLayer(
                    poseStack,
                    featureRenderer.getSubmitNodeStorage(),
                    LightCoordsUtil.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY,
                    0);
            featureRenderer.renderAllFeatures();
            minecraft.renderBuffers().bufferSource().endBatch();

            try (NativeImage image = readTransparent(target)) {
                image.writeToFile(output);
            }
        } finally {
            modelView.popMatrix();
            RenderSystem.restoreProjectionMatrix();
            RenderSystem.setShaderLights(previousLighting);
            RenderSystem.outputColorTextureOverride = previousColorTarget;
            RenderSystem.outputDepthTextureOverride = previousDepthTarget;
            target.destroyBuffers();
        }

        return output;
    }

    private static NativeImage readTransparent(TextureTarget target) {
        GpuTexture source = Objects.requireNonNull(target.getColorTexture());
        int width = target.width;
        int height = target.height;
        int pixelSize = source.getFormat().pixelSize();
        GpuBuffer buffer = RenderSystem.getDevice().createBuffer(
                () -> "Myotus isorender readback",
                GpuBuffer.USAGE_MAP_READ | GpuBuffer.USAGE_COPY_DST,
                (long) width * height * pixelSize);
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        CompletableFuture<NativeImage> result = new CompletableFuture<>();

        try {
            encoder.copyTextureToBuffer(source, buffer, 0L, () -> {
                NativeImage image = null;
                try (GpuBuffer.MappedView read = encoder.mapBuffer(buffer, true, false)) {
                    image = new NativeImage(width, height, false);
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            int abgr = read.data().getInt((x + y * width) * pixelSize);
                            image.setPixelABGR(x, height - y - 1, abgr);
                        }
                    }
                    result.complete(image);
                } catch (Throwable e) {
                    if (image != null) {
                        image.close();
                    }
                    result.completeExceptionally(e);
                } finally {
                    buffer.close();
                }
            }, 0);
        } catch (Throwable e) {
            buffer.close();
            throw e;
        }

        while (!result.isDone()) {
            RenderSystem.executePendingTasks();
            Thread.onSpinWait();
        }
        return result.join();
    }

    private static String blockStateSuffix(BlockState state) {
        if (state.isSingletonState()) {
            return "";
        }
        return state.getValues()
                .sorted(Comparator.comparing(entry -> entry.property().getName()))
                .map(MyotusIsorenderExporter::formatProperty)
                .collect(Collectors.joining("_", "_", ""));
    }

    private static String formatProperty(Property.Value<?> entry) {
        return entry.property().getName() + "_" + entry.valueName();
    }
}
