package net.melonrock.flaregun;

import net.melonrock.flaregun.entity.FlareEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, FlaregunMod.MODID);

    public static final RegistryObject<EntityType<FlareEntity>> FLARE =
            ENTITY_TYPES.register("flare",
                    () -> EntityType.Builder.<FlareEntity>of(FlareEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("flare"));
}
