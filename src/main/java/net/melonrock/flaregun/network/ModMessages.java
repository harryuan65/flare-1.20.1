package net.melonrock.flaregun.network;

import net.melonrock.flaregun.FlaregunMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModMessages {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(FlaregunMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        INSTANCE.registerMessage(packetId++,
                FlaregunFireC2SPacket.class,
                FlaregunFireC2SPacket::encode,
                FlaregunFireC2SPacket::decode,
                FlaregunFireC2SPacket::handle);

        INSTANCE.registerMessage(packetId++,
                FlaregunReloadC2SPacket.class,
                FlaregunReloadC2SPacket::encode,
                FlaregunReloadC2SPacket::decode,
                FlaregunReloadC2SPacket::handle);
    }
}
