package io.github.foundationgames.phonos.client.model;

import io.github.foundationgames.phonos.Phonos;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class AudioCableEndModel extends Model {
    public static final Identifier TEXTURE = Phonos.id("textures/entity/audio_cable.png");

    private final ModelPart root;

    public AudioCableEndModel(ModelPart root) {
        super(RenderLayer::getEntitySolid);

        this.root = root;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        this.root.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }
}
