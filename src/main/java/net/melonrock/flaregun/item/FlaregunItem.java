package net.melonrock.flaregun.item;

import net.melonrock.flaregun.client.FlaregunItemRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

/**
 * 信號槍：左鍵發射（單發制），R 鍵換彈（消耗 FlareShellItem）。
 * GeckoLib 3D 模型，動畫：draw / fire / reload。
 */
public class FlaregunItem extends Item implements GeoItem {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final String NBT_LOADED = "Loaded";

    /** 裝填冷卻：3.0417s ≈ 61 tick（配合 reload 動畫長度） */
    public static final int RELOAD_COOLDOWN_TICKS = 61;
    /** 換彈動畫計數（供動畫 controller 使用） */
    public static final int RELOAD_ANIM_TICKS = 61;
    /** 開火動畫計數：0.25s = 5 tick */
    public static final int FIRE_ANIM_TICKS = 5;
    /** 拿出動畫計數：0.5s = 10 tick */
    public static final int DRAW_ANIM_TICKS = 10;

    // ── 客戶端動畫狀態（static，singleton animatable 共享） ──
    public static int reloadAnimTicksRemaining = 0;
    public static int fireAnimTicksRemaining = 0;
    public static int drawAnimTicksRemaining = 0;
    public static boolean reloadResetPending = false;
    public static boolean fireResetPending = false;
    public static boolean drawResetPending = false;
    /** 供 Renderer 判斷當前渲染視角（GUI/FIXED 不播動畫） */
    public static ItemDisplayContext currentRenderContext = ItemDisplayContext.NONE;

    // ── 動畫 ──
    private static final RawAnimation ANIM_DRAW   = RawAnimation.begin().thenPlay("draw");
    private static final RawAnimation ANIM_FIRE   = RawAnimation.begin().thenPlay("fire");
    private static final RawAnimation ANIM_RELOAD = RawAnimation.begin().thenPlay("reload");

    public FlaregunItem(Properties props) {
        super(props);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    // ── GeckoLib ──

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 主 controller：reload > draw > fire > idle
        controllers.add(new AnimationController<>(this, "main", 0, this::handleMainAnim));
    }

    private PlayState handleMainAnim(software.bernie.geckolib.core.animation.AnimationState<FlaregunItem> state) {
        if (currentRenderContext == ItemDisplayContext.GUI
                || currentRenderContext == ItemDisplayContext.FIXED) {
            return PlayState.CONTINUE;
        }
        if (reloadAnimTicksRemaining > 0) {
            if (reloadResetPending) { state.getController().forceAnimationReset(); reloadResetPending = false; }
            state.getController().setAnimation(ANIM_RELOAD);
            return PlayState.CONTINUE;
        }

        if (drawAnimTicksRemaining > 0) {
            if (drawResetPending) { state.getController().forceAnimationReset(); drawResetPending = false; }
            state.getController().setAnimation(ANIM_DRAW);
            return PlayState.CONTINUE;
        }
        if (fireAnimTicksRemaining > 0) {
            if (fireResetPending) { state.getController().forceAnimationReset(); fireResetPending = false; }
            state.getController().setAnimation(ANIM_FIRE);
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    // ── 客戶端 extensions（自訂 Renderer + ArmPose） ──

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private FlaregunItemRenderer renderer;

            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new FlaregunItemRenderer();
                }
                return this.renderer;
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }
        });
    }

    // ── 左鍵被 InputHandler 攔截，use() 不處理 ──

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    // ── 彈藥 NBT 工具 ──

    /** 預設 true（出廠即裝填 1 發）。 */
    public static boolean isLoaded(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof FlaregunItem)) return false;
        if (!stack.hasTag() || !stack.getTag().contains(NBT_LOADED)) return true;
        return stack.getTag().getBoolean(NBT_LOADED);
    }

    public static void setLoaded(ItemStack stack, boolean loaded) {
        if (stack.isEmpty() || !(stack.getItem() instanceof FlaregunItem)) return;
        stack.getOrCreateTag().putBoolean(NBT_LOADED, loaded);
    }

    // ── 耐久條：顯示裝填狀態 ──

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return !isLoaded(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return 0;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xFFAA00;
    }
}
