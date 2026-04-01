package net.melonrock.flaregun;

import net.melonrock.flaregun.client.FlareEntityRenderer;
import net.melonrock.flaregun.client.ModKeyBindings;
import net.melonrock.flaregun.network.ModMessages;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(FlaregunMod.MODID)
public class FlaregunMod {

    public static final String MODID = "flaregun";

    public FlaregunMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);
        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModMessages::register);
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // 客戶端額外初始化（如有需要）
        }

        @SubscribeEvent
        public static void onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.FLARE.get(), FlareEntityRenderer::new);
        }

        @SubscribeEvent
        public static void onRegisterKeyMappings(net.minecraftforge.client.event.RegisterKeyMappingsEvent event) {
            ModKeyBindings.onRegisterKeyMappings(event);
        }
    }
}
