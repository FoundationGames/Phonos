package io.github.foundationgames.phonos.client.render.block;

import io.github.foundationgames.phonos.PhonosClient;
import io.github.foundationgames.phonos.client.model.AudioCableEndModel;
import io.github.foundationgames.phonos.client.render.ConnectionRenderer;
import io.github.foundationgames.phonos.world.sound.block.OutputBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

public class CableOutputBlockEntityRenderer<E extends BlockEntity & OutputBlockEntity> implements BlockEntityRenderer<E> {
    private final AudioCableEndModel cableEndModel;

    public CableOutputBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        cableEndModel = new AudioCableEndModel(ctx.getLayerModelPart(PhonosClient.AUDIO_CABLE_END_LAYER));
    }

    @Override
    public void render(E entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        var buffer = vertexConsumers.getBuffer(cableEndModel.getLayer(AudioCableEndModel.TEXTURE));

        matrices.push();
        matrices.translate(-entity.getPos().getX(), -entity.getPos().getY(), -entity.getPos().getZ());

        entity.getOutputs().forEach((i, conn) ->
                ConnectionRenderer.renderConnection(entity.getWorld(), conn, matrices, buffer, cableEndModel, overlay, tickDelta));

        matrices.pop();
    }

    @Override
    public boolean rendersOutsideBoundingBox(E blockEntity) {
        return true;
    }
}
