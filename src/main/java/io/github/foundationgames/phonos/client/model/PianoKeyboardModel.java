package io.github.foundationgames.phonos.client.model;

import io.github.foundationgames.phonos.Phonos;
import io.github.foundationgames.phonos.util.piano.PianoKeyboard;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class PianoKeyboardModel extends Model {
    public static final Identifier TEXTURE = Phonos.id("textures/entity/keyboard.png");

    private final ModelPart leftDummy;
    private final ModelPart rightDummy;
    private final ModelPart[] keys = new ModelPart[25];

    public PianoKeyboardModel(ModelPart root) {
        super(RenderLayer::getEntitySolid);

        leftDummy = root.getChild("l_dummy_key");
        rightDummy = root.getChild("r_dummy_key");

        for (int i = 0; i < keys.length; i++) {
            keys[i] = root.getChild("key"+i);
        }
    }

    public void setFrom(PianoKeyboard keyboard, float tickDelta) {
        for (int key = 0; key < keys.length; key++) {
            // Pitch as in angles, not sound
            keys[key].pitch = (float) -Math.toRadians(2 * Math.cos(2 * Math.PI * keyboard.getAnimationProgress(key, tickDelta)) - 2);
        }
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        matrices.push();

        matrices.translate(0.25, -0.5, -0.6875);
        this.leftDummy.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        this.rightDummy.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        for (var key : this.keys) {
            key.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        }

        matrices.pop();
    }
}
