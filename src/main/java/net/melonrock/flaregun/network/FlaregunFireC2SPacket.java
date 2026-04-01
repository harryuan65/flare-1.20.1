package net.melonrock.flaregun.network;

import net.melonrock.flaregun.ModItems;
import net.melonrock.flaregun.ModSounds;
import net.melonrock.flaregun.entity.FlareEntity;
import net.melonrock.flaregun.item.FlaregunItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * C2S：玩家左鍵發射信號槍。
 * 伺服端驗證、生成 FlareEntity、播放音效、設定冷卻。
 */
public class FlaregunFireC2SPacket {

    public static void encode(FlaregunFireC2SPacket packet, FriendlyByteBuf buf) {}

    public static FlaregunFireC2SPacket decode(FriendlyByteBuf buf) {
        return new FlaregunFireC2SPacket();
    }

    public static void handle(FlaregunFireC2SPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            ItemStack stack = player.getMainHandItem();
            if (stack.isEmpty() || stack.getItem() != ModItems.FLAREGUN.get()) return;
            if (!FlaregunItem.isLoaded(stack)) return;
            if (player.getCooldowns().isOnCooldown(ModItems.FLAREGUN.get())) return;

            // 消耗彈藥
            FlaregunItem.setLoaded(stack, false);
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);

            // 短暫冷卻（防止重複封包）
            player.getCooldowns().addCooldown(ModItems.FLAREGUN.get(), 5);

            // 生成信號彈
            Vec3 eye = player.getEyePosition(1f);
            Vec3 dir = player.getLookAngle();
            Vec3 start = eye.add(dir.scale(0.5));
            FlareEntity flare = FlareEntity.create(player.level(), player, start, dir);
            player.level().addFreshEntity(flare);

            // 播放發射音效
            player.level().playSound(null,
                    player.getX(), player.getY(), player.getZ(),
                    ModSounds.FLAREGUN_FIRE.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        });
        ctx.setPacketHandled(true);
    }
}
