package io.github.foundationgames.phonos.client.render.block;

import io.github.foundationgames.phonos.Phonos;
import io.github.foundationgames.phonos.PhonosClient;
import io.github.foundationgames.phonos.block.entity.SatelliteStationBlockEntity;
import io.github.foundationgames.phonos.client.model.BasicModel;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class SatelliteStationBlockEntityRenderer extends CableOutputBlockEntityRenderer<SatelliteStationBlockEntity> {
    public static final String[] LOAD_ANIM = {"Oooo", "oOoo", "ooOo", "oooO"};
    public static final Text TEXT_LAUNCH_READY = Text.translatable("display.phonos.satellite_station.launch_ready");
    public static final Text TEXT_LAUNCHING = Text.translatable("display.phonos.satellite_station.launching").formatted(Formatting.YELLOW);
    public static final Text TEXT_IN_ORBIT = Text.translatable("display.phonos.satellite_station.in_orbit");
    public static final Text TEXT_ERROR = Text.translatable("display.phonos.satellite_station.error").formatted(Formatting.RED);

    public static final Identifier TEXTURE = Phonos.id("textures/entity/satellite/satellite.png");
    public static final Identifier EXHAUST_TEX_1 = Phonos.id("textures/entity/satellite/exhaust_1.png");
    public static final Identifier EXHAUST_TEX_2 = Phonos.id("textures/entity/satellite/exhaust_2.png");

    public static int TEXT_COLOR = 0xEEEFFF;
    public static int OUTLINE_COLOR = 0x0512A7;

    private final BasicModel satelliteModel;

    private final TextRenderer font;

    public SatelliteStationBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        super(ctx);

        this.satelliteModel = new BasicModel(ctx.getLayerModelPart(PhonosClient.SATELLITE_LAYER));
        this.font = ctx.getTextRenderer();
    }

    @Override
    public void render(SatelliteStationBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        super.render(entity, tickDelta, matrices, vertexConsumers, light, overlay);

        var rocket = entity.getRocket();

        if (rocket != null) {
            matrices.push();

            var origin = entity.launchpadPos();
            var pos = entity.getPos();
            matrices.translate(origin.x - pos.getX(), origin.y - pos.getY(), origin.z - pos.getZ());

            matrices.translate(rocket.getX(tickDelta), rocket.getY(tickDelta), rocket.getZ(tickDelta));

            matrices.multiply(RotationAxis.POSITIVE_Z.rotation((float) Math.PI));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45 + entity.getRotation().asRotation()));

            satelliteModel.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(TEXTURE)),
                    light, overlay, 1, 1, 1, 1);

            if (rocket.inFlight) {
                boolean texFlag = (entity.getWorld().getTime() & 0b11) < 2;

                satelliteModel.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEyes(texFlag ? EXHAUST_TEX_2 : EXHAUST_TEX_1)),
                        light, overlay, 1, 1, 1, 1);
            }

            matrices.pop();
        }

        matrices.push();

        matrices.translate(0.5, 0.5, 0.5);
        matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(180 + entity.getRotation().asRotation()));
        matrices.translate(0, 0, -0.501);

        matrices.scale(0.02f, 0.02f, 0.02f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotation((float) Math.PI));
        matrices.translate(0, 10.75, 0);

        var text = switch (entity.getStatus()) {
            case IN_ORBIT -> TEXT_IN_ORBIT;
            case LAUNCHING -> TEXT_LAUNCHING;
            default -> {
                if (entity.getError() != null) {
                    yield TEXT_ERROR;
                }
                if (entity.getRocket() != null) {
                    yield TEXT_LAUNCH_READY;
                }
                yield Text.literal(LOAD_ANIM[(int) ((entity.getWorld().getTime() / 4) % 4)]);
            }
        };

        this.font.drawWithOutline(text.asOrderedText(), -this.font.getWidth(text) * 0.5f, 0, TEXT_COLOR, OUTLINE_COLOR,
                matrices.peek().getPositionMatrix(), vertexConsumers, 15728880);

        matrices.pop();
    }
}
