package io.github.foundationgames.phonos.client.model;

import io.github.foundationgames.phonos.item.HeadsetItem;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

public class HeadsetModel extends BasicModel {
    private final ModelPart main;
    private final ModelPart innerTube;
    private final ModelPart microphone;

    public HeadsetModel(ModelPart root) {
        super(root);

        this.main = root.getChild("main");
        this.innerTube = root.getChild("inner_tube");
        this.microphone = root.getChild("microphone");
    }

    public void render(MatrixStack matrices, VertexConsumerProvider buffers, ModelTransform transform, int light, int overlay, ItemStack stack, HeadsetItem item) {
        matrices.push();
        this.root.setTransform(transform);
        var texture = item.getTexture(stack);

        this.microphone.visible = false; // Todo?
        this.main.visible = true;
        this.innerTube.visible = false;

        var buffer = buffers.getBuffer(RenderLayer.getEntityCutoutNoCull(texture));
        this.render(matrices, buffer, light, overlay, 1, 1, 1, 1);

        this.main.visible = false;
        // this.microphone.visible = false;
        this.innerTube.visible = true;

        int color = item.getColor(stack);
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        this.render(matrices, buffer, light, overlay, r, g, b, 1);

        if (item.isGlowing(stack)) {
            buffer = buffers.getBuffer(RenderLayer.getEyes(texture));

            this.render(matrices, buffer, light, overlay, r, g, b, 1);
        }

        matrices.pop();
    }
}
