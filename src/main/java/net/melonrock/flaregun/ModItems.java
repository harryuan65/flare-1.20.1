package net.melonrock.flaregun;

import net.melonrock.flaregun.item.FlaregunItem;
import net.melonrock.flaregun.item.IlluminationFlareItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, FlaregunMod.MODID);

    public static final RegistryObject<Item> FLAREGUN = ITEMS.register("flaregun",
            () -> new FlaregunItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> ILLUMINATION_FLARE = ITEMS.register("illumination_flare",
            () -> new IlluminationFlareItem(new Item.Properties().stacksTo(64)));
}
