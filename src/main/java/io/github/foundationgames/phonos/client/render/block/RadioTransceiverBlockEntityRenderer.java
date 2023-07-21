package io.github.foundationgames.phonos.client.render.block;

import io.github.foundationgames.phonos.block.entity.RadioTransceiverBlockEntity;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

public class RadioTransceiverBlockEntityRenderer extends CableOutputBlockEntityRenderer<RadioTransceiverBlockEntity> {
    private final TextRenderer font;

    public RadioTransceiverBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        super(ctx);

        this.font = ctx.getTextRenderer();
    }

    @Override
    public void render(RadioTransceiverBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        super.render(entity, tickDelta, matrices, vertexConsumers, light, overlay);

        matrices.push();

        matrices.translate(0.5, 0.4378, 0.5);

        matrices.scale(0.0268f, 0.0268f, 0.0268f);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(entity.getRotation().asRotation()));

        matrices.translate(0, 4.75, 0);

        var text = RadioLoudspeakerBlockEntityRenderer.getTextForChannel(entity.getChannel()).asOrderedText();

        this.font.drawWithOutline(text, -this.font.getWidth(text) * 0.5f, 0,
                RadioLoudspeakerBlockEntityRenderer.TEXT_COLOR, RadioLoudspeakerBlockEntityRenderer.OUTLINE_COLOR,
                matrices.peek().getPositionMatrix(), vertexConsumers, 15728880);

        matrices.pop();
    }
}
