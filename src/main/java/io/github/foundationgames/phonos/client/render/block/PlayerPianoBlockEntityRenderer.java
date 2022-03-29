package io.github.foundationgames.phonos.client.render.block;

import io.github.foundationgames.phonos.Phonos;
import io.github.foundationgames.phonos.PhonosClient;
import io.github.foundationgames.phonos.block.entity.PlayerPianoBlockEntity;
import io.github.foundationgames.phonos.client.model.PianoKeyboardModel;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;

public class PlayerPianoBlockEntityRenderer implements BlockEntityRenderer<PlayerPianoBlockEntity> {
    private static final Identifier TEXTURE = Phonos.id("textures/entity/keyboard/keyboard.png");

    private final PianoKeyboardModel keyboardModel;

    public PlayerPianoBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.keyboardModel = new PianoKeyboardModel(ctx.getLayerModelPart(PhonosClient.KEYBOARD_MODEL_LAYER));
    }

    @Override
    public void render(PlayerPianoBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        this.keyboardModel.setFrom(entity.keyboard, tickDelta);

        matrices.push();

        matrices.scale(1, -1, -1);
        matrices.translate(0.5, 0, -0.5);
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(entity.getCachedState().get(Properties.HORIZONTAL_FACING).asRotation()));
        matrices.translate(-0.5, 0, 0.5);

        this.keyboardModel.render(matrices, vertexConsumers.getBuffer(this.keyboardModel.getLayer(TEXTURE)), light, overlay, 1, 1, 1, 1);

        matrices.pop();
    }
}
