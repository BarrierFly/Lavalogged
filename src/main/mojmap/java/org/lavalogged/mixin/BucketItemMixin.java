package org.lavalogged.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import org.lavalogged.Lavaloggable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin class for the {@link BucketItem Bucket Item}
 * <p>
 * Vanilla's {@code emptyContents} only routes {@link net.minecraft.world.level.block.LiquidBlockContainer LiquidBlockContainer}
 * targets through {@code placeLiquid} for water; for every other fluid a container that accepts the
 * fluid via {@code canPlaceLiquid} falls into the plain-placement branch and is destroyed and
 * replaced by the fluid block. Since {@link Lavaloggable Lavaloggable} blocks accept lava through
 * {@code canPlaceLiquid}, pouring a lava bucket at one - against a neighbouring block, from a
 * dispenser, or while sneaking - would swallow the block entirely. Intercept the call and fill the
 * block properly; when the block cannot take lava (already lavalogged, waterlogged, double slab)
 * refuse instead, never destroying it.
 */
@Mixin(BucketItem.class)
public abstract class BucketItemMixin {

    @Shadow
    @Final
    private Fluid content;

    @Shadow
    protected abstract void playEmptySound(final LivingEntity user, final LevelAccessor level, final BlockPos pos);

    /**
     * Fill a {@link Lavaloggable Lavaloggable} {@link net.minecraft.world.level.block.Block Block}
     * targeted by a lava bucket instead of letting vanilla replace it with a plain lava block
     *
     * @param user {@link LivingEntity The Living Entity emptying the bucket}
     * @param level {@link Level The Level reference}
     * @param pos {@link BlockPos The Block Pos the fluid is placed at}
     * @param hitResult {@link BlockHitResult The Block Hit Result}
     * @param callbackInfoReturnable {@link CallbackInfoReturnable<Boolean> The Callback Info Returnable}
     */
    @Inject(method = "emptyContents", at = @At("HEAD"), cancellable = true)
    private void lavalogged$emptyContents(final LivingEntity user, final Level level, final BlockPos pos, final BlockHitResult hitResult, final CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (this.content != Fluids.LAVA) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof Lavaloggable)) {
            return;
        }
        if (Lavaloggable.fillWithLava(level, pos, state)) {
            this.playEmptySound(user, level, pos);
            callbackInfoReturnable.setReturnValue(true);
        } else {
            callbackInfoReturnable.setReturnValue(false);
        }
    }

}
