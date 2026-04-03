package net.melonrock.flaregun.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.melonrock.flaregun.entity.IlluminationFlareEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

/**
 * 照明彈渲染：小彩色方塊（#E09D38），邊長 0.2，全亮度。
 */
public class IlluminationFlareEntityRenderer extends EntityRenderer<IlluminationFlareEntity> {

    public IlluminationFlareEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    protected int getBlockLightLevel(IlluminationFlareEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void render(IlluminationFlareEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        VertexConsumer vc = buffer.getBuffer(RenderType.lightning());
        float h = 0.10f;
        float r = 224 / 255f, g = 157 / 255f, b = 56 / 255f, a = 1.0f;
        Matrix4f m = poseStack.last().pose();

        // +Y (top) — CCW from above: (-,h,-) → (-,h,+) → (+,h,+) → (+,h,-)
        vc.vertex(m, -h,  h, -h).color(r, g, b, a).endVertex();
        vc.vertex(m, -h,  h,  h).color(r, g, b, a).endVertex();
        vc.vertex(m,  h,  h,  h).color(r, g, b, a).endVertex();
        vc.vertex(m,  h,  h, -h).color(r, g, b, a).endVertex();
        // -Y (bottom) — CCW from below
        vc.vertex(m, -h, -h,  h).color(r, g, b, a).endVertex();
        vc.vertex(m, -h, -h, -h).color(r, g, b, a).endVertex();
        vc.vertex(m,  h, -h, -h).color(r, g, b, a).endVertex();
        vc.vertex(m,  h, -h,  h).color(r, g, b, a).endVertex();
        // +X (right) — CCW from +X
        vc.vertex(m,  h, -h,  h).color(r, g, b, a).endVertex();
        vc.vertex(m,  h, -h, -h).color(r, g, b, a).endVertex();
        vc.vertex(m,  h,  h, -h).color(r, g, b, a).endVertex();
        vc.vertex(m,  h,  h,  h).color(r, g, b, a).endVertex();
        // -X (left) — CCW from -X
        vc.vertex(m, -h, -h, -h).color(r, g, b, a).endVertex();
        vc.vertex(m, -h, -h,  h).color(r, g, b, a).endVertex();
        vc.vertex(m, -h,  h,  h).color(r, g, b, a).endVertex();
        vc.vertex(m, -h,  h, -h).color(r, g, b, a).endVertex();
        // +Z (south) — CCW from +Z
        vc.vertex(m, -h, -h,  h).color(r, g, b, a).endVertex();
        vc.vertex(m,  h, -h,  h).color(r, g, b, a).endVertex();
        vc.vertex(m,  h,  h,  h).color(r, g, b, a).endVertex();
        vc.vertex(m, -h,  h,  h).color(r, g, b, a).endVertex();
        // -Z (north) — CCW from -Z
        vc.vertex(m,  h, -h, -h).color(r, g, b, a).endVertex();
        vc.vertex(m, -h, -h, -h).color(r, g, b, a).endVertex();
        vc.vertex(m, -h,  h, -h).color(r, g, b, a).endVertex();
        vc.vertex(m,  h,  h, -h).color(r, g, b, a).endVertex();

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    @SuppressWarnings("deprecation")
    public ResourceLocation getTextureLocation(IlluminationFlareEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
