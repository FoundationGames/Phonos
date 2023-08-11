package io.github.foundationgames.phonos.client.render.block;

import io.github.foundationgames.phonos.Phonos;
import io.github.foundationgames.phonos.PhonosClient;
import io.github.foundationgames.phonos.client.model.BasicModel;
import io.github.foundationgames.phonos.client.render.CableBounds;
import io.github.foundationgames.phonos.client.render.CableRenderer;
import io.github.foundationgames.phonos.client.render.CableVBOContainer;
import io.github.foundationgames.phonos.config.PhonosClientConfig;
import io.github.foundationgames.phonos.mixin.WorldRendererAccess;
import io.github.foundationgames.phonos.world.sound.block.OutputBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class CableOutputBlockEntityRenderer<E extends BlockEntity & OutputBlockEntity> implements BlockEntityRenderer<E> {
    public static final Identifier TEXTURE = Phonos.id("textures/entity/audio_cable.png");
    private final BasicModel cableEndModel;

    private final CableBounds boundCache = new CableBounds();

    public CableOutputBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        cableEndModel = new BasicModel(ctx.getLayerModelPart(PhonosClient.AUDIO_CABLE_END_LAYER));
    }

    @Override
    public void render(E entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        var renderLayer = cableEndModel.getLayer(TEXTURE);
        var immediate = vertexConsumers.getBuffer(renderLayer);
        var config = PhonosClientConfig.get();
        var frustum = ((WorldRendererAccess) MinecraftClient.getInstance().worldRenderer).phonos$getFrustum();

        matrices.push();

        matrices.translate(-entity.getPos().getX(), -entity.getPos().getY(), -entity.getPos().getZ());

        boolean vbos = config.cableVBOs;
        entity.enforceVBOState(vbos);

        if (vbos) {
            CableVBOContainer vboContainer = entity.getOrCreateVBOContainer();
            vboContainer.render(matrices, immediate, renderLayer, cableEndModel, frustum, entity.getOutputs(), config, entity.getWorld(), overlay, tickDelta);
        } else {
            entity.getOutputs().forEach((i, conn) ->
                    CableRenderer.renderConnection(null, config, entity.getWorld(), conn, boundCache, frustum,
                            matrices, immediate, cableEndModel, overlay, tickDelta));
        }

        matrices.pop();
    }

    @Override
    public boolean rendersOutsideBoundingBox(E blockEntity) {
        return true;
    }
}
