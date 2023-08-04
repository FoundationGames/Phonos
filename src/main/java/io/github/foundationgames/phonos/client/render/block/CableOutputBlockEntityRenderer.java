package io.github.foundationgames.phonos.client.render.block;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.foundationgames.phonos.Phonos;
import io.github.foundationgames.phonos.PhonosClient;
import io.github.foundationgames.phonos.client.model.BasicModel;
import io.github.foundationgames.phonos.client.render.BlockEntityClientState;
import io.github.foundationgames.phonos.client.render.CableRenderer;
import io.github.foundationgames.phonos.config.PhonosClientConfig;
import io.github.foundationgames.phonos.world.sound.block.OutputBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class CableOutputBlockEntityRenderer<E extends BlockEntity & OutputBlockEntity> implements BlockEntityRenderer<E> {
    public static final Identifier TEXTURE = Phonos.id("textures/entity/audio_cable.png");
    private final BasicModel cableEndModel;

    public CableOutputBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        cableEndModel = new BasicModel(ctx.getLayerModelPart(PhonosClient.AUDIO_CABLE_END_LAYER));
    }

    @Override
    public void render(E entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        RenderLayer layer = cableEndModel.getLayer(TEXTURE);
        var buffer = vertexConsumers.getBuffer(layer);
        var config = PhonosClientConfig.get();

        matrices.push();
        matrices.translate(-entity.getPos().getX(), -entity.getPos().getY(), -entity.getPos().getZ());

        BlockEntityClientState clientState = entity.getClientState();

        boolean rerender = clientState.buffer == null || clientState.dirty;

        if (rerender) {
            // If we're re-rendering into the vertexbuffer, create a new VBO, grab the tessellator and start tessellating with our vertex format
            VertexBuffer vbo = new VertexBuffer(VertexBuffer.Usage.STATIC);
            BufferBuilder builder = Tessellator.getInstance().getBuffer();
            builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL);
            clientState.buffer = vbo;
        }

        // Render each connection point in immediate mode, and render cables into the given vertex buffer
        entity.getOutputs().forEach((i, conn) ->
                CableRenderer.renderConnection(clientState, entity, config,  entity.getWorld(), conn, matrices, buffer, cableEndModel, overlay, tickDelta));

        VertexBuffer vbo = clientState.buffer;

        if (rerender) {
            // If we rerendered, upload the buffer to the GPU and mark ourselves as not dirty
            vbo.bind();
            vbo.upload(Tessellator.getInstance().getBuffer().end());
            VertexBuffer.unbind();
            clientState.dirty = false;
        }

        // Setup the render state for this render phase (and texture)
        layer.startDrawing();

        // Grab fog and set to an extravagant value
        // TODO: this is a total hack, but it's needed to convince the shader to not apply fog everywhere
        float realEnd = RenderSystem.getShaderFogEnd();
        RenderSystem.setShaderFogEnd(9999999);

        matrices.push();
        // Render the buffer, which contains all the cables connected to this
            vbo.bind();
            vbo.draw(matrices.peek().getPositionMatrix(), RenderSystem.getProjectionMatrix(), GameRenderer.getRenderTypeEntitySolidProgram());
            VertexBuffer.unbind();

        matrices.pop();

        // Reset the render state
        RenderSystem.setShaderFogEnd(realEnd);
        layer.endDrawing();

        matrices.pop();
    }

    @Override
    public boolean rendersOutsideBoundingBox(E blockEntity) {
        return true;
    }
}
