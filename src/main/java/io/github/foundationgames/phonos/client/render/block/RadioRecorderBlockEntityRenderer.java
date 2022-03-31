package io.github.foundationgames.phonos.client.render.block;

import io.github.foundationgames.phonos.PhonosClient;
import io.github.foundationgames.phonos.block.entity.RadioRecorderBlockEntity;
import io.github.foundationgames.phonos.client.model.PianoRollModel;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3f;

public class RadioRecorderBlockEntityRenderer implements BlockEntityRenderer<RadioRecorderBlockEntity> {
    private final PianoRollModel rollModel;

    public RadioRecorderBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.rollModel = new PianoRollModel(ctx.getLayerModelPart(PhonosClient.PIANO_ROLL_MODEL_LAYER));
    }

    @Override
    public void render(RadioRecorderBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();

        matrices.translate(0.5, 0.75, 0.5);
        float angle = -4 * (entity.getWorld().getTime() + tickDelta);

        this.rollModel.setRollShown(entity.hasRoll());
        this.rollModel.setAngle(entity.powered() ? angle : 0);
        this.rollModel.render(matrices, vertexConsumers.getBuffer(this.rollModel.getLayer(PianoRollModel.TEXTURE_EMPTY)), light, overlay, 1, 1, 1, 1);

        matrices.pop();
    }
}
