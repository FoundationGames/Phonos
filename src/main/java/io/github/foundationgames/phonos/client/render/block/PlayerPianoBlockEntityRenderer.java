package io.github.foundationgames.phonos.client.render.block;

import io.github.foundationgames.phonos.PhonosClient;
import io.github.foundationgames.phonos.block.entity.PlayerPianoBlockEntity;
import io.github.foundationgames.phonos.client.model.PianoKeyboardModel;
import io.github.foundationgames.phonos.client.model.PianoRollModel;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Vec3f;

public class PlayerPianoBlockEntityRenderer implements BlockEntityRenderer<PlayerPianoBlockEntity> {
    private final PianoKeyboardModel keyboardModel;
    private final PianoRollModel rollModel;

    public PlayerPianoBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.keyboardModel = new PianoKeyboardModel(ctx.getLayerModelPart(PhonosClient.KEYBOARD_MODEL_LAYER));
        this.rollModel = new PianoRollModel(ctx.getLayerModelPart(PhonosClient.PIANO_ROLL_MODEL_LAYER));
    }

    @Override
    public void render(PlayerPianoBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        this.keyboardModel.setFrom(entity.keyboard, tickDelta);

        matrices.push();

        matrices.scale(1, -1, -1);
        matrices.translate(0.5, 0, -0.5);
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(entity.getCachedState().get(Properties.HORIZONTAL_FACING).asRotation()));
        matrices.translate(-0.5, 0, 0.5);

        this.keyboardModel.render(matrices, vertexConsumers.getBuffer(this.keyboardModel.getLayer(PianoKeyboardModel.TEXTURE)), light, overlay, 1, 1, 1, 1);

        matrices.pop();

        matrices.push();

        matrices.translate(0.5, 0, 0.5);
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(360 - entity.getCachedState().get(Properties.HORIZONTAL_FACING).asRotation()));
        matrices.translate(-0.5, 0, -0.5);
        matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(90));
        matrices.translate(0.84375, -0.75, 0.53125);

        float angle = -4 * (entity.getWorld().getTime() + tickDelta);

        this.rollModel.setRollShown(entity.hasRoll());
        this.rollModel.setAngle(entity.rollTurning() ? angle : 0);
        this.rollModel.render(matrices, vertexConsumers.getBuffer(this.rollModel.getLayer(PianoRollModel.TEXTURE)), light, overlay, 1, 1, 1, 1);

        matrices.pop();
    }
}
