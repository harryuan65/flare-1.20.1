package net.melonrock.flaregun.client;

import com.mojang.brigadier.arguments.FloatArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

/**
 * 開發用客戶端指令：即時調整 FlaregunItem GUI/FIXED display transform。
 *
 * 用法：
 *   /flaregun transform          → 顯示當前值與可複製的程式碼片段
 *   /flaregun transform tx <值>  → 設定 translation X（1/16 格為單位）
 *   /flaregun transform ty <值>
 *   /flaregun transform tz <值>
 *   /flaregun transform rx <度>  → 設定 rotation X（角度）
 *   /flaregun transform ry <度>
 *   /flaregun transform rz <度>
 *   /flaregun transform scale <值>
 *   /flaregun transform reset    → 恢復程式碼內預設值
 */
@Mod.EventBusSubscriber(modid = "flaregun", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class FlaregunDevCommands {

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
            literal("flaregun")
                .then(literal("transform")
                    .executes(ctx -> showValues())
                    .then(literal("show").executes(ctx -> showValues()))
                    .then(literal("reset").executes(ctx -> {
                        FlaregunItemRenderer.devTx    = -23f;
                        FlaregunItemRenderer.devTy    =  -6f;
                        FlaregunItemRenderer.devTz    =   0f;
                        FlaregunItemRenderer.devRx    =  15f;
                        FlaregunItemRenderer.devRy    = 215f;
                        FlaregunItemRenderer.devRz    =   0f;
                        FlaregunItemRenderer.devScale =  1.0f;
                        return showValues();
                    }))
                    .then(literal("tx").then(argument("v", FloatArgumentType.floatArg()).executes(ctx -> {
                        FlaregunItemRenderer.devTx = FloatArgumentType.getFloat(ctx, "v");
                        return showValues();
                    })))
                    .then(literal("ty").then(argument("v", FloatArgumentType.floatArg()).executes(ctx -> {
                        FlaregunItemRenderer.devTy = FloatArgumentType.getFloat(ctx, "v");
                        return showValues();
                    })))
                    .then(literal("tz").then(argument("v", FloatArgumentType.floatArg()).executes(ctx -> {
                        FlaregunItemRenderer.devTz = FloatArgumentType.getFloat(ctx, "v");
                        return showValues();
                    })))
                    .then(literal("rx").then(argument("v", FloatArgumentType.floatArg()).executes(ctx -> {
                        FlaregunItemRenderer.devRx = FloatArgumentType.getFloat(ctx, "v");
                        return showValues();
                    })))
                    .then(literal("ry").then(argument("v", FloatArgumentType.floatArg()).executes(ctx -> {
                        FlaregunItemRenderer.devRy = FloatArgumentType.getFloat(ctx, "v");
                        return showValues();
                    })))
                    .then(literal("rz").then(argument("v", FloatArgumentType.floatArg()).executes(ctx -> {
                        FlaregunItemRenderer.devRz = FloatArgumentType.getFloat(ctx, "v");
                        return showValues();
                    })))
                    .then(literal("scale").then(argument("v", FloatArgumentType.floatArg()).executes(ctx -> {
                        FlaregunItemRenderer.devScale = FloatArgumentType.getFloat(ctx, "v");
                        return showValues();
                    })))
                )
        );
    }

    private static int showValues() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return 0;

        float tx = FlaregunItemRenderer.devTx;
        float ty = FlaregunItemRenderer.devTy;
        float tz = FlaregunItemRenderer.devTz;
        float rx = FlaregunItemRenderer.devRx;
        float ry = FlaregunItemRenderer.devRy;
        float rz = FlaregunItemRenderer.devRz;
        float sc = FlaregunItemRenderer.devScale;

        mc.player.sendSystemMessage(Component.literal(String.format(
            "§e[FlareTransform] §ftx=%.1f  ty=%.1f  tz=%.1f  |  rx=%.1f°  ry=%.1f°  rz=%.1f°  |  scale=%.3f",
            tx, ty, tz, rx, ry, rz, sc)));

        mc.player.sendSystemMessage(Component.literal(String.format(
            "§7tx=%.1ff; ty=%.1ff; tz=%.1ff; rx=(float)Math.toRadians(%.1f); ry=(float)Math.toRadians(%.1f); rz=(float)Math.toRadians(%.1f); sx=sy=sz=%.3ff;",
            tx, ty, tz, rx, ry, rz, sc)));

        return 1;
    }
}
