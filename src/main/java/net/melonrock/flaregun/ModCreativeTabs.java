package net.melonrock.flaregun;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, FlaregunMod.MODID);

    public static final RegistryObject<CreativeModeTab> FLAREGUN_TAB = CREATIVE_TABS.register("flaregun_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.flaregun"))
                    .icon(() -> new ItemStack(ModItems.FLAREGUN.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.FLAREGUN.get());
                        output.accept(ModItems.FLARE_SHELL.get());
                    })
                    .build());
}
