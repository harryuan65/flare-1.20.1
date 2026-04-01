package net.melonrock.flaregun.client;

import java.util.List;
import net.melonrock.flaregun.item.FlaregunItem;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.FastBoneFilterGeoLayer;

/**
 * 主渲染時隱藏 left_hand、right_hand；第一人稱由 FlaregunHandSkinLayer 以玩家皮膚重繪。
 */
public class FlaregunHideHandsLayer extends FastBoneFilterGeoLayer<FlaregunItem> {

    private static final List<String> HAND_BONES = List.of("left_hand", "right_hand");

    public FlaregunHideHandsLayer(GeoRenderer<FlaregunItem> renderer) {
        super(renderer, () -> HAND_BONES, (bone, item, partialTick) -> {
            bone.setHidden(true);
        });
    }
}
