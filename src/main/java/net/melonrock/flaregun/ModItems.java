package net.melonrock.flaregun;

import net.melonrock.flaregun.item.FlaregunItem;
import net.melonrock.flaregun.item.FlareShellItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, FlaregunMod.MODID);

    public static final RegistryObject<Item> FLAREGUN = ITEMS.register("flaregun",
            () -> new FlaregunItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> FLARE_SHELL = ITEMS.register("flare_shell",
            () -> new FlareShellItem(new Item.Properties().stacksTo(64)));
}
