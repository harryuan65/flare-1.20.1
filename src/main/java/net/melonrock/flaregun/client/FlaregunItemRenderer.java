package net.melonrock.flaregun.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.melonrock.flaregun.item.FlaregunItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/**
 * 信號槍 3D 渲染（GeckoLib），含各視角 transform。
 */
public class FlaregunItemRenderer extends GeoItemRenderer<FlaregunItem> {

    private static final float S = 1f / 16f;

    // ── 開發用：GUI/FIXED transform 即時調整（/flaregun transform 指令）──
    public static float devTx = -23f, devTy = -6f,  devTz = 0f;
    public static float devRx =  15f, devRy = 215f, devRz = 0f;
    public static float devScale = 1.0f;

    public FlaregunItemRenderer() {
        super(new FlaregunGeoModel());
        addRenderLayer(new FlaregunHideHandsLayer(this));
        addRenderLayer(new FlaregunHandSkinLayer(this));
    }

    /** 供 FlaregunHandSkinLayer 判斷是否為第一人稱。 */
    public ItemDisplayContext getRenderPerspective() {
        return renderPerspective;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType,
                             PoseStack poseStack, MultiBufferSource buffer,
                             int packedLight, int packedOverlay) {
        FlaregunItem.currentRenderContext = transformType;
        poseStack.pushPose();
        applyDisplayTransform(transformType, poseStack);
        super.renderByItem(stack, transformType, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private void applyDisplayTransform(ItemDisplayContext type, PoseStack ps) {
        float tx, ty, tz, sx, sy, sz;
        float rx = 0, ry = 0, rz = 0;
        switch (type) {
            case FIRST_PERSON_RIGHT_HAND -> {
                tx = 1.75f; ty = -1.5f; tz = -7f;
                rx = (float) Math.toRadians(4f); ry = (float) Math.toRadians(6f); rz = 0;
                sx = sy = sz = 1f;
            }
            case FIRST_PERSON_LEFT_HAND -> {
                tx = 1.75f; ty = -1.5f; tz = -5f;
                sx = sy = sz = 1f;
            }
            case THIRD_PERSON_RIGHT_HAND -> {
                tx = 6f; ty = 2f; tz = -2f;
                sx = sy = sz = 0.6f;
            }
            case THIRD_PERSON_LEFT_HAND -> {
                tx = -6f; ty = 2f; tz = -3f;
                sx = sy = sz = 0.6f;
            }
            case GROUND -> {
                tx = 8f; ty = 6f; tz = 0;
                sx = sy = sz = 0.45f;
            }
            case FIXED, GUI -> {
                tx = devTx; ty = devTy; tz = devTz;
                rx = (float) Math.toRadians(devRx);
                ry = (float) Math.toRadians(devRy);
                rz = (float) Math.toRadians(devRz);
                sx = sy = sz = devScale;
            }
            default -> { tx = ty = tz = 0; sx = sy = sz = 1f; }
        }
        ps.scale(sx, sy, sz);
        if (rx != 0) ps.mulPose(Axis.XP.rotation(rx));
        if (ry != 0) ps.mulPose(Axis.YP.rotation(ry));
        if (rz != 0) ps.mulPose(Axis.ZP.rotation(rz));
        ps.translate(tx * S, ty * S, tz * S);
    }
}
