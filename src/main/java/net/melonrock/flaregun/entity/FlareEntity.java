package net.melonrock.flaregun.entity;

import net.melonrock.flaregun.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * 信號彈拋射物。
 * <ul>
 *   <li>飛行 0.5s（10 tick）：受重力，ray-cast 偵測方塊碰撞，穿過生物（傷害 20 + 擊退 + 點火）。</li>
 *   <li>停滯 20s（400 tick）：不動，產生 LAVA / FLAME particle。</li>
 *   <li>停滯時以 Flare 為中心放置 3×3×3（間距 8 格）共 27 個 minecraft:light 15。</li>
 *   <li>若網格位置在方塊內，沿射線從目標回退至最近可放置表面。</li>
 *   <li>停滯結束後消失，移除所有 light block。</li>
 * </ul>
 */
public class FlareEntity extends Entity {

    private static final double GRAVITY           = 0.04;
    private static final int    FLIGHT_TICKS      = 10;   // 0.5s
    private static final int    HOVER_TICKS       = 400;  // 20s
    private static final float  DAMAGE            = 20.0f;
    private static final float  KNOCKBACK_FORCE   = 1.5f;
    private static final int    FIRE_TICKS_ON_HIT = 100;  // 5s

    /** 3×3×3 網格的軸向間距（格）。相鄰光源距離 = GRID_STEP，< 14 → 無暗帶。 */
    private static final int GRID_STEP = 8;

    private boolean isHovering = false;
    private int hoverTickCount = 0;
    /** 實際放置的所有 light block 位置（含 surface fallback 後的實際座標）。 */
    private final List<BlockPos> lightPositions = new ArrayList<>();

    /** 發射者，用於排除自傷。 */
    @Nullable private LivingEntity owner = null;

    public FlareEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noCulling = true;
    }

    /** 工廠方法，由伺服端封包呼叫。 */
    public static FlareEntity create(Level level, LivingEntity owner, Vec3 start, Vec3 direction) {
        FlareEntity e = new FlareEntity(ModEntities.FLARE.get(), level);
        e.owner = owner;
        e.setPos(start.x, start.y, start.z);
        e.setDeltaMovement(direction.normalize().scale(3.0));
        return e;
    }

    // ── Entity 必要 overrides ──

    @Override
    protected void defineSynchedData() {}

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putBoolean("Hovering", isHovering);
        tag.putInt("HoverTick", hoverTickCount);
        if (!lightPositions.isEmpty()) {
            ListTag list = new ListTag();
            for (BlockPos p : lightPositions) {
                CompoundTag entry = new CompoundTag();
                entry.putInt("X", p.getX());
                entry.putInt("Y", p.getY());
                entry.putInt("Z", p.getZ());
                list.add(entry);
            }
            tag.put("Lights", list);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        isHovering = tag.getBoolean("Hovering");
        hoverTickCount = tag.getInt("HoverTick");
        lightPositions.clear();
        if (tag.contains("Lights")) {
            ListTag list = tag.getList("Lights", 10); // 10 = CompoundTag
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                lightPositions.add(new BlockPos(entry.getInt("X"), entry.getInt("Y"), entry.getInt("Z")));
            }
        }
    }

    // ── 清理：移除所有記錄的 light block ──

    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide()) {
            for (BlockPos p : lightPositions) {
                if (level().getBlockState(p).is(Blocks.LIGHT)) {
                    level().removeBlock(p, false);
                }
            }
            lightPositions.clear();
        }
        super.remove(reason);
    }

    // ── 主 tick ──

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) return;

        if (!isHovering) {
            tickFlying();
        } else {
            tickHovering();
        }
    }

    private void tickFlying() {
        Vec3 delta = getDeltaMovement();
        delta = new Vec3(delta.x, delta.y - GRAVITY, delta.z);
        setDeltaMovement(delta);

        Vec3 oldPos = position();
        Vec3 newPos = oldPos.add(delta);

        // Ray-cast 偵測方塊碰撞
        ClipContext ctx = new ClipContext(oldPos, newPos,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this);
        BlockHitResult blockHit = level().clip(ctx);
        if (blockHit.getType() == HitResult.Type.BLOCK) {
            Vec3 hitPos = blockHit.getLocation();
            setPos(hitPos.x, hitPos.y, hitPos.z);
            enterHoverPhase();
            return;
        }

        BlockState atNew = level().getBlockState(BlockPos.containing(newPos));
        if (!atNew.isAir() && !atNew.canBeReplaced()) {
            enterHoverPhase();
            return;
        }

        setPos(newPos.x, newPos.y, newPos.z);

        if (tickCount >= FLIGHT_TICKS) {
            enterHoverPhase();
            return;
        }

        applyEntityEffects(oldPos);

        if (level() instanceof ServerLevel sl && tickCount % 2 == 0) {
            sl.sendParticles(ParticleTypes.SMOKE,
                    getX(), getY(), getZ(), 3, 0.05, 0.05, 0.05, 0.01);
        }
    }

    private void applyEntityEffects(Vec3 oldPos) {
        double margin = 0.3;
        AABB box = new AABB(
                Math.min(oldPos.x, getX()) - margin, Math.min(oldPos.y, getY()) - margin, Math.min(oldPos.z, getZ()) - margin,
                Math.max(oldPos.x, getX()) + margin, Math.max(oldPos.y, getY()) + margin, Math.max(oldPos.z, getZ()) + margin
        );
        List<LivingEntity> targets = level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e != owner && e.isAlive() && !(e instanceof Player));

        Vec3 dir = getDeltaMovement().normalize();
        for (LivingEntity target : targets) {
            target.hurt(level().damageSources().magic(), DAMAGE);
            target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), FIRE_TICKS_ON_HIT));
            target.knockback(KNOCKBACK_FORCE, -dir.x, -dir.z);
        }
    }

    // ── 停滯：3×3×3 網格光源 ──

    private void enterHoverPhase() {
        isHovering = true;
        setDeltaMovement(Vec3.ZERO);
        lightPositions.clear();

        // 以 Flare 所在方塊為中心，若在方塊內則往上退一格
        BlockPos base = blockPosition();
        if (!isReplaceable(base)) base = base.above();

        int[] steps = { -2 * GRID_STEP, -GRID_STEP, 0, GRID_STEP, 2 * GRID_STEP };
        for (int dx : steps) {
            for (int dy : steps) {
                for (int dz : steps) {
                    BlockPos pos = findPlaceablePos(base, dx, dy, dz);
                    if (pos != null && !lightPositions.contains(pos)) {
                        level().setBlock(pos,
                                Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 15), 3);
                        lightPositions.add(pos);
                    }
                }
            }
        }
    }

    /**
     * 找到從 base 偏移 (dx, dy, dz) 方向上最遠的可放置位置。
     * 若目標格在方塊內，沿射線從目標往 base 方向回退，直到找到可放置的表面格。
     */
    @Nullable
    private BlockPos findPlaceablePos(BlockPos base, int dx, int dy, int dz) {
        int steps = Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
        if (steps == 0) {
            // 中心格
            return isReplaceable(base) ? base : null;
        }
        // 從目標回退到 base，回傳第一個（即最遠的）可放置位置
        for (int step = 0; step <= steps; step++) {
            float t = (float)(steps - step) / steps; // 1.0 → 0.0（目標 → base）
            BlockPos candidate = base.offset(
                    Math.round(dx * t),
                    Math.round(dy * t),
                    Math.round(dz * t)
            );
            if (isReplaceable(candidate)) return candidate;
        }
        return null;
    }

    private boolean isReplaceable(BlockPos pos) {
        BlockState bs = level().getBlockState(pos);
        return bs.isAir() || bs.canBeReplaced();
    }

    private void tickHovering() {
        setDeltaMovement(Vec3.ZERO);
        hoverTickCount++;

        if (level() instanceof ServerLevel sl) {
            // 岩漿火花（每 2 tick）
            if (hoverTickCount % 2 == 0) {
                sl.sendParticles(ParticleTypes.LAVA,
                        getX(), getY(), getZ(), 3, 0.25, 0.1, 0.25, 0.0);
            }
            // 火焰（每 tick）
            sl.sendParticles(ParticleTypes.FLAME,
                    getX(), getY(), getZ(), 2, 0.15, 0.15, 0.15, 0.02);
        }

        if (hoverTickCount >= HOVER_TICKS) {
            discard();
        }
    }
}
