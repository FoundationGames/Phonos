package io.github.foundationgames.phonos.client.render.entity;

import io.github.foundationgames.phonos.PhonosClient;
import io.github.foundationgames.phonos.client.model.HeadsetModel;
import io.github.foundationgames.phonos.item.HeadsetItem;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;

public class HeadsetFeatureRenderer<E extends LivingEntity, M extends BipedEntityModel<E>> extends FeatureRenderer<E, M> {
    private final HeadsetModel headsetModel;

    public HeadsetFeatureRenderer(FeatureRendererContext<E, M> context, EntityModelLoader models) {
        super(context);
        this.headsetModel = new HeadsetModel(models.getModelPart(PhonosClient.HEADSET_LAYER));
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, E entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        var headset = entity.getEquippedStack(EquipmentSlot.HEAD);
        if (headset.getItem() instanceof HeadsetItem item) {
            this.headsetModel.render(matrices, vertexConsumers, this.getContextModel().getHead().getTransform(), light, OverlayTexture.DEFAULT_UV, headset, item);
        }
    }
}
