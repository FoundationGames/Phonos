package io.github.foundationgames.phonos.client.render.block;

import io.github.foundationgames.phonos.block.entity.RadioLoudspeakerBlockEntity;
import io.github.foundationgames.phonos.radio.RadioStorage;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.RotationAxis;

public class RadioLoudspeakerBlockEntityRenderer implements BlockEntityRenderer<RadioLoudspeakerBlockEntity> {
    public static final Text[] CHANNEL_TO_TEXT = new Text[RadioStorage.CHANNEL_COUNT];
    public static int TEXT_COLOR = 0xFF2A2A;
    public static int OUTLINE_COLOR = 0x4F0000;

    private final TextRenderer font;

    public RadioLoudspeakerBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.font = ctx.getTextRenderer();
    }

    @Override
    public void render(RadioLoudspeakerBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();

        matrices.translate(0.5, 0.5, 0.5);
        matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(entity.getRotation().asRotation()));
        matrices.translate(0, 0, -0.501);

        matrices.scale(0.0268f, 0.0268f, 0.0268f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotation((float) Math.PI));
        matrices.translate(0, 2.25, 0);

        var text = RadioLoudspeakerBlockEntityRenderer.getTextForChannel(entity.getChannel()).asOrderedText();

        this.font.drawWithOutline(text, -this.font.getWidth(text) * 0.5f, 0,
                RadioLoudspeakerBlockEntityRenderer.TEXT_COLOR, RadioLoudspeakerBlockEntityRenderer.OUTLINE_COLOR,
                matrices.peek().getPositionMatrix(), vertexConsumers, 15728880);

        matrices.pop();
    }

    public static Text getTextForChannel(int channel) {
        if (CHANNEL_TO_TEXT[channel] == null) {
            CHANNEL_TO_TEXT[channel] = Text.literal(Integer.toString(channel));
        }

        return CHANNEL_TO_TEXT[channel];
    }
}
