package net.melonrock.flaregun.network;

import net.melonrock.flaregun.ModItems;
import net.melonrock.flaregun.ModSounds;
import net.melonrock.flaregun.item.FlaregunItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * C2S：玩家按下 R 鍵換彈。
 * 伺服端驗證、消耗 1 FlareShellItem、設定已裝填、觸發 reload 動畫、播放音效、加冷卻。
 */
public class FlaregunReloadC2SPacket {

    public static void encode(FlaregunReloadC2SPacket packet, FriendlyByteBuf buf) {}

    public static FlaregunReloadC2SPacket decode(FriendlyByteBuf buf) {
        return new FlaregunReloadC2SPacket();
    }

    public static void handle(FlaregunReloadC2SPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            ItemStack gunStack = player.getMainHandItem();
            if (gunStack.isEmpty() || gunStack.getItem() != ModItems.FLAREGUN.get()) return;
            if (FlaregunItem.isLoaded(gunStack)) return;
            if (player.getCooldowns().isOnCooldown(ModItems.FLAREGUN.get())) return;

            // 找背包中的 FlareShellItem
            ItemStack shellStack = findShell(player);
            if (shellStack == null && !player.getAbilities().instabuild) return;

            // 消耗彈藥
            if (!player.getAbilities().instabuild && shellStack != null) {
                shellStack.shrink(1);
            }

            // 裝填
            FlaregunItem.setLoaded(gunStack, true);
            player.setItemInHand(InteractionHand.MAIN_HAND, gunStack);
            player.getCooldowns().addCooldown(ModItems.FLAREGUN.get(), FlaregunItem.RELOAD_COOLDOWN_TICKS);

            // 播放換彈音效
            player.level().playSound(null,
                    player.getX(), player.getY(), player.getZ(),
                    ModSounds.FLAREGUN_RELOAD.get(), SoundSource.PLAYERS, 0.8f, 1.0f);
        });
        ctx.setPacketHandled(true);
    }

    private static ItemStack findShell(ServerPlayer player) {
        for (ItemStack s : player.getInventory().items) {
            if (!s.isEmpty() && s.getItem() == ModItems.FLARE_SHELL.get()) return s;
        }
        ItemStack off = player.getInventory().offhand.get(0);
        if (!off.isEmpty() && off.getItem() == ModItems.FLARE_SHELL.get()) return off;
        return null;
    }
}
