package net.melonrock.flaregun.client;

import net.melonrock.flaregun.ModItems;
import net.melonrock.flaregun.item.FlaregunItem;
import net.melonrock.flaregun.network.FlaregunFireC2SPacket;
import net.melonrock.flaregun.network.FlaregunReloadC2SPacket;
import net.melonrock.flaregun.network.ModMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 客戶端輸入處理：
 * <ul>
 *   <li>左鍵：攔截攻擊、發射信號彈（單次觸發）</li>
 *   <li>R 鍵：換彈（需持有 FlareShellItem）</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = "flaregun", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class FlaregunInputHandler {

    private static boolean wasAttackDown = false;
    private static boolean wasHoldingFlaregun = false;

    // ── 攔截左鍵 / 右鍵，避免手臂揮動與方塊互動 ──

    @SubscribeEvent
    public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        ItemStack main = mc.player.getMainHandItem();
        if (main.isEmpty() || main.getItem() != ModItems.FLAREGUN.get()) return;

        if (event.isAttack() || event.isUseItem()) {
            event.setCanceled(true);
            event.setSwingHand(false);
        }
    }

    // ── 每 tick 處理 ──

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || player.isSpectator()) return;

        ItemStack main = player.getMainHandItem();
        boolean hasFlaregun = !main.isEmpty() && main.getItem() == ModItems.FLAREGUN.get();

        // 拿出動畫（記錄 justEquipped 讓同 tick 不遞減，確保 forceAnimationReset 觸發）
        boolean justEquipped = hasFlaregun && !wasHoldingFlaregun;
        if (justEquipped) {
            FlaregunItem.drawAnimTicksRemaining = FlaregunItem.DRAW_ANIM_TICKS;
            FlaregunItem.drawResetPending = true;
        }
        wasHoldingFlaregun = hasFlaregun;

        // 遞減動畫 tick
        if (FlaregunItem.reloadAnimTicksRemaining > 0) FlaregunItem.reloadAnimTicksRemaining--;
        if (FlaregunItem.fireAnimTicksRemaining > 0) FlaregunItem.fireAnimTicksRemaining--;
        if (!hasFlaregun) {
            FlaregunItem.drawAnimTicksRemaining = 0;
        } else if (!justEquipped && FlaregunItem.drawAnimTicksRemaining > 0) {
            FlaregunItem.drawAnimTicksRemaining--;
        }

        if (!hasFlaregun) {
            wasAttackDown = false;
            return;
        }

        // ── 左鍵發射（單次觸發，非連發）──
        boolean attackDown = mc.options.keyAttack.isDown();
        if (attackDown && !wasAttackDown) {
            if (FlaregunItem.isLoaded(main)
                    && !player.getCooldowns().isOnCooldown(ModItems.FLAREGUN.get())
                    && FlaregunItem.reloadAnimTicksRemaining == 0) {
                // 觸發客戶端火焰動畫
                FlaregunItem.fireAnimTicksRemaining = FlaregunItem.FIRE_ANIM_TICKS;
                FlaregunItem.fireResetPending = true;
                // 傳送封包給伺服端
                ModMessages.INSTANCE.sendToServer(new FlaregunFireC2SPacket());
            }
        }
        wasAttackDown = attackDown;

        // ── R 鍵換彈 ──
        if (ModKeyBindings.RELOAD_KEY.consumeClick()) {
            if (!FlaregunItem.isLoaded(main)
                    && !player.getCooldowns().isOnCooldown(ModItems.FLAREGUN.get())
                    && FlaregunItem.reloadAnimTicksRemaining == 0
                    && hasShellInInventory(player)) {
                FlaregunItem.reloadAnimTicksRemaining = FlaregunItem.RELOAD_ANIM_TICKS;
                FlaregunItem.reloadResetPending = true;
                ModMessages.INSTANCE.sendToServer(new FlaregunReloadC2SPacket());
            }
        }
    }

    private static boolean hasShellInInventory(LocalPlayer player) {
        for (ItemStack s : player.getInventory().items) {
            if (!s.isEmpty() && s.getItem() == ModItems.ILLUMINATION_FLARE.get()) return true;
        }
        ItemStack off = player.getInventory().offhand.get(0);
        return !off.isEmpty() && off.getItem() == ModItems.ILLUMINATION_FLARE.get();
    }
}
