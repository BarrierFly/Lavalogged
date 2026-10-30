package org.lavalogged.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
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
 * Vanilla's {@code placeFluid} only routes {@link net.minecraft.block.FluidFillable FluidFillable}
 * targets through {@code tryFillWithFluid} for water; for every other fluid a container that
 * accepts the fluid via {@code canFillWithFluid} falls into the plain-placement branch and is
 * destroyed and replaced by the fluid block. Since {@link Lavaloggable Lavaloggable} blocks accept
 * lava through {@code canFillWithFluid}, pouring a lava bucket at one - against a neighbouring
 * block, from a dispenser, or while sneaking - would swallow the block entirely. Intercept the
 * call and fill the block properly; when the block cannot take lava (already lavalogged,
 * waterlogged, double slab) refuse instead, never destroying it.
 */
@Mixin(BucketItem.class)
public abstract class BucketItemMixin {

    @Shadow
    @Final
    private Fluid fluid;

    @Shadow
    protected abstract void playEmptyingSound(final PlayerEntity player, final WorldAccess world, final BlockPos pos);

    /**
     * Fill a {@link Lavaloggable Lavaloggable} {@link net.minecraft.block.Block Block} targeted by
     * a lava bucket instead of letting vanilla replace it with a plain lava block
     *
     * @param player {@link PlayerEntity The Player emptying the bucket}
     * @param world {@link World The World reference}
     * @param pos {@link BlockPos The Block Pos the fluid is placed at}
     * @param hitResult {@link BlockHitResult The Block Hit Result}
     * @param callbackInfoReturnable {@link CallbackInfoReturnable<Boolean> The Callback Info Returnable}
     */
    @Inject(method = "placeFluid", at = @At("HEAD"), cancellable = true)
    private void lavalogged$placeFluid(final PlayerEntity player, final World world, final BlockPos pos, final BlockHitResult hitResult, final CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (this.fluid != Fluids.LAVA) {
            return;
        }
        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof Lavaloggable)) {
            return;
        }
        if (Lavaloggable.fillWithLava(world, pos, state)) {
            this.playEmptyingSound(player, world, pos);
            callbackInfoReturnable.setReturnValue(true);
        } else {
            callbackInfoReturnable.setReturnValue(false);
        }
    }

}
