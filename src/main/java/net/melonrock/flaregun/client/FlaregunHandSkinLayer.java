package net.melonrock.flaregun.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.melonrock.flaregun.item.FlaregunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.Optional;

/**
 * 信號槍 left_hand、right_hand 使用玩家皮膚貼圖（僅第一人稱）。
 * 搭配 FlaregunHideHandsLayer 在主渲染時隱藏手骨。
 */
public class FlaregunHandSkinLayer extends GeoRenderLayer<FlaregunItem> {

    private static final String LEFT_HAND  = "left_hand";
    private static final String RIGHT_HAND = "right_hand";

    public FlaregunHandSkinLayer(GeoRenderer<FlaregunItem> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, FlaregunItem animatable, BakedGeoModel bakedModel,
                       RenderType renderType, MultiBufferSource buffer, VertexConsumer vertexConsumer,
                       float partialTick, int packedLight, int packedOverlay) {
        if (getRenderer() instanceof FlaregunItemRenderer fr) {
            ItemDisplayContext ctx = fr.getRenderPerspective();
            if (ctx == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND || ctx == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                    || ctx == ItemDisplayContext.GUI || ctx == ItemDisplayContext.FIXED
                    || ctx == ItemDisplayContext.GROUND) {
                return;
            }
        }
        ResourceLocation skinTexture = getPlayerSkinTexture();
        if (skinTexture == null) return;

        RenderType skinRenderType = RenderType.entityTranslucent(skinTexture);
        VertexConsumer skinBuffer = buffer.getBuffer(skinRenderType);

        for (String boneName : new String[]{LEFT_HAND, RIGHT_HAND}) {
            Optional<GeoBone> opt = bakedModel.getBone(boneName);
            if (opt.isEmpty()) continue;
            GeoBone bone = opt.get();
            boolean wasHidden = bone.isHidden();
            bone.setHidden(false);
            getRenderer().renderRecursively(poseStack, animatable, bone, skinRenderType, buffer, skinBuffer,
                    false, partialTick, packedLight, packedOverlay, 1f, 1f, 1f, 1f);
            bone.setHidden(wasHidden);
        }
    }

    private static ResourceLocation getPlayerSkinTexture() {
        var player = Minecraft.getInstance().player;
        if (player != null) {
            ResourceLocation skin = player.getSkinTextureLocation();
            if (skin != null) return skin;
        }
        return ResourceLocation.withDefaultNamespace("textures/entity/steve.png");
    }
}
