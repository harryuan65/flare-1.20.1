package net.melonrock.flaregun;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, FlaregunMod.MODID);

    public static final RegistryObject<SoundEvent> FLAREGUN_FIRE = SOUNDS.register("flaregun/fire",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(FlaregunMod.MODID, "flaregun/fire")));

    public static final RegistryObject<SoundEvent> FLAREGUN_RELOAD = SOUNDS.register("flaregun/reload",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(FlaregunMod.MODID, "flaregun/reload")));
}
