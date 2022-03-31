package io.github.foundationgames.phonos.client.model;

import io.github.foundationgames.phonos.Phonos;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class PianoRollModel extends Model {
    public static final Identifier TEXTURE = Phonos.id("textures/entity/piano_roll.png");
    public static final Identifier TEXTURE_EMPTY = Phonos.id("textures/entity/empty_piano_roll.png");

    private final ModelPart stand;
    private final ModelPart roll;

    private boolean showRoll = true;

    public PianoRollModel(ModelPart root) {
        super(RenderLayer::getEntityCutoutNoCull);

        this.stand = root.getChild("stand");
        this.roll = root.getChild("roll");
    }

    public void setAngle(float degrees) {
        this.stand.yaw = (float) Math.toRadians(degrees);
        this.roll.yaw = (float) Math.toRadians(45 + degrees);
    }

    public void setRollShown(boolean show) {
        this.showRoll = show;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        this.stand.render(matrices, vertices, light, overlay, red, green, blue, alpha);

        if (this.showRoll) {
            this.roll.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        }

        this.showRoll = true;
    }
}
