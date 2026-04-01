package net.melonrock.flaregun.client;

import net.melonrock.flaregun.FlaregunMod;
import net.melonrock.flaregun.item.FlaregunItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class FlaregunGeoModel extends GeoModel<FlaregunItem> {

    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(FlaregunMod.MODID, "geo/flaregun.geo.json");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(FlaregunMod.MODID, "textures/entity/flaregun.png");
    private static final ResourceLocation ANIMATIONS =
            ResourceLocation.fromNamespaceAndPath(FlaregunMod.MODID, "animations/flaregun.animation.json");

    @Override
    public ResourceLocation getModelResource(FlaregunItem animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(FlaregunItem animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(FlaregunItem animatable) {
        return ANIMATIONS;
    }
}
